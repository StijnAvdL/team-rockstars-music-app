import { observable, action } from "mobx";
import regeneratorRuntime from "regenerator-runtime"; // TODO: receive error without this import

class Playlists {
    @observable playlists = []
    @observable songs = []
    @observable init = true;
    @observable initSongs = false;
    @observable error = null;

    constructor() {
        this._onReceive();
    }

    async _onReceive() {
        this.init = true;
        await fetch("http://localhost:3000/playlists")
            .then(res => res.json())
            .then(data => {
                this.playlists = data
                this.init = false;
            })
            .catch(error => {
                console.log("error!", error)
                this.init = false;
                this.error = "Server is not available right now";
            });
    }

    add(name) {
        this.init = true;
        const playlist = { name: name, songs: [] }
        fetch("http://localhost:3000/playlists", {
            method: "POST",
            body: JSON.stringify(playlist),
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(res => res.json())
            .then(data => {
                this.playlists.push(data)
                this.init = false;
            });

    }
}
export default Playlists