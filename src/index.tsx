import { NativeModules } from 'react-native';

type TboxCommonsType = {
  measure(options:Array<any>): Promise<Array<number>>;
};

const { TboxCommons } = NativeModules;

export default TboxCommons as TboxCommonsType;
