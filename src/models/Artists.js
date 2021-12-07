import { observable, action } from "mobx";
import regeneratorRuntime from "regenerator-runtime";

class Artists {
    @observable artists = []
    @observable songs = []
    @observable init = true;
    @observable initSongs = false;

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
            });
    }

    getSongs(artist) {
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
        this.songs = []
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