
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNZebraLinkosSpec.h"

@interface ZebraLinkos : NSObject <NativeZebraLinkosSpec>
#else
#import <React/RCTBridgeModule.h>

@interface ZebraLinkos : NSObject <RCTBridgeModule>
#endif

@end
