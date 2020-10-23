cordova.define("tug.Tug", function(require, exports, module) {
(function () {
    const Tug = {};
    Tug.version = "0.1.0";
    Tug.start = () => {
        return new Promise((resolve, reject) =>
            cordova.exec(resolve, reject, "TugPlugin", "start", ["exp_iddd"])
        );
    };
    Tug.stop = () => {
        return new Promise((resolve, reject) =>
            cordova.exec(resolve, reject, "TugPlugin", "stop", [])
        );
    };
    window.Tug = Tug;
})();

});
