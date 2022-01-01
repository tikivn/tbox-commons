import { NativeModules } from 'react-native';
const {
  TboxCommons,
  TboxContacts: TboxContactsNative
} = NativeModules;
export default TboxCommons;
export const TboxContacts = TboxContactsNative;
//# sourceMappingURL=index.js.map