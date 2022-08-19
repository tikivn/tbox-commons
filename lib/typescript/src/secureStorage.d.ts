export declare const ACCESSIBLE: {
    WHEN_UNLOCKED: string;
    AFTER_FIRST_UNLOCK: string;
    ALWAYS: string;
    WHEN_PASSCODE_SET_THIS_DEVICE_ONLY: string;
    WHEN_UNLOCKED_THIS_DEVICE_ONLY: string;
    AFTER_FIRST_UNLOCK_THIS_DEVICE_ONLY: string;
    ALWAYS_THIS_DEVICE_ONLY: string;
};
export declare const ACCESS_CONTROL: {
    USER_PRESENCE: string;
    BIOMETRY_ANY: string;
    BIOMETRY_CURRENT_SET: string;
    DEVICE_PASSCODE: string;
    APPLICATION_PASSWORD: string;
    BIOMETRY_ANY_OR_DEVICE_PASSCODE: string;
    BIOMETRY_CURRENT_SET_OR_DEVICE_PASSCODE: string;
};
export declare const AUTHENTICATION_TYPE: {
    DEVICE_PASSCODE_OR_BIOMETRICS: string;
    BIOMETRICS: string;
};
export declare const BIOMETRY_TYPE: {
    TOUCH_ID: string;
    FACE_ID: string;
    FINGERPRINT: string;
};
export declare const TboxSecureStorage: {
    ACCESSIBLE: {
        WHEN_UNLOCKED: string;
        AFTER_FIRST_UNLOCK: string;
        ALWAYS: string;
        WHEN_PASSCODE_SET_THIS_DEVICE_ONLY: string;
        WHEN_UNLOCKED_THIS_DEVICE_ONLY: string;
        AFTER_FIRST_UNLOCK_THIS_DEVICE_ONLY: string;
        ALWAYS_THIS_DEVICE_ONLY: string;
    };
    ACCESS_CONTROL: {
        USER_PRESENCE: string;
        BIOMETRY_ANY: string;
        BIOMETRY_CURRENT_SET: string;
        DEVICE_PASSCODE: string;
        APPLICATION_PASSWORD: string;
        BIOMETRY_ANY_OR_DEVICE_PASSCODE: string;
        BIOMETRY_CURRENT_SET_OR_DEVICE_PASSCODE: string;
    };
    AUTHENTICATION_TYPE: {
        DEVICE_PASSCODE_OR_BIOMETRICS: string;
        BIOMETRICS: string;
    };
    BIOMETRY_TYPE: {
        TOUCH_ID: string;
        FACE_ID: string;
        FINGERPRINT: string;
    };
    getItem(key: string, options: any): any;
    setItem(key: string, value: string, options: any): any;
    removeItem(key: string, options: any): any;
    getAllKeys(options: any): any;
    getSupportedBiometryType(): any;
    canCheckAuthentication(options: any): any;
};
