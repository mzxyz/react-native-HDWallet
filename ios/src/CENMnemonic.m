//
//  CENMnemonic.m
//  RNHDWallet
//
//  Created by Michael Zhai on 13/08/18.
//  Copyright © 2018 Centrality. All rights reserved.
//

#import "CENMnemonic.h"
#include "bip39.h"

@implementation CENMnemonic

+ (NSData *)generateMnemonic {
    return nil;
}

+ (NSData *)mnemonicFromSeed:(NSData *)seed {
    return nil;
}

+ (NSData *)seedFromMnemonic:(NSData *)mnemonic {
    return nil;
}

+ (BOOL)validateMnemonic:(NSData *)mnemonic {
    NSString *phrase = [CENMnemonic dataToString:mnemonic];
    return (mnemonic_check([phrase cStringUsingEncoding:NSUTF8StringEncoding]));
}

+ (NSString *)dataToString:(NSData *)data {
    NSParameterAssert(data);
    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

@end
