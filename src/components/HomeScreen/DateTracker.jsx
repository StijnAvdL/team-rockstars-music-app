import { observable, action, computed } from "mobx";

import Moment from "moment";

class DateTracker {
    @observable day = 0;
    @observable month = 0;
    @observable year = 0;

    constructor() {
        this.setDate(Moment());
    }

    @action.bound
    setDate(newDate) {
        let dateMoment;
        if (!Moment.isMoment(newDate)) {
            dateMoment = Moment(newDate);
        } else {
            dateMoment = newDate;
        }
        this.day = dateMoment.date();
        this.month = dateMoment.month();
        this.year = dateMoment.year();
    }

    @computed
    get moment() {
        return Moment({ day: this.day, month: this.month, year: this.year });
    }

    @computed
    get toDate() {
        return new Date(this.year, this.month, this.day);
    }

    @computed
    get reachedToday() {
        return !this.moment.isBefore(Moment(), "day");
    }

    @computed
    get isToday() {
        return this.moment.isSame(Moment(), "day");
    }

    @computed
    get isYesterday() {
        return this.moment.isSame(Moment().subtract(1, "days"), "day");
    }

    @action.bound
    previous() {
        this.setDate(this.moment.subtract(1, "days"));
    }
    @action.bound
    next() {
        this.setDate(this.moment.add(1, "days"));
    }
}

export default DateTracker;
