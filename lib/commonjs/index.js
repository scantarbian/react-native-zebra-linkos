"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.multiply = multiply;
exports.writeTCP = writeTCP;
var _reactNative = require("react-native");
const LINKING_ERROR = `The package 'react-native-zebra-linkos' doesn't seem to be linked. Make sure: \n\n` + _reactNative.Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo Go\n';
const ZebraLinkOS = _reactNative.NativeModules.ZebraLinkOS ? _reactNative.NativeModules.ZebraLinkOS : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }
});
function multiply(a, b) {
  return ZebraLinkOS.multiply(a, b);
}
async function writeTCP(ipAddress, zpl) {
  return await ZebraLinkOS.writeTCP(ipAddress, zpl);
}
//# sourceMappingURL=index.js.map