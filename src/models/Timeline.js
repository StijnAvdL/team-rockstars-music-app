import moment from "moment";
import fakeData from "./fakeData";

class Timeline {
    all = [];

    constructor() {
        this._onReceive();
    }

    _onReceive() {
        this.all = fakeData;
    }

    getData(date) {
        var data = [];

        this.all.map(d => {
            if(moment(d.timestamp).isSame(moment(date), "day")) {
                data = d.value;
            }
        });
        return data;
    }
}
export default Timeline;