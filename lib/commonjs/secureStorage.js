"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.TboxSecureStorage = exports.BIOMETRY_TYPE = exports.AUTHENTICATION_TYPE = exports.ACCESS_CONTROL = exports.ACCESSIBLE = void 0;

var _reactNative = require("react-native");

const {
  TboxSecureStorageModule: RNSecureStorage
} = _reactNative.NativeModules;
const ACCESSIBLE = {
  WHEN_UNLOCKED: 'AccessibleWhenUnlocked',
  AFTER_FIRST_UNLOCK: 'AccessibleAfterFirstUnlock',
  ALWAYS: 'AccessibleAlways',
  WHEN_PASSCODE_SET_THIS_DEVICE_ONLY: 'AccessibleWhenPasscodeSetThisDeviceOnly',
  WHEN_UNLOCKED_THIS_DEVICE_ONLY: 'AccessibleWhenUnlockedThisDeviceOnly',
  AFTER_FIRST_UNLOCK_THIS_DEVICE_ONLY: 'AccessibleAfterFirstUnlockThisDeviceOnly',
  ALWAYS_THIS_DEVICE_ONLY: 'AccessibleAlwaysThisDeviceOnly'
};
exports.ACCESSIBLE = ACCESSIBLE;
const ACCESS_CONTROL = {
  USER_PRESENCE: 'UserPresence',
  BIOMETRY_ANY: 'BiometryAny',
  BIOMETRY_CURRENT_SET: 'BiometryCurrentSet',
  DEVICE_PASSCODE: 'DevicePasscode',
  APPLICATION_PASSWORD: 'ApplicationPassword',
  BIOMETRY_ANY_OR_DEVICE_PASSCODE: 'BiometryAnyOrDevicePasscode',
  BIOMETRY_CURRENT_SET_OR_DEVICE_PASSCODE: 'BiometryCurrentSetOrDevicePasscode'
};
exports.ACCESS_CONTROL = ACCESS_CONTROL;
const AUTHENTICATION_TYPE = {
  DEVICE_PASSCODE_OR_BIOMETRICS: 'AuthenticationWithBiometricsDevicePasscode',
  BIOMETRICS: 'AuthenticationWithBiometrics'
};
exports.AUTHENTICATION_TYPE = AUTHENTICATION_TYPE;
const BIOMETRY_TYPE = {
  TOUCH_ID: 'TouchID',
  FACE_ID: 'FaceID',
  FINGERPRINT: 'Fingerprint'
};
exports.BIOMETRY_TYPE = BIOMETRY_TYPE;
const isAndroid = _reactNative.Platform.OS === 'android';
const defaultOptions = {
  accessControl: null,
  accessible: ACCESSIBLE.WHEN_UNLOCKED,
  accessGroup: null,
  authenticationPrompt: 'Authenticate to retrieve secret data',
  service: null,
  authenticateType: AUTHENTICATION_TYPE.DEVICE_PASSCODE_OR_BIOMETRICS
};
const TboxSecureStorage = {
  ACCESSIBLE,
  ACCESS_CONTROL,
  AUTHENTICATION_TYPE,
  BIOMETRY_TYPE,

  getItem(key, options) {
    const finalOptions = { ...defaultOptions,
      ...options
    };
    return RNSecureStorage.getItem(key, finalOptions);
  },

  setItem(key, value, options) {
    const finalOptions = { ...defaultOptions,
      ...options
    };
    return RNSecureStorage.setItem(key, value, finalOptions);
  },

  removeItem(key, options) {
    const finalOptions = { ...defaultOptions,
      ...options
    };

    if (isAndroid) {
      return RNSecureStorage.removeItem(key, finalOptions.service);
    }

    return RNSecureStorage.removeItem(key, finalOptions);
  },

  getAllKeys(options) {
    const finalOptions = { ...defaultOptions,
      ...options
    };
    return RNSecureStorage.getAllKeys(finalOptions);
  },

  getSupportedBiometryType() {
    return RNSecureStorage.getSupportedBiometryType();
  },

  canCheckAuthentication(options) {
    if (isAndroid) {
      return RNSecureStorage.getSupportedBiometryType() !== null;
    }

    return RNSecureStorage.canCheckAuthentication(options);
  }

};
exports.TboxSecureStorage = TboxSecureStorage;
//# sourceMappingURL=secureStorage.js.map