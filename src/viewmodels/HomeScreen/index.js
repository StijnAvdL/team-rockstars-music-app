import React from "react";
import PropTypes from "prop-types";
import { observable, action, autorun, computed, when } from "mobx";
import { observer } from "mobx-react";

import BackButton from "/src/services/backbutton";
import HomeScreen from "/src/components/HomeScreen";
import {
    ErrorDialog,
    ChangedPrivacyDialog,
    ConfirmationDialog,
    RedoDeleteConfirmation
} from "/src/components/HomeScreen/Dialogs";
import { ConfirmationDialogWithOptions } from "/src/components/Dialogs";

import HomeScreenModel from "./HomeScreen";

import { logError } from "/src/services/logger";
import t from "/src/services/translate";
import { httpPost } from "/src/services/httpCall";

const propTypes = {
    go: PropTypes.func.isRequired,
    userModel: PropTypes.object.isRequired,
    study: PropTypes.object.isRequired
};
const STATE_LOADING = 0;
const STATE_READY = 1;
const STATE_NOTE = 2;

@observer
class HomeScreenViewModel extends React.Component {
    @observable status = STATE_LOADING;
    @observable userId = null;
    @observable _dialog = null;
    timelineModel = null;

    constructor(props) {
        super(props);
        this.userId = props.userModel._id;
        this.model = new HomeScreenModel(props);
        this.timelineModel = props.timelineModel;
        this.cancelNote = this.cancelNote.bind(this);
        if (!this.model.rememberDate) {
            this.model.date.setDate(new Date());
        }
        this.model.rememberDate = false;
        when(
            () => !this.model.initializing,
            () => {
                this.status = STATE_READY;
            }
        );
    }

    componentWillMount() {
        BackButton.subscribe("note", this.cancelNote);
    }

    componentWillUnmount() {
        BackButton.unsubscribe("note", this.cancelNote);
        // Just to be sure we'll always end up in default scenario again
        BackButton.setScenario("default");
        // this.model.destroy();
    }

    @computed
    get dialog() {
        if (!this.model.initializing && !this.model.privacyStatementAccepted) {
            const onConfirm = () => {
                this.props.go("/privacyverklaring");
            };
            return ChangedPrivacyDialog({ onConfirm });
        } else {
            return this._dialog;
        }
    }
    set dialog(dialog) {
        this._dialog = dialog;
    }

    @computed
    get results() {
        const results = this.model.results.map(item => {
            if (item.type === "experiment") {
                item.overflowActions = this.experimentOverflowActions(item);
                item.infoAction =
                    item.source === "walking" && item.value.error && item.value.error !== "null"
                        ? this.infoDialog.bind(this, item)
                        : null;
            } else if (item.type === "question" || item.type === "questionContainer") {
                item.overflowActions = this.questionOverflowActions(item);
            }
            return item;
        });
        return results;
    }

    infoDialog(item) {
        const _onConfirm = value => {
            const archiveSpecs = {
                type: value,
                reason: "invalid experiment result"
            };
            this._deleteItem(item, archiveSpecs)
                .then(() => {
                    archiveSpecs.type === "redo" ? this.model.goToExperiment(item.source, null) : null;
                })
                .catch(err => {
                    logError("Error deleting item");
                    this.setError(err.message);
                });

            this.closeDialog();
        };
        var options = [{ label: t("HomeScreen.Results.Delete"), value: "delete" }];
        if (this.model.date.isToday) options.push({ label: t("HomeScreen.Results.Redo"), value: "redo" });
        this.dialog = (
            <ConfirmationDialogWithOptions
                text={t("HomeScreen.Results.TestFailed")}
                onSubmit={_onConfirm}
                onCancel={this.closeDialog}
                options={options}
            />
        );
    }

