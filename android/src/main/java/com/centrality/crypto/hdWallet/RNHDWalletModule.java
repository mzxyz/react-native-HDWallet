
package com.centrality.crypto.hdWallet;

import com.centrality.cryptp.bip39.Bip39;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

public class RNHDWalletModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNHDWalletModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNHDWallet";
  }

  @ReactMethod
  public void generateMnemonic(int entropyLength, Promise promise) {
    try {
      String[] wordList = Bip39.createRandomWords(entropyLength);
      String mnemonicPhrase = String.join(" ", wordList);

      promise.resolve(mnemonicPhrase);
    } catch (Exception e) {
      promise.reject("Generate random mnemonic phrase failed", e);
    }
  }

  @ReactMethod
  public void seedFromMnemonic(String mnemonic, Promise promise) {
    try {
      String[] wordList = mnemonic.split(" ");
      Bip39.MasterSeed masterSeed = Bip39.generateSeedFromWordList(wordList);
      byte[] seedBytes = masterSeed.getBip32Seed();

      promise.resolve(byteArrayToWritable(seedBytes));
    } catch (Exception e) {
      promise.reject("Generate seed from mnemonic phrase failed", e);
    }
  }

  @ReactMethod
  public void validateMnemonic(String mnemonic,, Promise promise) {
    try {
      String[] wordList = mnemonic.split(" ");
      boolean wordListValid = Bip39.isValidWordList(wordList);
      
      promise.resolve(wordListValid);
    } catch (Exception e) {
      promise.reject("validate mnemonic phrase failed", e);
    }
  }


  private WritableArray byteArrayToWritable(byte [] arr) {
    WritableArray arrWritable = Arguments.createArray();
    for (int i = 0; i < arr.length; i++) {
      int unsignedByte = arr[i] & 0xFF;
      arrWritable.pushInt(unsignedByte);
    }

    return arrWritable;
  }
}