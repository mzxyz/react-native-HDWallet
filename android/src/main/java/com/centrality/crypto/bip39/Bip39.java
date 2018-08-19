package com.centrality.crypto.bip39;

// import com.google.common.base.Optional;
import com.centrality.crypto.bip39.WordList;
import com.centrality.crypto.bip39.PBKDF;
import com.centrality.crypto.utils.ByteReader;
import com.centrality.crypto.utils.ByteWriter;
import com.centrality.crypto.utils.HashUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Implementation of Bip39
 */
public class Bip39 {
   private static final String ALGORITHM = "HmacSHA512";
   private static final int REPETITIONS = 2048;
   private static final int BIP32_SEED_LENGTH = 64;
   private static final String BASE_SALT = "mnemonic";
   private static final String UTF8 = "UTF-8";
   private static final String[] ENGLISH_WORD_LIST = WordList.words;
   private static final byte ENGLISH_WORD_LIST_TYPE = 0;

   public static class MasterSeed implements Serializable {
      private static final long serialVersionUID = 1L;

      private final byte[] _bip39RawEntropy;
      private final String _bip39Passphrase;
      private final byte[] _bip32MasterSeed;
      private final byte _wordListType;

      private MasterSeed(byte[] bip39RawEntropy, String bip39Passphrase, byte[] bip32MasterSeed) {
         _bip39RawEntropy = bip39RawEntropy;
         _bip39Passphrase = Normalizer.normalize(bip39Passphrase, Normalizer.Form.NFKD);
         _bip32MasterSeed = bip32MasterSeed;
         _wordListType = ENGLISH_WORD_LIST_TYPE;
      }

      public List<String> getBip39WordList() {
         return Arrays.asList(rawEntropyToWords(_bip39RawEntropy));
      }

      public String getBip39Passphrase() {
         return _bip39Passphrase;
      }

      public byte[] getBip32Seed() {
         return BitUtils.copyByteArray(_bip32MasterSeed);
      }

      /**
       * Turn the master seed into binary form. The format can be full or compressed. Compressed form is smaller,
       * but requires the BIP32 seed to be calculated.
       *
       * @param compressed Use the compressed form or the full form
       * @return the master seed in binary form
       */
      public byte[] toBytes(boolean compressed) {
         ByteWriter writer = new ByteWriter(1024);

         // Add the word list type used
         writer.put(_wordListType);

         // Add the raw entropy
         putByteArray(_bip39RawEntropy, writer);

         // Add the passphrase
         try {
            putByteArray(_bip39Passphrase.getBytes("UTF-8"), writer);
         } catch (UnsupportedEncodingException e) {
            // Never happens
            throw new RuntimeException(e);
         }

         // The uncompressed form also has the BIP32 seed
         if (!compressed) {
            putByteArray(_bip32MasterSeed, writer);
         }
         return writer.toBytes();
      }

      /**
       * Creates a MasterSeed from binary form
       *
       * @param bytes      the binary form
       * @param compressed is the binary form a compressed or uncompressed representation
       * @return A master seed
       */
      public static Optional<MasterSeed> fromBytes(byte[] bytes, boolean compressed) {
         ByteReader reader = new ByteReader(bytes);
         try {
            // Get the type of word list used. So far only english is supported
            byte wordListType = reader.get();
            if (wordListType != ENGLISH_WORD_LIST_TYPE) {
               return Optional.absent();
            }
            byte[] bip39RawEntropy = getByteArray(reader);
            if (bip39RawEntropy.length != 128 / 8 &&
                  bip39RawEntropy.length != 160 / 8 &&
                  bip39RawEntropy.length != 192 / 8 &&
                  bip39RawEntropy.length != 224 / 8 &&
                  bip39RawEntropy.length != 256 / 8) {
               return Optional.absent();
            }
            String bip39Passphrase;
            try {
               byte[] bip39PassphraseBytes = getByteArray(reader);
               bip39Passphrase = new String(bip39PassphraseBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
               // Never happens
               throw new RuntimeException(e);
            }

            if (compressed) {
               // We are using compressed form, so we have to calculate the actual master seed
               return Optional.of(generateSeedFromWordList(rawEntropyToWords(bip39RawEntropy), bip39Passphrase));
            } else {
               byte[] bip32MasterSeed = getByteArray(reader);
               if (bip32MasterSeed.length != BIP32_SEED_LENGTH) {
                  return Optional.absent();
               }
               return Optional.of(new MasterSeed(bip39RawEntropy, bip39Passphrase, bip32MasterSeed));
            }
         } catch (ByteReader.InsufficientBytesException e) {
            return Optional.absent();
         }
      }

      private static byte[] getByteArray(ByteReader reader) throws ByteReader.InsufficientBytesException {
         int size = (int) reader.getCompactInt();
         if (size < 0 || size > 200) {
            throw new ByteReader.InsufficientBytesException();
         }
         return reader.getBytes(size);
      }

      private static void putByteArray(byte[] buf, ByteWriter writer) {
         writer.putCompactInt(buf.length);
         writer.putBytes(buf);
      }

      @Override
      public int hashCode() {
         return (int) BitUtils.uint32ToLong(_bip32MasterSeed, 0);
      }

      @Override
      public boolean equals(Object obj) {
         if (!(obj instanceof MasterSeed)) {
            return false;
         }
         MasterSeed other = (MasterSeed) obj;
         return BitUtils.areEqual(_bip32MasterSeed, other._bip32MasterSeed);
      }
   }