    experimentOverflowActions(item) {
        let overflowActions = [];
        const redoAction = item => {
            if (item.sourceId) {
                return {
                    label: t("HomeScreen.Results.Redo"),
                    action: () => {
                        this.redoWithConfirm(item);
                    }
                };
            }
        };
        const deleteAction = item => {
            if (item.sourceId) {
                return {
                    label: t("HomeScreen.Results.Delete"),
                    action: () => {
                        this.deleteWithConfirm(item);
                    }
                };
            }
        };
        switch (item.source) {
            case "rdfsScore":
                overflowActions.push(deleteAction(item));
                break;
            default: {
                if (this.model.date.isToday) {
                    let redo = redoAction(item);
                    if (redo) overflowActions.push(redo);
                }
                let deleteItem = deleteAction(item);
                if (deleteItem) overflowActions.push(deleteItem);
            }
        }
        return overflowActions;
    }

    questionOverflowActions(item) {
        // for both questions and questionContainers
        let overflowActions = [];
        if ((item.type === "question" || item.type === "questionContainer") && item.sourceId) {
            if (this.model.date.isToday || this.model.date.isYesterday) {
                overflowActions.push({
                    label: t("HomeScreen.Questions.Redo"),
                    action: () => {
                        this.redoWithConfirm(item);
                    }
                });
            }
            overflowActions.push({
                label: t("HomeScreen.Questions.Delete"),
                action: () => {
                    this.deleteWithConfirm(item);
                }
            });
        }
        return overflowActions;
    }

    // item: { timelineId, timestamp, type, label, result, value, source, sourceId}
    deleteWithConfirm(item) {
        if (item.type === "experiment") {
            this.setRedoDeleteConfirmation(t("HomeScreen.Results.AskReasonDelete"), (value, customReason) => {
                const archiveSpecs = {
                    type: "delete",
                    reason: value,
                    customReason: customReason
                };
                this._deleteItem(item, archiveSpecs).catch(err => {
                    logError("Error deleting item");
                    this.setError(err.message);
                });
            });
        } else {
            const tKeys = {
                question: "HomeScreen.Questions.ConfirmDelete",
                questionContainer: "HomeScreen.Questions.ConfirmDelete"
            };
            this.setConfirmationDialog(t(tKeys[item.type]), () => {
                const archiveSpecs = {
                    type: "delete",
                    reason: "Unknown",
                    customReason: ""
                };
                this._deleteItem(item, archiveSpecs).catch(err => {
                    logError("Error deleting item");
                    this.setError(err.message);
                });
            });
        }
    }

    // item: { timelineId, timestamp, type, label, result, value, source, sourceId}
    redoWithConfirm(item) {
        if (item.type === "experiment") {
            this.setRedoDeleteConfirmation(t("HomeScreen.Results.AskReasonRedo"), (value, customReason) => {
                const archiveSpecs = {
                    type: "redo",
                    reason: value,
                    customReason: customReason
                };
                this._deleteItem(item, archiveSpecs)
                    .then(this.model.todoItemAction(item.type, item.source))
                    .catch(err => {
                        logError("Error deleting item");
                        this.setError(err.message);
                    });
                this.model.goToExperiment(item.source, null)
            });
        } else {
            const tKeys = {
                question: "HomeScreen.Questions.ConfirmRedo",
                questionContainer: "HomeScreen.Questions.ConfirmRedo"
            };
            this.setConfirmationDialog(t(tKeys[item.type]), () => {
                const archiveSpecs = {
                    type: "redo",
                    reason: "Other",
                    customReason: "Unknown"
                };
                this._deleteItem(item, archiveSpecs)
                    .then(this.model.todoItemAction(item.type, item.source))
                    .catch(err => {
                        logError("Error deleting item");
                        this.setError(err.message);
                    });

                this.model.goToQuestion(item.source, null)
            });
        }
    }

