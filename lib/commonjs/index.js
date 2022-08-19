"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
Object.defineProperty(exports, "TboxContacts", {
  enumerable: true,
  get: function () {
    return _contact.TboxContacts;
  }
});
Object.defineProperty(exports, "TboxSecureStorage", {
  enumerable: true,
  get: function () {
    return _secureStorage.TboxSecureStorage;
  }
});
exports.default = void 0;

var _reactNative = require("react-native");

var _contact = require("./contact");

var _secureStorage = require("./secureStorage");

const {
  TboxCommons
} = _reactNative.NativeModules;
var _default = TboxCommons;
exports.default = _default;
//# sourceMappingURL=index.js.map