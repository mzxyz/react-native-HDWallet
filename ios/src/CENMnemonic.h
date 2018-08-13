//
//  CENMnemonic.h
//  RNHDWallet
//
//  Created by Michael Zhai on 13/08/18.
//  Copyright © 2018 Centrality. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CENMnemonic : NSObject

+ (NSData *)generateMnemonic;

+ (NSData *)mnemonicFromSeed:(NSData *)seed;

+ (NSData *)seedFromMnemonic:(NSData *)mnemonic;

+ (BOOL)validateMnemonic;

@end
