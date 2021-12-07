import { observable, action } from "mobx";
import regeneratorRuntime from "regenerator-runtime";

class Artists {
    artists = []
    @observable init = true;

    constructor() {
        this._onReceive();
    }

    async _onReceive() {
        console.log("her!")
        await fetch("http://localhost:3000/artists")
            .then(res => res.json())
            .then(data => {
                this.artists = data
                this.init = false;
                console.log("data", data)
            });
    }

}
export default Artists