#if __has_include(<React/RCTConvert.h>)
#import <React/RCTConvert.h>
#elif __has_include("RCTConvert.h")
#import "RCTConvert.h"
#else
#import "React/RCTConvert.h"
#endif

#import "React/RCTFont.h"
#import "TboxCommons.h"

@implementation TboxCommons

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(measure:(NSArray *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSMutableArray* results = [[NSMutableArray alloc] init];
    for (NSDictionary *option in options) {
        float width = [RCTConvert float:option[@"width"]];
        float height = [RCTConvert float:option[@"height"]];
        NSInteger lineHeight = [RCTConvert NSInteger:option[@"lineHeight"]];
        NSString *text = [RCTConvert NSString:option[@"text"]];
        CGFloat fontSize = [RCTConvert CGFloat:option[@"fontSize"]];
        NSString *fontFamily = [RCTConvert NSString:option[@"fontFamily"]];
        NSString *fontWeight = [RCTConvert NSString:option[@"fontWeight"]];
        UIFont *font = [self getFont:fontFamily size:fontSize weight:fontWeight];
        float result = 0;
        if (font) {
            result = [self calculteSize:text font:font lineHeight:lineHeight width:width height:height];
        }
        [results addObject:[NSNumber numberWithFloat:result]];
    }
    resolve(results);
}


- (float)calculteSize:(NSString*)text
                 font:(UIFont *)font
           lineHeight:(NSInteger)lineHeight
                width:(float)width
               height:(float)height {
    BOOL isMeasureWidth = (width == 0);
    NSTextContainer *textContainer = [[NSTextContainer alloc] initWithSize: CGSizeMake(isMeasureWidth ? FLT_MAX : width, isMeasureWidth ? height : FLT_MAX)];
    NSTextStorage *textStorage = [[NSTextStorage alloc] initWithString:text];
    NSLayoutManager *layoutManager = [[NSLayoutManager alloc] init];
    
    [layoutManager addTextContainer:textContainer];
    [textStorage addLayoutManager:layoutManager];
    [textStorage addAttribute:NSFontAttributeName value:font
                        range:NSMakeRange(0, [textStorage length])];
    if (lineHeight > 0) {
        NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
        paragraphStyle.lineBreakMode = NSLineBreakByWordWrapping;
        paragraphStyle.minimumLineHeight = lineHeight;
        paragraphStyle.maximumLineHeight = lineHeight;
        [textStorage addAttribute:NSParagraphStyleAttributeName value:paragraphStyle
        range:NSMakeRange(0, [textStorage length])];
    }
    [textContainer setLineFragmentPadding:0.0];
    (void) [layoutManager glyphRangeForTextContainer:textContainer];
    CGRect rect = [layoutManager usedRectForTextContainer:textContainer];
    return isMeasureWidth ? rect.size.width : rect.size.height;
}

- (UIFont *)getFont:(NSString *)fontFamily
               size:(CGFloat)fontSize
             weight:(NSString*)fontWeight {
    if (fontWeight == nil) {
        fontWeight = @"normal";
    };
    return fontFamily == nil ?
    [RCTConvert UIFont:@{@"fontWeight": fontWeight, @"fontSize": [NSNumber numberWithFloat:fontSize]}] :
    [RCTConvert UIFont:@{@"fontFamily": fontFamily, @"fontSize": [NSNumber numberWithFloat:fontSize], @"fontWeight": fontWeight}];
}
@end
