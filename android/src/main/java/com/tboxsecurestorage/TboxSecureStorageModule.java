package com.tboxsecurestorage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.x500.X500Principal;

public class TboxSecureStorageModule extends ReactContextBaseJavaModule {

  public static final String SECURE_STORAGE_MODULE = "TboxSecureStorageModule";
  public static final String DEFAULT_SERVICE = "RN_SECURE_STORAGE_DEFAULT_ALIAS";
  // This must have 'AndroidKeyStore' as value. Unfortunately there is no predefined constant.
  private static final String ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore";

  // This is the default transformation used throughout this sample project.
  private static final String AES_DEFAULT_TRANSFORMATION =
    KeyProperties.KEY_ALGORITHM_AES + "/" +
      KeyProperties.BLOCK_MODE_CBC + "/" +
      KeyProperties.ENCRYPTION_PADDING_PKCS7;

  private static final String AES_GCM = "AES/GCM/NoPadding";
  private static final String RSA_ECB = "RSA/ECB/PKCS1Padding";
  private static final String DELIMITER = "]";
  private static final byte[] FIXED_IV = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1};
  private static final String KEY_ALIAS_AES = "MyAesKeyAlias";

  private FingerprintManager mFingerprintManager;
  private KeyStore mKeyStore;
  private CancellationSignal mCancellationSignal;

  // Keep it true by default to maintain backwards compatibility with existing users.
  private boolean invalidateEnrollment = true;

  public TboxSecureStorageModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return SECURE_STORAGE_MODULE;
  }

  /**
   * Checks whether the device supports Biometric authentication and if the user has
   * enrolled at least one credential.
   *
   * @return true if the user has a biometric capable device and has enrolled
   * one or more credentials
   */
  private boolean hasSetupBiometricCredential() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ReactApplicationContext reactApplicationContext = getReactApplicationContext();
        BiometricManager biometricManager = BiometricManager.from(reactApplicationContext);
        int canAuthenticate = biometricManager.canAuthenticate();

        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  @ReactMethod
  public void setInvalidatedByBiometricEnrollment(final boolean invalidatedByBiometricEnrollment, final Promise pm) {
    this.invalidateEnrollment = invalidatedByBiometricEnrollment;
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        prepareKey();
      }
    } catch (Exception e) {
      pm.reject(e);
    }
  }

  @ReactMethod
  public void getSupportedBiometryType(final Promise promise) {
    promise.resolve(hasSetupBiometricCredential());
  }

  @ReactMethod
  public void getItem(String key, ReadableMap options, Promise pm) {

    String name = sharedPreferences(options);
    String value = prefs(name).getString(key, null);
    String service = getDefaultServiceIfEmpty(options);

    try {
      initKeyStore(service);

      if (value != null && options.hasKey("touchID") && options.getBoolean("touchID")) {
        decryptWithAes(value, options.toHashMap(), pm, null);
      } else if (value != null) {
        pm.resolve(decrypt(value, service));
      } else {
        pm.resolve(value);
      }
    } catch (Exception e) {
      pm.reject(e);
    }

  }

  @ReactMethod
  public void hasItem(String key, ReadableMap options, Promise pm) {
    String name = sharedPreferences(options);

    String value = prefs(name).getString(key, null);

    pm.resolve(value != null ? true : false);
  }

  @ReactMethod
  public void setItem(String key, String value, ReadableMap options, Promise pm) {
    String name = sharedPreferences(options);
    String service = getDefaultServiceIfEmpty(options);

    try {
      initKeyStore(service);
      if (options.hasKey("touchID") && options.getBoolean("touchID")) {
        putExtraWithAES(key, value, prefs(name), options.toHashMap(), pm, null);
      } else {
        putExtra(key, encrypt(value, service), prefs(name));
        pm.resolve(value);
      }
    } catch (Exception e) {
      pm.reject(e);
    }
  }


  @ReactMethod
  public void deleteItem(String key, ReadableMap options, Promise pm) {

    String name = sharedPreferences(options);

    SharedPreferences.Editor editor = prefs(name).edit();

    boolean wasRemoved = editor.remove(key).commit();
    if (!wasRemoved) {
      pm.reject(new Exception("Could not remove " + key + " from Shared Preferences"));
    } else {
      pm.resolve(null);
    }
  }


  @ReactMethod
  public void getAllKeys(ReadableMap options, Promise pm) {

    String name = sharedPreferences(options);
//    String service = getDefaultServiceIfEmpty(options);

    Map<String, ?> allEntries = prefs(name).getAll();
    WritableArray resultData = new WritableNativeArray();

    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
      resultData.pushString(entry.getKey());
    }
    pm.resolve(resultData);
  }

  @ReactMethod
  public void cancelFingerprintAuth() {
    if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
      mCancellationSignal.cancel();
    }
  }

  private SharedPreferences prefs(String name) {
    return getReactApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
  }

  @NonNull
  private String sharedPreferences(ReadableMap options) {
    String name = options.hasKey("sharedPreferencesName") ? options.getString("sharedPreferencesName") : "shared_preferences";
    if (name == null) {
      name = "shared_preferences";
    }
    return name;
  }


  private void putExtra(String key, String value, SharedPreferences mSharedPreferences) throws Exception {
    SharedPreferences.Editor editor = mSharedPreferences.edit();
    boolean wasWritten = editor.putString(key, value).commit();
    if (!wasWritten) {
      throw new Exception("Could not write " + key + " to Shared Preferences");
    }
  }

  /**
   * Generates a new RSA key and stores it under the { @code KEY_ALIAS } in the
   * Android Keystore.
   */
  private void initKeyStore(String service) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
      Exception cause = new RuntimeException("Keystore is not supported!");
      throw new RuntimeException("Android version is too low", cause);
    }
    try {
      mKeyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER);
      mKeyStore.load(null);

      if (!mKeyStore.containsAlias(service)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER);
          keyGenerator.init(
            new KeyGenParameterSpec.Builder(service,
              KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
              .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
              .setRandomizedEncryptionRequired(false)
              .build());
          keyGenerator.generateKey();
        } else {
          Calendar notBefore = Calendar.getInstance();
          Calendar notAfter = Calendar.getInstance();
          notAfter.add(Calendar.YEAR, 10);
          KeyPairGeneratorSpec spec = null;
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            spec = new KeyPairGeneratorSpec.Builder(getReactApplicationContext())
              .setAlias(service)
              .setSubject(new X500Principal("CN=" + service))
              .setSerialNumber(BigInteger.valueOf(1337))
              .setStartDate(notBefore.getTime())
              .setEndDate(notAfter.getTime())
              .build();
            KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEYSTORE_PROVIDER);
            kpGenerator.initialize(spec);
            kpGenerator.generateKeyPair();
            return;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showDialog(final HashMap options, final BiometricPrompt.CryptoObject cryptoObject, final BiometricPrompt.AuthenticationCallback callback) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

      UiThreadUtil.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            try {
              Activity activity = getCurrentActivity();
              if (activity == null) {
                callback.onAuthenticationError(BiometricConstants.ERROR_CANCELED,
                  options.containsKey("cancelled") ? options.get("cancelled").toString() : "Authentication was cancelled");
                return;
              }

              FragmentActivity fragmentActivity = (FragmentActivity) getCurrentActivity();
              Executor executor = Executors.newSingleThreadExecutor();
              BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, executor, callback);

              BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setDeviceCredentialAllowed(false)
                .setNegativeButtonText(options.containsKey("cancel") ? options.get("cancel").toString() : "Cancel")
                .setDescription(options.containsKey("description") ? options.get("description").toString() : null)
                .setTitle(options.containsKey("authenticationPrompt") ? options.get("authenticationPrompt").toString() : "Unlock with your fingerprint")
                .build();
              biometricPrompt.authenticate(promptInfo, cryptoObject);
            } catch (Exception e) {
              throw e;
            }
          }
        }
      );
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void prepareKey() throws Exception {

    KeyGenerator keyGenerator = KeyGenerator.getInstance(
      KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER);

    KeyGenParameterSpec.Builder builder = null;
    builder = new KeyGenParameterSpec.Builder(
      KEY_ALIAS_AES,
      KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);

    builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
      .setKeySize(256)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
      // forces user authentication with fingerprint
      .setUserAuthenticationRequired(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      try {
        builder.setInvalidatedByBiometricEnrollment(invalidateEnrollment);
      } catch (Exception e) {
        Log.d("RNSensitiveInfo", "Error setting setInvalidatedByBiometricEnrollment: " + e.getMessage());
      }
    }

    keyGenerator.init(builder.build());
    keyGenerator.generateKey();
  }

  private void putExtraWithAES(final String key, final String value, final SharedPreferences mSharedPreferences, final HashMap options, final Promise pm, Cipher cipher) {

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && hasSetupBiometricCredential()) {
      try {
        if (cipher == null) {
          SecretKey secretKey = (SecretKey) mKeyStore.getKey(KEY_ALIAS_AES, null);
          cipher = Cipher.getInstance(AES_DEFAULT_TRANSFORMATION);
          cipher.init(Cipher.ENCRYPT_MODE, secretKey);

          // Retrieve information about the SecretKey from the KeyStore.
          SecretKeyFactory factory = SecretKeyFactory.getInstance(
            secretKey.getAlgorithm(), ANDROID_KEYSTORE_PROVIDER);
          KeyInfo info = (KeyInfo) factory.getKeySpec(secretKey, KeyInfo.class);

          if (info.isUserAuthenticationRequired() &&
            info.getUserAuthenticationValidityDurationSeconds() <= 0) {

            class PutExtraWithAESCallback extends BiometricPrompt.AuthenticationCallback {
              @Override
              public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                  putExtraWithAES(key, value, mSharedPreferences, options, pm, result.getCryptoObject().getCipher());
                }
              }

              @Override
              public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                pm.reject(String.valueOf(errorCode), errString.toString());
              }

              @Override
              public void onAuthenticationFailed() {
                getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                  .emit(AppConstants.E_AUTHENTICATION_NOT_RECOGNIZED, "Authentication not recognized.");
              }
            }

            showDialog(options, new BiometricPrompt.CryptoObject(cipher), new PutExtraWithAESCallback());

          }
          return;
        }

        byte[] encryptedBytes = cipher.doFinal(value.getBytes());

        // Encode the initialization vector (IV) and encryptedBytes to Base64.
        String base64IV = Base64.encodeToString(cipher.getIV(), Base64.DEFAULT);
        String base64Cipher = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);

        String result = base64IV + DELIMITER + base64Cipher;

        try {
          putExtra(key, result, mSharedPreferences);
          pm.resolve(value);
        } catch (Exception e) {
          pm.reject(e);
        }

      } catch (InvalidKeyException | UnrecoverableKeyException e) {
        try {
          mKeyStore.deleteEntry(KEY_ALIAS_AES);
          prepareKey();
        } catch (Exception keyResetError) {
          pm.reject(keyResetError);
        }
        pm.reject(e);
      } catch (IllegalBlockSizeException e) {
        if (e.getCause() != null && e.getCause().getMessage().contains("Key user not authenticated")) {
          try {
            mKeyStore.deleteEntry(KEY_ALIAS_AES);
            prepareKey();
            pm.reject(AppConstants.KM_ERROR_KEY_USER_NOT_AUTHENTICATED, e.getCause().getMessage());
          } catch (Exception keyResetError) {
            pm.reject(keyResetError);
          }
        } else {
          pm.reject(e);
        }
      } catch (SecurityException e) {
        pm.reject(e);
      } catch (Exception e) {
        pm.reject(e);
      }
    } else {
      pm.reject(AppConstants.E_BIOMETRIC_NOT_SUPPORTED, "Biometrics not supported");
    }
  }

  private void decryptWithAes(final String encrypted, final HashMap options, final Promise pm, Cipher cipher) {

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
      && hasSetupBiometricCredential()) {

      String[] inputs = encrypted.split(DELIMITER);
      if (inputs.length < 2) {
        pm.reject("DecryptionFailed", "DecryptionFailed");
      }

      try {
        byte[] iv = Base64.decode(inputs[0], Base64.DEFAULT);
        byte[] cipherBytes = Base64.decode(inputs[1], Base64.DEFAULT);

        if (cipher == null) {
          SecretKey secretKey = (SecretKey) mKeyStore.getKey(KEY_ALIAS_AES, null);
          cipher = Cipher.getInstance(AES_DEFAULT_TRANSFORMATION);
          cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

          SecretKeyFactory factory = SecretKeyFactory.getInstance(
            secretKey.getAlgorithm(), ANDROID_KEYSTORE_PROVIDER);
          KeyInfo info = (KeyInfo) factory.getKeySpec(secretKey, KeyInfo.class);

          if (info.isUserAuthenticationRequired() &&
            info.getUserAuthenticationValidityDurationSeconds() <= 0) {

            class DecryptWithAesCallback extends BiometricPrompt.AuthenticationCallback {
              @Override
              public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                  decryptWithAes(encrypted, options, pm, result.getCryptoObject().getCipher());
                }
              }

              @Override
              public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                pm.reject(String.valueOf(errorCode), errString.toString());
              }

              @Override
              public void onAuthenticationFailed() {
                getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                  .emit(AppConstants.E_AUTHENTICATION_NOT_RECOGNIZED, "Authentication not recognized.");
              }
            }

            showDialog(options, new BiometricPrompt.CryptoObject(cipher), new DecryptWithAesCallback());
          }
          return;
        }
        byte[] decryptedBytes = cipher.doFinal(cipherBytes);
        pm.resolve(new String(decryptedBytes));
      } catch (InvalidKeyException | UnrecoverableKeyException e) {
        try {
          mKeyStore.deleteEntry(KEY_ALIAS_AES);
          prepareKey();
        } catch (Exception keyResetError) {
          pm.reject(keyResetError);
        }
        pm.reject(e);
      } catch (IllegalBlockSizeException e) {
        if (e.getCause() != null && e.getCause().getMessage().contains("Key user not authenticated")) {
          try {
            mKeyStore.deleteEntry(KEY_ALIAS_AES);
            prepareKey();
            pm.reject(AppConstants.KM_ERROR_KEY_USER_NOT_AUTHENTICATED, e.getCause().getMessage());
          } catch (Exception keyResetError) {
            pm.reject(keyResetError);
          }
        } else {
          pm.reject(e);
        }
      } catch (BadPaddingException e) {
        Log.d("RNSensitiveInfo", "Biometric key invalid");
        pm.reject(AppConstants.E_BIOMETRICS_INVALIDATED, e.getCause().getMessage());
      } catch (SecurityException e) {
        pm.reject(e);
      } catch (Exception e) {
        pm.reject(e);
      }
    } else {
      pm.reject(AppConstants.E_BIOMETRIC_NOT_SUPPORTED, "Biometrics not supported");
    }
  }

  public String encrypt(String input, String service) throws Exception {
    byte[] bytes = input.getBytes();
    Cipher c;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Key secretKey = ((KeyStore.SecretKeyEntry) mKeyStore.getEntry(service, null)).getSecretKey();
      c = Cipher.getInstance(AES_GCM);
      c.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, FIXED_IV));
    } else {
      PublicKey publicKey = ((KeyStore.PrivateKeyEntry) mKeyStore.getEntry(service, null)).getCertificate().getPublicKey();
      c = Cipher.getInstance(RSA_ECB);
      c.init(Cipher.ENCRYPT_MODE, publicKey);
    }

    int cipherTextSize = 0;
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    DataOutputStream dataStream = new DataOutputStream(byteStream);
    ByteArrayInputStream plaintextStream = new ByteArrayInputStream(bytes);
    final int chunkSize = 4 * 1024;
    byte[] buffer = new byte[chunkSize];
    while (plaintextStream.available() > chunkSize) {
      int readBytes = plaintextStream.read(buffer);
      byte[] ciphertextChunk = c.update(buffer, 0, readBytes);
      cipherTextSize += ciphertextChunk.length;
      dataStream.write(ciphertextChunk);
    }
    int readBytes = plaintextStream.read(buffer);
    byte[] ciphertextChunk = c.doFinal(buffer, 0, readBytes);
    cipherTextSize += ciphertextChunk.length;
    dataStream.write(ciphertextChunk);

    String encryptedBase64Encoded = Base64.encodeToString(byteStream.toByteArray(), Base64.NO_WRAP);
    return encryptedBase64Encoded;
  }


  public String decrypt(String encrypted, String service) throws Exception {
    if (encrypted == null) {
      Exception cause = new RuntimeException("Invalid argument at decrypt function");
      throw new RuntimeException("encrypted argument can't be null", cause);
    }

    Cipher c;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Key secretKey = ((KeyStore.SecretKeyEntry) mKeyStore.getEntry(service, null)).getSecretKey();
      c = Cipher.getInstance(AES_GCM);
      c.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, FIXED_IV));
    } else {
      PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) mKeyStore.getEntry(service, null)).getPrivateKey();
      c = Cipher.getInstance(RSA_ECB);
      c.init(Cipher.DECRYPT_MODE, privateKey);
    }

    byte[] bytes = Base64.decode(encrypted, Base64.NO_WRAP);
    ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
    DataInputStream dataStream = new DataInputStream(byteStream);

    CipherInputStream cipherStream = new CipherInputStream(byteStream, c);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    byte[] buffer = new byte[1024];
    int len;
    while ((len = cipherStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, len);
    }

    byte[] decodedBytes = outputStream.toByteArray();
    return new String(decodedBytes);
  }

  private String getDefaultServiceIfEmpty(ReadableMap options) {
    String service = options.hasKey("service") && options.getString("service") != null ? options.getString("service") : "";
    return service.isEmpty() ? DEFAULT_SERVICE : service;
  }

}
