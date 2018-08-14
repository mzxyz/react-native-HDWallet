//
//  CENMnemonic.m
//  RNHDWallet
//
//  Created by Michael Zhai on 13/08/18.
//  Copyright © 2018 Centrality. All rights reserved.
//

#import "CENMnemonic.h"
#import <CommonCrypto/CommonCrypto.h>
#include "bip39.h"

#define PBKDF2_HMAC_SHA512_SEED_LEN 512;

@implementation CENMnemonic

+ (NSString *)generateMnemonic:(NSUInteger)entropyLength {
    // generate random data With entropy length
    const NSUInteger entropyBytesLength = entropyLength / 8;
    NSMutableData *entropy = [NSMutableData dataWithLength:(entropyBytesLength)];
    if (SecRandomCopyBytes(kSecRandomDefault, entropy.length, entropy.mutableBytes) != noErr) {
        return nil;
    }

    const char *mnemonicStr = mnemonic_from_data(entropy.bytes, (int)entropy.length);
    NSString *mnemonicPhrase = [NSString stringWithCString:mnemonicStr encoding:NSUTF8StringEncoding];

    return mnemonicPhrase;
}

+ (NSString *)mnemonicFromSeed:(NSData *)seed {
    if (!seed) {
        @throw [NSException exceptionWithName:@"seed error"
                                       reason:@"seed can not be nil"
                                     userInfo:nil];
    }

    const char *mnemonicStr = mnemonic_from_data(seed.bytes, (int)seed.length);
    NSString *mnemonicPhrase = [NSString stringWithCString:mnemonicStr encoding:NSUTF8StringEncoding];
    
    return mnemonicPhrase;
}

+ (NSData *)seedFromMnemonic:(NSString *)mnemonic {
    const char *phrase = [mnemonic cStringUsingEncoding:NSUTF8StringEncoding];
    if (![CENMnemonic validateMnemonic:mnemonic]) {
        @throw [NSException exceptionWithName:@"mnemonic error"
                                       reason:@"invalid mnemonic phrase"
                                     userInfo:nil];
    }
    
    NSUInteger phraseLen = [[mnemonic componentsSeparatedByString:@" "] count];
    // use pbkdf2_hmac_sha512 to get seed, seed length 512-bits (64 bytes)
    NSMutableData *seed = [NSMutableData dataWithLength:PBKDF2_HMAC_SHA512_SEED_LEN];
    mnemonic_to_seed(phrase, "", seed.mutableBytes, NULL);
    
    return [seed copy];
}

+ (BOOL)validateMnemonic:(NSString *)mnemonic {
    if (!mnemonic) {
        @throw [NSException exceptionWithName:@"mnemonic error"
                                       reason:@"mnemonic phrase can not be nil"
                                     userInfo:nil];
    }
    
    return (mnemonic_check([mnemonic cStringUsingEncoding:NSUTF8StringEncoding]));
}

@end
