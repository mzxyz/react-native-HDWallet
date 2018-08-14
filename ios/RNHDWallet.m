
#import "RNHDWallet.h"
#import "CENMnemonic.h"
#import "NSData+Buffer.h"

@implementation RNHDWallet

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(generateMnemonic:(NSUInteger)entropyLength
                           resolve:(RCTPromiseResolveBlock)resolve
                            reject:(RCTPromiseRejectBlock)reject) {
    @try
    {
        NSString *mnemonicPhrase = [CENMnemonic generateMnemonic:entropyLength];
        resolve(mnemonicPhrase);
    }
    @catch (NSException *ex)
    {
        [RNHDWallet reject:reject exception:ex];
    }
}

RCT_EXPORT_METHOD(seedFromMnemonic:(NSString *)mnemonic
                           resolve:(RCTPromiseResolveBlock)resolve
                            reject:(RCTPromiseRejectBlock)reject) {
    @try
    {
        NSData *seed = [CENMnemonic seedFromMnemonic:mnemonic];
        NSArray<NSNumber *> *seedBuffer = [seed dataToBuffer];
        resolve(seedBuffer);
    }
    @catch (NSException *ex)
    {
        [RNHDWallet reject:reject exception:ex];
    }
}

RCT_EXPORT_METHOD(validateMnemonic:(NSString *)mnemonic
                           resolve:(RCTPromiseResolveBlock)resolve
                            reject:(RCTPromiseRejectBlock)reject) {
    @try
    {
        BOOL isValid = [CENMnemonic validateMnemonic:mnemonic];
        resolve(@(isValid));
    }
    @catch (NSException *ex)
    {
        [RNHDWallet reject:reject exception:ex];
    }
}

+ (void)reject:(RCTPromiseRejectBlock)reject exception:(NSException *)exception
{
    NSString *name = exception.name;
    NSString *reason = exception.reason;
    NSError *error = [[NSError alloc] initWithDomain:NSCocoaErrorDomain
                                                code:0
                                            userInfo:@{@"message": reason}];
    
    if (reject) {
        reject(name, reason, error);
    }
}

@end
