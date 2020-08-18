import { observable, action, autorun, computed } from "mobx";
import PropTypes from "prop-types";
import moment from "moment";
import _ from "lodash";

import { isVisualResponse } from "/src/models/Experiment/TaskBattery";
import DateTracker from "/src/components/HomeScreen/DateTracker";
import t from "/src/services/translate";

const propTypes = {
    go: PropTypes.func.isRequired,
    userModel: PropTypes.object.isRequired,
    study: PropTypes.object.isRequired
};

var globalState = globalState || {
    rememberDate: false,
    date: new DateTracker()
};

class HomeScreenModel {

    constructor(props) {
        this.props = props;
        this.date = globalState.date;
        this.timelineModel = props.timelineModel;

        autorun(() => {
            if (!this.timelineModel.init) {
                this.timelineModel.fetchData(this.date.toDate);
            }
        });
        this.filterInquiry = this.filterInquiry.bind(this);
    }

    // as long as the model is still fetching data (user and inquiries)
    // initializing will be true;
    @computed
    get initializing() {
        return this.props.userModel.initializing || this.props.study.initializing;
    }

    @computed
    get scheduledInquiries() {
        if (!this.initializing) {
            return this.props.study.scheduledInquiries(this.date.toDate, this.props.userModel.enrollDate || new Date());
        }
        return [];
    }

    @computed
    get uniqueNotScheduledInquiries() {
        if (!this.initializing) {
            return this.props.study.uniqueNotScheduledInquiries(this.date.toDate, this.props.userModel.enrollDate || new Date());
        }
        return [];
    }

    // is set when a user clicks to result screen from a date in the past
    // so to be able to return to that date when 'back' is clicked
    get rememberDate() {
        return globalState.rememberDate;
    }
    set rememberDate(onOff) {
        globalState.rememberDate = onOff;
    }

    @computed
    get results() {
        return this.timelineModel.results;
    }

    @computed
    get toDoItems() {
        if (!(this.date.isToday || this.date.isYesterday)) {
            return [];
        } else {
            return [].concat(this.studyToDoItems, this.configToDoItems, this.profileToDoItems);
        }
    }
    @computed
    get overflowToDoItems() {
        if (!(this.date.isToday || this.date.isYesterday)) {
            return [];
        } else {
            return this.overflowItems;
        }
    }

    @computed
    get privacyStatementAccepted() {
        return this.props.userModel.privacyStatement;
    }

    @computed
    get noDevices() {
        return this.props.userModel.fitbit ? false : true;
    }

    @computed
    get studyToDoItems() {
        if (!this.date.isToday && !this.date.isYesterday) {
            return [];
        }
        const inquiries = this.scheduledInquiries.filter(this.filterInquiry).map(inquiry => {
            inquiry.action = this.inquiryAction(inquiry);
            inquiry.disabled = this.date.isToday && !inquiry.isAvailable(new Date());
            return inquiry;
        });
        return inquiries;
    }

    // overflowItems are the tasks (questions and experiments) that are in
    // the study, but not scheduled for today and therefore don't apear in
    // todo list directly
    @computed
    get overflowItems() {
        let filter;
        if (this.date.isToday) {
            filter = inquiry => inquiry.type !== "taskBattery" && inquiry.id !== "VAS";
        } else if (this.date.isYesterday) {
            filter = inquiry => (inquiry.type === "question" || inquiry.type === "questionContainer") && inquiry.id !== "VAS";
        } else {
            return [];
        }
        return this.uniqueNotScheduledInquiries
            .filter(inquiry => filter(inquiry))
            .map(inquiry => {
                return {
                    key: inquiry.label,
                    label: inquiry.label,
                    action: this.inquiryAction(inquiry)
                };
            });
    }

    // the todo items concerning (missing) configuration
    @computed
    get configToDoItems() {
        const { go, study } = this.props;
        if (this.date.isToday && this.noDevices && !study.ceApproved) {
            return [
                {
                    type: "wearable",
                    key: "wearable",
                    get label() {
                        return t("HomeScreen.ConnectWearable");
                    },
                    action: () => go("/devices")
                }
            ];
        } else return [];
    }

