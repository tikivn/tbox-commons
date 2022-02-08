import { NativeModules } from 'react-native';

type AddToHomeParams = {
  url: string;
  appName: string;
  image?: string;
  appId: string;
  icon?: string;
};

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

type TboxCommonsType = {
  measure(options: Array<any>): Promise<Array<number>>;
  addToHome(option: AddToHomeParams): Promise<Array<number>>;
};

type TboxContactsType = {
  openContactForm(options: AddPhoneContact): Promise<Array<number>>;
  addToExistingContact(option: AddPhoneContact): Promise<Array<number>>;
  getAllScope(options?: Array<any>): Promise<Array<number>>;
};

const { TboxCommons } = NativeModules;
const TboxContacts: TboxContactsType = NativeModules.TboxContacts;

export default TboxCommons as TboxCommonsType;
export { TboxContacts };
