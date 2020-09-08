import ReactDOM from "react-dom";
import App from "/src/components/App";

function startWeb() {
    ReactDOM.render(<App />, document.getElementById("app"));
}

if (window.cordova) {
    document.addEventListener("deviceready", () => {
        // codePush.sync();
        startWeb();
    }, false);

    document.addEventListener("resume", () => {
        // codePush.sync();
    }, false);
} else {
    startWeb();
}