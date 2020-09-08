cordova.define("sensingkit.SensingKit", function(require, exports, module) {
(function() {
  // // TODO include as JS in the plugin
  const callbacks = [];

  const SensingKit = {};
  SensingKit.version = "1.0.11";
  SensingKit.start = opts => {
    return new Promise((resolve, reject) =>
      cordova.exec(resolve, reject, "SensingKit", "start", [opts])
    );
  };
  SensingKit.startWalkingExperiment = opts => {
    return new Promise((resolve, reject) =>
      cordova.exec(resolve, reject, "SensingKit", "startWalkingExperiment", [
        opts
      ])
    );
  };
  SensingKit.nextStage = () => {
    return new Promise((resolve, reject) =>
      cordova.exec(resolve, reject, "SensingKit", "nextStage", [])
    );
  };
  SensingKit.stop = () => {
    return new Promise((resolve, reject) =>
      cordova.exec(resolve, reject, "SensingKit", "stop", [])
    );
  };
  SensingKit.subscribe = callback => {
    callbacks.push(callback);
    return new Promise((resolve, reject) =>
      cordova.exec(
        event => {
          callbacks.forEach(function(callback) {
            callback(event);
          });
          resolve(event);
        },
        reject,
        "SensingKit",
        "subscribe",
        []
      )
    );
  };

  SensingKit.unsubscribe = callback => {
    var index = callbacks.indexOf(callback);
    if (index >= 0) {
      callbacks.splice(index, 1);
    }
  };
  SensingKit._callbacks = callbacks; // for testing and debugging
  window.SensingKit = SensingKit;
})();

});
