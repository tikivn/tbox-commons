import { NativeModules } from 'react-native';

const { TboxContacts: RNTboxContacts } = NativeModules;

type AddPhoneContact = {
  photoFilePath: string;
  nickName: string;
  lastName: string;
  middleName: string;
  firstName: string;
  remark: string;
  mobilePhoneNumber: string;
  homePhoneNumber: string;
  workPhoneNumber: string;
  homeFaxNumber: string;
  workFaxNumber: string;
  hostNumber: string;
  addressCountry: string;
  addressState: string;
  addressCity: string;
  addressStreet: string;
  addressPostalCode: string;
  workAddressCountry: string;
  workAddressState: string;
  workAddressCity: string;
  workAddressStreet: string;
  workAddressPostalCode: string;
  homeAddressCountry: string;
  homeAddressState: string;
  homeAddressCity: string;
  homeAddressStreet: string;
  homeAddressPostalCode: string;
  organization: string;
  title: string;
  email: string;
  url: string;
};

export class TboxContacts {
  static openContactForm(option: AddPhoneContact) {
    return RNTboxContacts.openContactForm(option);
  }

  static addToExistingContact(option: AddPhoneContact) {
    return RNTboxContacts.addToExistingContact(option);
  }

  static getAllContacts(options?: Array<string>) {
    return RNTboxContacts.getAllContacts(options || []);
  }

  static choosePhoneContact() {
    return RNTboxContacts.choosePhoneContact();
  }
}
