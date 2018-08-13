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

#define MNEMONIC_PHRASE_LENGTH    16
#define SEED_LENGTH               32

@implementation CENMnemonic

+ (NSString *)generateMnemonic {
    NSMutableData *data = [NSMutableData dataWithLength:MNEMONIC_PHRASE_LENGTH];
    if (SecRandomCopyBytes(kSecRandomDefault, data.length, data.mutableBytes) != noErr) {
        return nil;
    }

    const char *mnemonicStr = mnemonic_from_data(data.bytes, (int)data.length);
    NSString *mnemonicPhrase = [NSString stringWithCString:mnemonicStr encoding:NSUTF8StringEncoding];

    return mnemonicPhrase;
}

+ (NSString *)mnemonicFromSeed:(NSData *)seed {
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
    
    NSMutableData *seed = [NSMutableData dataWithLength:SEED_LENGTH];
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
