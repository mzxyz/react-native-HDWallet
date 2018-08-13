//
//  NSData+Buffer.m
//  RNNacl
//
//  Created by Michael Zhai on 25/07/18.
//  Copyright © 2018 Centrality. All rights reserved.
//

#import "NSData+Buffer.h"

@implementation NSData (Buffer)

+ (NSData *)dataWithBuffer:(NSArray<NSNumber *> *)buffer {
    if (buffer == nil || buffer.count == 0) {
        @throw [NSException exceptionWithName:@"Init data with buffer problem"
                                       reason:@"Buffer can not be nil or empty"
                                     userInfo:nil];
    }
    
    NSMutableData *data = [[NSMutableData alloc] initWithCapacity:buffer.count];
    for (NSNumber *number in buffer) {
        Byte byte = [number unsignedCharValue];
        [data appendBytes:&byte length:sizeof(Byte)];
    }
    
    return data;
}

- (NSArray<NSNumber *> *)dataToBuffer {
    const uint8_t *bytes = [self bytes];
    NSMutableArray *array = [NSMutableArray array];
    for (NSUInteger i = 0, byteStep = 0; i < [self length]; i += 1) {
        uint8_t byteInt = OSReadLittleInt32(bytes, byteStep);
        [array addObject:[NSNumber numberWithInt:byteInt]];
        byteStep += sizeof(uint8_t);
    }
    
    return array;
}

@end
