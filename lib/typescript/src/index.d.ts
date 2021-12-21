declare type AddToHomeParams = {
    url: string;
    appName: string;
    image?: string;
    appId: string;
    icon?: string;
};
declare type TboxCommonsType = {
    measure(options: Array<any>): Promise<Array<number>>;
    addToHome(option: AddToHomeParams): Promise<Array<number>>;
};
declare const _default: TboxCommonsType;
export default _default;
