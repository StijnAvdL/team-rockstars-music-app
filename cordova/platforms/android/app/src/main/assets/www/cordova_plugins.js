cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "sensingkit.SensingKit",
      "file": "plugins/sensingkit/sensingkit.js",
      "pluginId": "sensingkit",
      "runs": true
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.4",
    "uploader": "1.0.6",
    "sensingkit": "1.0.11"
  };
});