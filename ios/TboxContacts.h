//
//  TboxContact.h
//  tbox-commons
//
//  Created by LAP01378 on 21/12/2021.
//
#import <React/RCTBridgeModule.h>

@import Contacts;
@import ContactsUI;

@interface TboxContacts : NSObject <RCTBridgeModule, CNContactPickerDelegate>

@end
