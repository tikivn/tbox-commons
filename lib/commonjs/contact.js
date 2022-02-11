"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.TboxContacts = void 0;

var _reactNative = require("react-native");

const {
  TboxContacts: RNTboxContacts
} = _reactNative.NativeModules;

class TboxContacts {
  static openContactForm(option) {
    return RNTboxContacts.openContactForm(option);
  }

  static addToExistingContact(option) {
    return RNTboxContacts.addToExistingContact(option);
  }

  static getAllContacts(options) {
    return RNTboxContacts.getAllContacts(options || []);
  }

  static choosePhoneContact() {
    return RNTboxContacts.choosePhoneContact();
  }

}

exports.TboxContacts = TboxContacts;
//# sourceMappingURL=contact.js.map