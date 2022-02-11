import { NativeModules } from 'react-native';
const {
  TboxContacts: RNTboxContacts
} = NativeModules;
export class TboxContacts {
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
//# sourceMappingURL=contact.js.map