    _deleteItem(item, archived) {
        this.timelineModel.removeItem(item);
        if (item.type === "questionContainer") {
            return new Promise((resolve, reject) => {
                httpPost(
                    `${process.env.SERVER_URL}/v1/sherpa-observation/archive?token=${localStorage.id_token}`,
                    { observationId: item.sourceId, archived },
                    () => this.deleteTimelineItem(item.timelineId).then(() => resolve())
                );
            });
        } else if (item.type === "experiment") {
            return new Promise((resolve, reject) => {
                httpPost(
                    `${process.env.SERVER_URL}/v1/sherpa-experiment/archive?token=${localStorage.id_token}`,
                    { experimentId: item.sourceId, archived },
                    () => this.deleteTimelineItem(item.timelineId).then(() => resolve())
                );
            });
        }
    }

    deleteTimelineItem(timelineId) {
        return new Promise((resolve, reject) => {
            httpPost(
                `${process.env.SERVER_URL}/v1/sherpa-timeline/delete?token=${localStorage.id_token}`,
                { timelineId: timelineId },
                () => resolve()
            );
        });
    }

    @action.bound
    gotoNotePage() {
        BackButton.setScenario("note");
        this.status = STATE_NOTE;
    }

    @action.bound
    saveNote(result) {
        BackButton.setScenario("default");
        const note = this.timelineModel.note;
        note.timestamp = new Date(this.model.date.toDate.getTime());
        note.content = result.text;
        return new Promise((resolve, reject) => {
            note
                .save()
                .then((result) => {
                    this.timelineModel.all.push(this.timelineModel.prepareResultItem(result))
                    this.status = STATE_READY
                })
                .catch(error => {
                    logError("Error saving Note");
                    reject(error);
                });
        });
    }

    @action.bound
    deleteNote() {
        const note = this.timelineModel.note;
        const timelineId = note.timelineId;
        if (note.noteId) {
            this.setConfirmationDialog(
                t("HomeScreen.Notes.ConfirmDelete"),
                () => {
                    this.closeDialog();
                    note
                        .delete()
                        .then(() => {
                            this.timelineModel.removeNote(timelineId)
                        })
                        .catch(err => {
                            logError("Error deleting note");
                            this.setError(err.message);
                        });
                },
                () => {
                    this.closeDialog();
                }
            );
        }
    }

    @action.bound
    cancelNote() {
        BackButton.setScenario("default");
        this.status = STATE_READY;
    }

    @action.bound
    setError(error) {
        this.dialog = ErrorDialog({
            text: error,
            onConfirm: this.closeDialog
        });
    }

    @action.bound
    setConfirmationDialog(text, onConfirm) {
        const _onConfirm = () => {
            this.closeDialog();
            onConfirm();
        };
        this.dialog = ConfirmationDialog({
            text,
            onConfirm: _onConfirm,
            onCancel: this.closeDialog
        });
    }

    @action.bound
    setRedoDeleteConfirmation(text, action) {
        const onConfirm = (value, customValue) => {
            this.closeDialog();
            action(value, customValue);
        };
        this.dialog = RedoDeleteConfirmation({
            text,
            onConfirm,
            onCancel: this.closeDialog
        });
    }

    @action.bound
    closeDialog() {
        this.dialog = null;
    }

    render() {
        const { go } = this.props;
        let view;
        if (this.status === STATE_LOADING) {
            view = "loading";
        } else if (this.status === STATE_NOTE) {
            view = "note";
        } else {
            view = "default";
        }
        return (
            <HomeScreen
                view={view}
                dialog={this.dialog}
                // Navigator
                date={this.model.date.moment}
                nextDay={this.model.date.reachedToday ? null : this.model.date.next}
                previousDay={this.model.date.previous}
                gotoDate={this.model.date.setDate}
                // ToDoBlock
                toDoItems={this.model.toDoItems}
                overflowToDoItems={this.model.overflowToDoItems}
                // DataBlock
                results={this.results}
                // NotesBlock
                note={this.timelineModel.note}
                addNote={this.gotoNotePage}
                editNote={this.gotoNotePage}
                deleteNote={this.deleteNote}
                //
                saveNote={this.saveNote}
                cancelNote={this.cancelNote}
                //
                goToTimegraph={() => go("/timegraph")}
            />
        );
    }
}
HomeScreenModel.propTypes = propTypes;
export default HomeScreenViewModel;