   /**
    * Check whether a list of words is valid
    * </p>
    * Checks that the number of words is valid for Bip39.
    * Checks that the words are in the list of accepted words.
    * Checks that the word have a valid checksum.
    */
   public static boolean isValidWordList(String[] wordList) {
      // Check word list length
      if (wordList.length != 12 &&
            wordList.length != 15 &&
            wordList.length != 18 &&
            wordList.length != 21 &&
            wordList.length != 24) {
         return false;
      }

      // Check words
      for (String word : wordList) {
         if (getWordIndex(word) == -1) {
            return false;
         }
      }

      // Get bytes
      byte[] rawAndChecksum = wordListToBytes(wordList);

      // Verify checksum
      return verifyChecksum(rawAndChecksum);
   }

   private static boolean verifyChecksum(byte[] rawAndChecksum) {
      int bitLength = rawAndChecksum.length * 8;
      int checksumLength;
      if (bitLength == 128 + 8) {
         checksumLength = 4;
      } else if (bitLength == 160 + 8) {
         checksumLength = 5;
      } else if (bitLength == 192 + 8) {
         checksumLength = 6;
      } else if (bitLength == 224 + 8) {
         checksumLength = 7;
      } else if (bitLength == 256 + 8) {
         checksumLength = 8;
      } else {
         return false;
      }

      // Get the raw entropy
      byte[] raw = new byte[rawAndChecksum.length - 1];
      System.arraycopy(rawAndChecksum, 0, raw, 0, raw.length);

      // Calculate checksum
      byte[] csHash = HashUtils.sha256(raw).getBytes();
      byte checksumByte = (byte) (((0xFF << (8 - checksumLength)) & 0xFF) & (0xFF & ((int) csHash[0])));

      // Verify that the checksum is valid
      byte c = rawAndChecksum[rawAndChecksum.length - 1];
      return checksumByte == c;
   }

   private static byte[] wordListToBytes(String[] wordList) {
      if (wordList.length != 12 &&
            wordList.length != 15 &&
            wordList.length != 18 &&
            wordList.length != 21 &&
            wordList.length != 24) {
         throw new RuntimeException("Word list must be 12, 15, 18, 21, or 24 words and not " + wordList.length);
      }
      int bitLength = wordList.length * 11;
      byte[] buf = new byte[bitLength / 8 + ((bitLength % 8) > 0 ? 1 : 0)];
      for (int i = 0; i < wordList.length; i++) {
         String word = wordList[i];
         int wordIndex = getWordIndex(word);
         if (wordIndex == -1) {
            throw new RuntimeException("The word '" + word + "' is not valid");
         }
         integerTo11Bits(buf, i * 11, wordIndex);
      }
      return buf;
   }

   private static byte[] wordListToRawEntropy(String[] wordList) {
      // Get the bytes of the word list
      byte[] bytes = wordListToBytes(wordList);
      // Chop off the checksum byte
      return BitUtils.copyOf(bytes, bytes.length - 1);
   }

   private static void integerTo11Bits(byte[] buf, int bitIndex, int integer) {
      for (int i = 0; i < 11; i++) {
         if ((integer & 0x400) == 0x400) {
            setBit(buf, bitIndex + i);
         }
         integer = integer << 1;
      }
   }

   private static void setBit(byte[] buf, int bitIndex) {
      int value = ((int) buf[bitIndex / 8]) & 0xFF;
      value = value | (1 << (7 - (bitIndex % 8)));
      buf[bitIndex / 8] = (byte) value;
   }

   private static int getWordIndex(String word) {
      for (int i = 0; i < ENGLISH_WORD_LIST.length; i++) {
         if (ENGLISH_WORD_LIST[i].equals(word)) {
            return i;
         }
      }
      return -1;
   }

