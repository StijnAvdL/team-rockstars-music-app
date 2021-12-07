import { observable, action } from "mobx";
import regeneratorRuntime from "regenerator-runtime";

class Artists {
    @observable artists = []
    @observable songs = []
    @observable init = true;
    @observable initSongs = false;
    @observable error = null;

    constructor() {
        this._onReceive();
    }

    async _onReceive() {
        this.init = true;
        await fetch("http://localhost:3000/artists")
            .then(res => res.json())
            .then(data => {
                this.artists = data
                this.init = false;
            })
            .catch(error => {
                console.log("error!", error)
                this.init = false;
                this.error = "Server is not available right now";
            });
    }

    getSongs(artist) {
        this.error = null;
        this.initSongs = true;
        this.songs = []
        fetch(`http://localhost:3000/songs?artist=${artist}`)
            .then(res => res.json())
            .then(data => {
                this.songs = data
                this.initSongs = false;
            });
    }

    searchArtists(value) {
        this.error = null;
        this.songs = [];
        this.init = true;
        fetch(`http://localhost:3000/artists?name_like=${value}`)
            .then(res => res.json())
            .then(data => {
                this.artists = data
                this.init = false;
            });
    }

}
export default Artists