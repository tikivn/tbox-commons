import { NativeModules } from 'react-native';

type AddToHomeParams = {
  url: string;
  appName: string;
  image?: string;
  appId: string;
  icon?: string;
};

type TboxCommonsType = {
  measure(options: Array<any>): Promise<Array<number>>;
  addToHome(option: AddToHomeParams): Promise<Array<number>>;
};

const { TboxCommons } = NativeModules;

export default TboxCommons as TboxCommonsType;
export { TboxContacts } from './contact';
export { TboxSecureStorage } from './secureStorage';
