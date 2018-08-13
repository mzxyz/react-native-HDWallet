
import { NativeModules } from 'react-native';
import { Buffer } from 'buffer';

const { RNHDWallet } = NativeModules;

export const generateMnemonic = () => {
  return RNHDWallet.generateMnemonic();
};

export const mnemonicFromSeed = (seed) => {
  if (Buffer.isBuffer(seed)) {
    return RNHDWallet.mnemonicFromSeed(seed);
  }

  if (buffer instanceof Uint8Array) {
    return RNHDWallet.mnemonicFromSeed(Buffer.from(seed.buffer));
  }

  throw new TypeError('unexpected type, use Buffer or Uint8Array');
};

export const seedFromMnemonic = (mnemonic) => {
  return RNHDWallet.seedFromMnemonic(mnemonic);
};

export const validateMnemonic = mnemonic => {
  return RNHDWallet.validateMnemonic(mnemonic);
};
