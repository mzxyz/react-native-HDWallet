//
//  NSData+Buffer.h
//  RNNacl
//
//  Created by Michael Zhai on 25/07/18.
//  Copyright © 2018 wallet. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSData (Buffer)

+ (NSData *)dataWithBuffer:(NSArray<NSNumber *> *)buffer;

- (NSArray<NSNumber *> *)dataToBuffer;

@end
