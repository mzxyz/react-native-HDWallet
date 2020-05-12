//
//  CENMnemonic.h
//  RNHDWallet
//
//  Created by Michael Zhai on 13/08/18.
//  Copyright © 2018 wallet. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CENMnemonic : NSObject

+ (NSString *)generateMnemonic:(NSUInteger)entropyLength;

+ (NSData *)seedFromMnemonic:(NSString *)mnemonic;

+ (BOOL)validateMnemonic:(NSString *)mnemonic;

@end