    // the todo items concerning (missing) profile fields
    @computed
    get profileToDoItems() {
        const { go } = this.props;

        let profileFieldsMissing = true;
        if (this.initializing) {
            return [];
        }
        const requiredFields = ["lastname", "birthday", "length", "weight", "subtypeDiagnose"];
        const profile = this.props.userModel.profile;
        if (profile && Object.keys(profile).length > 0) {
            profileFieldsMissing = requiredFields.some(key => {
                return !profile.hasOwnProperty(key) || ["", null, undefined].indexOf(profile[key]) > -1;
            });
        }
        if (profileFieldsMissing) {
            return [
                {
                    type: "profile",
                    key: "profile",
                    label: t("HomeScreen.FillInProfile"),
                    action: () => go("/profile")
                }
            ];
        } else {
            return [];
        }
    }


    inquiryAction(inquiry) {
        const { type, id, scheduleId } = inquiry;
        switch (type) {
            case "experiment":
                return () => this.goToExperiment(id, scheduleId);
            case "question":
            case "questionContainer":
                if (inquiry.ofYesterday && this.date.isToday) {
                    const yesterday = new Date(this.date.toDate.getTime());
                    yesterday.setDate(yesterday.getDate() - 1);
                    return () => this.goToQuestion(id, yesterday, scheduleId);
                } else {
                    return () => this.goToQuestion(id, undefined, scheduleId);
                }
            case "taskBattery":
                return () => this.goToTaskBattery(id, scheduleId);
            default:
                return null;
        }
    }

    todoItemAction(type, source) {
        return this.inquiryAction({
            type: type,
            id: source,
            ofYesterday: false
        });
    }

    // filter inquiries that are already done
    filterInquiry(inquiry) {
        if (!inquiry.ofYesterday) {
            if (inquiry.type === "taskBattery") {
                var resultExists = this.results.some(result => {
                    const answerTime = new Date(result.timestamp).getTime();
                    const startTask = new Date(inquiry.availableFrom).getTime();
                    const endTask = new Date(inquiry.expireAt).getTime();
                    return (answerTime > startTask && answerTime < endTask && result.type === "taskBattery");
                });
            } else {
                var resultExists = this.results.some(result => {
                    return (
                        result.source === inquiry.id ||
                        (isVisualResponse(result.source) && isVisualResponse(inquiry.id))
                    );
                });
            }
        } else {
            const resultYesterday = _.filter(this.timelineModel.all, (item) => {
                return moment(item.timestamp).isSame(moment().subtract(1, 'days').startOf('day'), 'd');
            });
            var resultExists = _.find(resultYesterday, { source: inquiry.id })
        }
        // add sdmt if study schedule it every 4 week and its not done the week it should
        const today = moment();
        const from_date = new Date(today.startOf('week')).getTime();
        const to_date = new Date(today.endOf('week')).getTime();

        if (!resultExists &&
            inquiry.schedule &&
            inquiry.schedule.weekInTodo &&
            inquiry.schedule.daySchedule.match(/every [0-9] weeks/g).length === 1 &&
            ((moment().format('W') - 1) % parseInt(inquiry.schedule.daySchedule.split(" ")[1])) === 0
        ) {
            this.timelineModel.all.map(item => {
                const answerTime = new Date(item.timestamp).getTime();
                if (answerTime > from_date && answerTime < to_date && item.source === inquiry.id) {
                    resultExists = true;
                }
            })

        }
        return !resultExists;
    }

    goToExperiment(experimentTypeId, scheduleId) {
        const { go } = this.props;
        const route = "/exp/" + (isVisualResponse(experimentTypeId) ? "vrt" : experimentTypeId);
        this.rememberDate = true;
        let params;
        if (scheduleId) {
            params = { scheduleId };
        }
        go(route, params);
    }

    goToTaskBattery(taskBatteryId, scheduleId) {
        const { go } = this.props;
        this.rememberDate = true;
        const route = "/exp/taskBattery";
        let params = { itemId: taskBatteryId };
        if (scheduleId) {
            params.scheduleId = scheduleId;
        }
        go(route, params);
    }

    goToQuestion(questionId, date, scheduleId) {
        const { go } = this.props;
        if (typeof date === "undefined") {
            date = this.date.toDate;
        }
        this.rememberDate = true;
        const route = "/question";
        let params = {
            itemId: questionId,
            date: date.getTime().toString()
        };
        if (scheduleId) {
            params.scheduleId = scheduleId;
        }
        go(route, params);
    }
}
HomeScreenModel.propTypes = propTypes;
export default HomeScreenModel;