   /**
    * Turn raw entropy into a BIP39 word list with checksum.
    * <p/>
    * The raw entropy must be 128, 160, 192, 244, or 256 bits
    *
    * @param rawEntropy the raw entropy
    * @return the corresponding list of words
    */
   public static String[] rawEntropyToWords(byte[] rawEntropy) {
      int bitLength = rawEntropy.length * 8;
      if (bitLength != 128 &&
            bitLength != 160 &&
            bitLength != 192 &&
            bitLength != 224 &&
            bitLength != 256) {
         throw new RuntimeException("Raw entropy must be 128, 160, 192, 224, or 256 bits and not " + bitLength);
      }

      // Calculate the checksum
      int checksumLength = bitLength / 32;
      byte[] csHash = HashUtils.sha256(rawEntropy).getBytes();
      byte checksumByte = (byte) (((0xFF << (8 - checksumLength)) & 0xFF) & (0xFF & ((int) csHash[0])));

      // Append the checksum to the raw entropy
      byte[] buf = new byte[rawEntropy.length + 1];
      System.arraycopy(rawEntropy, 0, buf, 0, rawEntropy.length);
      buf[rawEntropy.length] = checksumByte;

      // Turn the array of bytes into a word list where each word represents 11 bits
      String[] words = new String[(bitLength + checksumLength) / 11];
      for (int i = 0; i < words.length; i++) {
         int wordIndex = integerFrom11Bits(buf, i * 11);
         words[i] = ENGLISH_WORD_LIST[wordIndex];
      }
      return words;
   }

   /**
    * Turn the next 11 bits of a specified bit index of an array of bytes into a positive integer
    *
    * @param buf      the array of bytes
    * @param bitIndex the bit index in the array of bytes
    * @return the next 11 bits of a specified bit index of an array of bytes into a positive integer
    */
   private static int integerFrom11Bits(byte[] buf, int bitIndex) {
      int value = 0;
      for (int i = 0; i < 11; i++) {
         if (isBitSet(buf, bitIndex + i)) {
            value = (value << 1) | 0x01;
         } else {
            value = (value << 1);
         }
      }
      return value;
   }

   /**
    * Determine whether s bit at a specified index in an array of bytes is set.
    *
    * @param buf      the array of bytes
    * @param bitIndex the bit index in the array of bytes
    * @return true if the bit is set, false otherwise
    */
   private static boolean isBitSet(byte[] buf, int bitIndex) {
      int val = ((int) buf[bitIndex / 8]) & 0xFF;
      val = val << (bitIndex % 8);
      val = val & 0x80;
      return val == 0x80;
   }

   /**
    * Create a random mnemonic phrase from a random source
    *
    * @param randomSource the random source to use
    * @return a mnemonic word list 
    */
   public static String[] createRandomWords(int entropyLength) {
      SecureRandom randomSource = new SecureRandom();
      byte[] rawEntropy = new byte[entropyLength / 8];
      randomSource.nextBytes(rawEntropy);
      String[] wordList = rawEntropyToWords(rawEntropy);
      return wordList;
   }

   /**
    * Create a random master seed from a random source
    *
    * @param randomSource the random source to use
    * @return a random master seed
    */
   public static MasterSeed createRandomMasterSeed(RandomSource randomSource) {
      byte[] rawEntropy = new byte[128 / 8];
      randomSource.nextBytes(rawEntropy);
      String[] wordList = rawEntropyToWords(rawEntropy);
      return generateSeedFromWordList(wordList, "");
   }

   /**
    * Generate a master seed from a BIP39 word list.
    * <p/>
    * This method does not check whether the check sum of the word list id valid
    *
    * @param wordList the word list
    * @param passphrase the optional passphrase
    * @return the BIP32 master seed
    */
   public static MasterSeed generateSeedFromWordList(List<String> wordList, String passphrase) {
      // Null passphrase defaults to the empty string
      if (passphrase == null) {
         passphrase = "";
      }

      // Concatenate all words using a single space as separator
      StringBuilder sb = new StringBuilder();
      for (String s : wordList) {
         sb.append(s).append(' ');
      }
      String mnemonic = sb.toString().trim();

      // The salt is the passphrase with a prefix
      String salt = BASE_SALT + passphrase;

      // Calculate and return the seed
      byte[] seed;
      try {
         byte[] saltBytes = Normalizer.normalize(salt, Normalizer.Form.NFKD).getBytes(UTF8);
         seed = PBKDF.pbkdf2(ALGORITHM, mnemonic.getBytes(UTF8), saltBytes, REPETITIONS, BIP32_SEED_LENGTH);
      } catch (UnsupportedEncodingException | GeneralSecurityException e) {
         // UTF-8 should be supported by every system we run on
         throw new RuntimeException(e);
      }
      return new MasterSeed(wordListToRawEntropy(wordList.toArray(new String[0])), passphrase, seed);
   }
}