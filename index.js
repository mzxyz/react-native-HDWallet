
import { NativeModules } from 'react-native';
import { Buffer } from 'buffer';

const { RNHDWallet } = NativeModules;

/**
 * Generates mnemonic phrase. (12 words by default)
 * 
 * @param {Number} entropyLength 128 bits entropy by default
 * @return {PromiseLike<Object>}
 */
export const generateMnemonic = (entropyLength = 128) => {
  const bytesLen = entropyLength / 8;
  if (bytesLen % 4 || bytesLen < 16 || bytesLen > 32) {
    throw new TypeError(`entropy length ${entropyLength} is invalid`);
  }
  
  return RNHDWallet.generateMnemonic(entropyLength);
};

/**
 * Generates 12 mnemonic words from 32 bits seed.
 * 
 * @param {Buffer} seed
 * @return {PromiseLike<Object>}
 */
export const mnemonicFromSeed = (seed) => {
  if (Buffer.isBuffer(seed)) {
    return RNHDWallet.mnemonicFromSeed(seed);
  }

  if (buffer instanceof Uint8Array) {
    return RNHDWallet.mnemonicFromSeed(Buffer.from(seed.buffer));
  }

  throw new TypeError('unexpected type, use Buffer or Uint8Array');
};

/**
 * Get 32 bits seed from 12 mnemonic words.
 * 
 * @param {String} mnemonic
 * @return {PromiseLike<Object>}
 */
export const seedFromMnemonic = (mnemonic) => {
  return RNHDWallet.seedFromMnemonic(mnemonic);
};

/**
 * Validate mnemonic phrase
 * 
 * @param {String} mnemonic
 * @return {PromiseLike<Object>}
 */
export const validateMnemonic = mnemonic => {
  return RNHDWallet.validateMnemonic(mnemonic);
};
