import React from "react";
import PropTypes from "prop-types";
import { observer, PropTypes as MobxPropTypes } from "mobx-react";

import AppBar from "/src/components/AppBar";
import { ContentContainer, Loading } from "/orikami/components/main";
import Note from "/src/components/Note";

import Navigator from "./Navigator";
import ToDoBlock from "./ToDoBlock";
import DataBlock from "./DataBlock";
import NotesBlock from "./NotesBlock";

import AnalysisIcon from "material-ui/svg-icons/action/timeline";

import Moment from "moment";
import t from "/src/services/translate";
import menuToggle from "/src/services/menuToggle";

const propTypes = {
    view: PropTypes.oneOf(["loading", "default", "note"]),
    dialog: PropTypes.node,
    date: PropTypes.object, // of type Moment
    nextDay: PropTypes.func,
    previousDay: PropTypes.func,
    gotoDate: PropTypes.func,
    toDoItems: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            action: PropTypes.func,
            disabled: PropTypes.bool
        })
    ),
    overflowToDoItems: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            action: PropTypes.func
        })
    ),
    results: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            value: PropTypes.any,
            action: PropTypes.func,
            overflowActions: MobxPropTypes.arrayOrObservableArrayOf(
                PropTypes.shape({
                    label: PropTypes.string,
                    action: PropTypes.func
                })
            ),
            // depricated instead use value
            result: PropTypes.string
        })
    ),
    addNote: PropTypes.func,
    editNote: PropTypes.func,
    deleteNote: PropTypes.func,
    saveNote: PropTypes.func,
    cancelNote: PropTypes.func,
    goToTimegraph: PropTypes.func.isRequired
};

function isToday(moment) {
    return moment.isSame(Moment(), "day");
}
function isYesterday(moment) {
    return moment.isSame(Moment().subtract(1, "days"), "day");
}

@observer
class HomeScreen extends React.Component {
    noteTitle(dateMoment) {
        let formattedDate;
        if (dateMoment.isSame(new Date(), "day")) {
            formattedDate = t("Today", "vandaag");
        } else {
            formattedDate = dateMoment.format("D-M");
        }
        return t("HomeScreen.Notes.Title") + " (" + formattedDate + ")";
    }

    render() {
        const {
            view,
            dialog,
            previousDay,
            nextDay,
            gotoDate,
            toDoItems,
            overflowToDoItems,
            results,
            note,
            addNote,
            editNote,
            deleteNote,
            date,
            saveNote,
            cancelNote,
            goToTimegraph
        } = this.props;
        let content;
        switch (view) {
            case "note": {
                // AppBar is allready rendered in Note component
                return (
                    <Note
                        style={{ zIndex: 999999 }}
                        readonly={false}
                        value={note ? note.content : null}
                        onSubmit={saveNote}
                        onCancel={cancelNote}
                        showConfirmDialog={false}
                        title={this.noteTitle(date)}
                    />
                );
            }
            case "loading": {
                content = <Loading />;
                // FIXME should there not be a 'break;' statement here?!
            }
            default: {
                content = [
                    <Navigator key="Navigator" date={date} previous={previousDay} next={nextDay} gotoDate={gotoDate} />
                ];
                if (isToday(date) || isYesterday(date)) {
                    content.push(
                        <ToDoBlock key="ToDoBlock" itemList={toDoItems} overflowToDoItems={overflowToDoItems} />
                    );
                }
                content.push(<DataBlock key="DataBlock" results={results} />);
                content.push(
                    <NotesBlock key="NotesBlock" note={note ? note.content : null} add={addNote} edit={editNote} deleteNote={deleteNote} />
                );
            }
        }
        return (
            <div>
                <AppBar
                    leftButtonType="mainMenu"
                    leftButtonOptions={{ action: menuToggle.toggle }}
                    rightButtonType={"action"}
                    rightButtonOptions={{
                        icon: <AnalysisIcon />,
                        action: goToTimegraph
                    }}
                />
                <ContentContainer>{content}</ContentContainer>
                {dialog}
            </div>
        );
    }
}

HomeScreen.propTypes = propTypes;

export default HomeScreen;
