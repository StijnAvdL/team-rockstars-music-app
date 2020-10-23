import moment from "moment";
import { observable } from "mobx";

class Timeline {
    all = [];
    @observable init = true;

    constructor() {
        this._onReceive();
    }

    async _onReceive() {
        await fetch("https://alpha.orikami-api.nl/v1/janssen-demo/get")
            .then(res => res.json())
            .then(data => {
                data.map(d => {
                    var result = {
                        timestamp: d.timestamp,
                        type: d.experiment
                    };
                    if (result.type === "sdmt") {
                        result.value = `${d.value.correct}/${d.value.nb}`;
                    } else if(result.type === "tug") {
                        result.value = `${Math.round(d.value.duration * 10) / 10} sec`;
                    }
                    this.all.push(result);
                });
            });
        this.init = false;
    }

    getData(date) {
        var data = [];

        this.all.map(d => {
            if (moment(d.timestamp).isSame(moment(date), "day") && (d.type === "sdmt" || d.type === "tug")) {
                data.push(d)
            }
        });
        return data;
    }
}
export default Timeline;