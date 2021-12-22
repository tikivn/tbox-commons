#import <AddressBook/AddressBook.h>
#import <UIKit/UIKit.h>
#import "TboxContacts.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import <React/RCTLog.h>
@import ContactsUI;

@implementation TboxContacts : NSObject  {
    CNContactStore * contactStore;
    RCTPromiseResolveBlock updateContactPromise;
    CNMutableContact* selectedContact;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        [self preLoadContactView];
    }
    return self;
}

- (void)preLoadContactView
{
    // Init the contactViewController so it will display quicker first time it's accessed
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        NSLog(@"Preloading CNContactViewController");
        CNContactViewController *contactViewController = [CNContactViewController viewControllerForNewContact:nil];
        [contactViewController view];
    });
}

RCT_EXPORT_MODULE();

-(NSDictionary*) contactToDictionary:(CNContact *) person
                      withThumbnails:(BOOL)withThumbnails
{
    NSMutableDictionary* output = [NSMutableDictionary dictionary];
    
    NSString *recordID = person.identifier;
    NSString *givenName = person.givenName;
    NSString *familyName = person.familyName;
    NSString *middleName = person.middleName;
    NSString *company = person.organizationName;
    NSString *jobTitle = person.jobTitle;
    NSDateComponents *birthday = person.birthday;
    
    [output setObject:recordID forKey: @"recordID"];
    
    if (givenName) {
        [output setObject: (givenName) ? givenName : @"" forKey:@"givenName"];
    }
    
    if (familyName) {
        [output setObject: (familyName) ? familyName : @"" forKey:@"familyName"];
    }
    
    if(middleName){
        [output setObject: (middleName) ? middleName : @"" forKey:@"middleName"];
    }
    
    if(company){
        [output setObject: (company) ? company : @"" forKey:@"company"];
    }
    
    if(jobTitle){
        [output setObject: (jobTitle) ? jobTitle : @"" forKey:@"jobTitle"];
    }
    
    
    if (birthday) {
        if (birthday.month != NSDateComponentUndefined && birthday.day != NSDateComponentUndefined) {
            //months are indexed to 0 in JavaScript (0 = January) so we subtract 1 from NSDateComponents.month
            if (birthday.year != NSDateComponentUndefined) {
                [output setObject:@{@"year": @(birthday.year), @"month": @(birthday.month), @"day": @(birthday.day)} forKey:@"birthday"];
            } else {
                [output setObject:@{@"month": @(birthday.month), @"day":@(birthday.day)} forKey:@"birthday"];
            }
        }
    }
    
    //handle phone numbers
    NSMutableArray *phoneNumbers = [[NSMutableArray alloc] init];
    
    for (CNLabeledValue<CNPhoneNumber*>* labeledValue in person.phoneNumbers) {
        NSMutableDictionary* phone = [NSMutableDictionary dictionary];
        NSString * label = [CNLabeledValue localizedStringForLabel:[labeledValue label]];
        NSString* value = [[labeledValue value] stringValue];
        
        if(value) {
            if(!label) {
                label = [CNLabeledValue localizedStringForLabel:@"other"];
            }
            [phone setObject: value forKey:@"number"];
            [phone setObject: label forKey:@"label"];
            [phoneNumbers addObject:phone];
        }
    }
    
    [output setObject: phoneNumbers forKey:@"phoneNumbers"];
    //end phone numbers
    
    //handle urls
    NSMutableArray *urlAddresses = [[NSMutableArray alloc] init];
    
    for (CNLabeledValue<NSString*>* labeledValue in person.urlAddresses) {
        NSMutableDictionary* url = [NSMutableDictionary dictionary];
        NSString* label = [CNLabeledValue localizedStringForLabel:[labeledValue label]];
        NSString* value = [labeledValue value];
        
        if(value) {
            if(!label) {
                label = [CNLabeledValue localizedStringForLabel:@"home"];
            }
            [url setObject: value forKey:@"url"];
            [url setObject: label forKey:@"label"];
            [urlAddresses addObject:url];
        } else {
            NSLog(@"ignoring blank url");
        }
    }
    
    [output setObject: urlAddresses forKey:@"urlAddresses"];
    
    //end urls
    
    //handle emails
    NSMutableArray *emailAddreses = [[NSMutableArray alloc] init];
    
    for (CNLabeledValue<NSString*>* labeledValue in person.emailAddresses) {
        NSMutableDictionary* email = [NSMutableDictionary dictionary];
        NSString* label = [CNLabeledValue localizedStringForLabel:[labeledValue label]];
        NSString* value = [labeledValue value];
        
        if(value) {
            if(!label) {
                label = [CNLabeledValue localizedStringForLabel:@"other"];
            }
            [email setObject: value forKey:@"email"];
            [email setObject: label forKey:@"label"];
            [emailAddreses addObject:email];
        } else {
            RCTLog(@"ignoring blank email");
        }
    }
    
    [output setObject: emailAddreses forKey:@"emailAddresses"];
    //end emails
    
    //handle postal addresses
    NSMutableArray *postalAddresses = [[NSMutableArray alloc] init];
    
    for (CNLabeledValue<CNPostalAddress*>* labeledValue in person.postalAddresses) {
        CNPostalAddress* postalAddress = labeledValue.value;
        NSMutableDictionary* address = [NSMutableDictionary dictionary];
        
        NSString* street = postalAddress.street;
        if(street){
            [address setObject:street forKey:@"street"];
        }
        NSString* city = postalAddress.city;
        if(city){
            [address setObject:city forKey:@"city"];
        }
        NSString* state = postalAddress.state;
        if(state){
            [address setObject:state forKey:@"state"];
        }
        NSString* region = postalAddress.state;
        if(region){
            [address setObject:region forKey:@"region"];
        }
        NSString* postCode = postalAddress.postalCode;
        if(postCode){
            [address setObject:postCode forKey:@"postCode"];
        }
        NSString* country = postalAddress.country;
        if(country){
            [address setObject:country forKey:@"country"];
        }
        
        NSString* label = [CNLabeledValue localizedStringForLabel:labeledValue.label];
        if(label) {
            [address setObject:label forKey:@"label"];
            
            [postalAddresses addObject:address];
        }
    }
    
    [output setObject:postalAddresses forKey:@"postalAddresses"];
    //end postal addresses
    
    //handle instant message addresses
    NSMutableArray *imAddresses = [[NSMutableArray alloc] init];
    
    for (CNLabeledValue<CNInstantMessageAddress*>* labeledValue in person.instantMessageAddresses) {
        NSMutableDictionary* imAddress = [NSMutableDictionary dictionary];
        CNInstantMessageAddress* imAddressData = labeledValue.value;
        NSString* service = [CNLabeledValue localizedStringForLabel: imAddressData.service];
        NSString* username = imAddressData.username;
        
        if(username) {
            if(!service) {
                service = [CNLabeledValue localizedStringForLabel:@"other"];
            }
            [imAddress setObject: service forKey:@"service"];
            [imAddress setObject: username forKey:@"username"];
            [imAddresses addObject: imAddress];
        } else {
            RCTLog(@"ignoring blank instant message address");
        }
    }
    
    [output setObject: imAddresses forKey:@"imAddresses"];
    //end instant message addresses
    
    [output setValue:[NSNumber numberWithBool:person.imageDataAvailable] forKey:@"hasThumbnail"];
    if (withThumbnails) {
        [output setObject:[self getFilePathForThumbnailImage:person recordID:recordID] forKey:@"thumbnailPath"];
    }
    
    return output;
}

- (NSString *)thumbnailFilePath:(NSString *)recordID
{
    NSString *filename = [recordID stringByReplacingOccurrencesOfString:@":ABPerson" withString:@""];
    NSString* filepath = [NSString stringWithFormat:@"%@/rncontacts_%@.png", [self getPathForDirectory:NSCachesDirectory], filename];
    return filepath;
}

-(NSString *) getFilePathForThumbnailImage:(CNContact*) contact recordID:(NSString*) recordID
{
    if (contact.imageDataAvailable){
        NSString *filepath = [self thumbnailFilePath:recordID];
        NSData *contactImageData = contact.thumbnailImageData;
        
        if ([[NSFileManager defaultManager] fileExistsAtPath:filepath]) {
            NSData *existingImageData = [NSData dataWithContentsOfFile: filepath];
            
            if([contactImageData isEqual: existingImageData]) {
                return filepath;
            }
        }
        
        BOOL success = [[NSFileManager defaultManager] createFileAtPath:filepath contents:contactImageData attributes:nil];
        
        if (!success) {
            RCTLog(@"Unable to copy image");
            return @"";
        }
        
        return filepath;
    }
    
    return @"";
}

- (NSString *)getPathForDirectory:(int)directory
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(directory, NSUserDomainMask, YES);
    return [paths firstObject];
}

-(NSString *) getFilePathForThumbnailImage:(NSString *)recordID
                               addressBook:(CNContactStore*)addressBook
{
    NSString *filepath = [self thumbnailFilePath:recordID];
    
    if([[NSFileManager defaultManager] fileExistsAtPath:filepath]) {
        return filepath;
    }
    
    NSError* contactError;
    NSArray * keysToFetch =@[CNContactThumbnailImageDataKey, CNContactImageDataAvailableKey];
    CNContact* contact = [addressBook unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError];
    
    return [self getFilePathForThumbnailImage:contact recordID:recordID];
}

RCT_EXPORT_METHOD(openContactForm:(NSDictionary *)contactData
                  resolver:(RCTPromiseResolveBlock) resolve
                  rejecter:(RCTPromiseRejectBlock) __unused reject)
{
    CNMutableContact * contact = [[CNMutableContact alloc] init];
    
    [self updateRecord:contact withData:contactData];
    
    CNContactViewController *controller = [CNContactViewController viewControllerForNewContact:contact];
    
    
    controller.delegate = self;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:controller];
        UIViewController *viewController = (UIViewController*)[[[[UIApplication sharedApplication] delegate] window] rootViewController];
        while (viewController.presentedViewController)
        {
            viewController = viewController.presentedViewController;
        }
        [viewController presentViewController:navigation animated:YES completion:nil];
        
        self->updateContactPromise = resolve;
    });
}

RCT_EXPORT_METHOD(openExistingContact:(NSDictionary *)contactData resolver:(RCTPromiseResolveBlock) resolve
                  rejecter:(RCTPromiseRejectBlock) reject)
{
    if(!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }
    
    NSString* recordID = [contactData valueForKey:@"recordID"];
    NSString* backTitle = [contactData valueForKey:@"backTitle"];
    
    NSArray *keys = @[CNContactIdentifierKey,
                      CNContactEmailAddressesKey,
                      CNContactBirthdayKey,
                      CNContactImageDataKey,
                      CNContactPhoneNumbersKey,
                      [CNContactFormatter descriptorForRequiredKeysForStyle:CNContactFormatterStyleFullName],
                      [CNContactViewController descriptorForRequiredKeys]];
    
    @try {
        
        CNContact *contact = [contactStore unifiedContactWithIdentifier:recordID keysToFetch:keys error:nil];
        
        CNContactViewController *contactViewController = [CNContactViewController viewControllerForContact:contact];
        
        // Add a cancel button which will close the view
        contactViewController.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:backTitle == nil ? @"Cancel" : backTitle style:UIBarButtonSystemItemCancel target:self action:@selector(cancelContactForm)];
        contactViewController.delegate = self;
        
        
        dispatch_async(dispatch_get_main_queue(), ^{
            UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:contactViewController];
            
            UIViewController *currentViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
            
            while (currentViewController.presentedViewController)
            {
                currentViewController = currentViewController.presentedViewController;
            }
            
            [currentViewController presentViewController:navigation animated:YES completion:nil];
            [contactViewController performSelector:@selector(toggleEditing:) withObject:nil afterDelay:0.1];
            
            self->updateContactPromise = resolve;
        });
        
    }
    @catch (NSException *exception) {
        reject(@"Error", [exception reason], nil);
    }
}

RCT_EXPORT_METHOD(addToExistingContact: (NSDictionary *)contactData resolver: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock) reject ) {
    UIViewController *picker = [[CNContactPickerViewController alloc] init];
    ((CNContactPickerViewController *)picker).delegate = self;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *root = [[[UIApplication sharedApplication] delegate] window].rootViewController;
        while(root.presentedViewController) {
            root = root.presentedViewController;
        }
        [root presentViewController:picker animated:YES completion:nil];
        self->updateContactPromise = resolve;
    });
}


- (void)contactPicker:(CNContactPickerViewController *)picker didSelectContact:(CNContact *)contact {
    NSDictionary *contactDict = [self contactToDictionary:contact withThumbnails:true];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (self->updateContactPromise != nil) {
            self->updateContactPromise(contactDict);
        }
    });
}

-(void)contactPickerDidCancel:(CNContactPickerViewController *)picker {
    [self cancelContactForm];
}

- (void)doneContactForm
{
    if (updateContactPromise != nil) {
        UIViewController *rootViewController = (UIViewController*)[[[[UIApplication sharedApplication] delegate] window] rootViewController];
        [rootViewController dismissViewControllerAnimated:YES completion:nil];
        
        NSDictionary *contactDict = [self contactToDictionary:selectedContact withThumbnails:true];
        updateContactPromise(contactDict);
        updateContactPromise = nil;
    }
}

- (void)cancelContactForm
{
    if (updateContactPromise != nil) {
        UIViewController *rootViewController = (UIViewController*)[[[[UIApplication sharedApplication] delegate] window] rootViewController];
        [rootViewController dismissViewControllerAnimated:YES completion:nil];
        
        updateContactPromise(nil);
        updateContactPromise = nil;
    }
}

//dismiss open contact page after done or cancel is clicked
- (void)contactViewController:(CNContactViewController *)viewController didCompleteWithContact:(CNContact *)contact {
    [viewController dismissViewControllerAnimated:YES completion:nil];
    
    if(updateContactPromise) {
        
        if (contact) {
            NSDictionary *contactDict = [self contactToDictionary:contact withThumbnails:true];
            updateContactPromise(contactDict);
        } else {
            updateContactPromise(nil);
        }
        
        updateContactPromise = nil;
    }
}

-(void) updateRecord:(CNMutableContact *)contact withData:(NSDictionary *)contactData {
    NSString *givenName = [contactData valueForKey:@"givenName"];
    NSString *familyName = [contactData valueForKey:@"familyName"];
    NSString *middleName = [contactData valueForKey:@"middleName"];
    NSString *company = [contactData valueForKey:@"company"];
    NSString *jobTitle = [contactData valueForKey:@"jobTitle"];
    
    NSDictionary *birthday = [contactData valueForKey:@"birthday"];
    
    contact.givenName = givenName;
    contact.familyName = familyName;
    contact.middleName = middleName;
    contact.organizationName = company;
    contact.jobTitle = jobTitle;
    
    if (birthday) {
        NSDateComponents *components;
        if (contact.birthday != nil) {
            components = contact.birthday;
        } else {
            components = [[NSDateComponents alloc] init];
        }
        if (birthday[@"month"] && birthday[@"day"]) {
            if (birthday[@"year"]) {
                components.year = [birthday[@"year"] intValue];
            }
            components.month = [birthday[@"month"] intValue];
            components.day = [birthday[@"day"] intValue];
        }
        
        contact.birthday = components;
    }
    
    NSMutableArray *phoneNumbers = [[NSMutableArray alloc]init];
    
    for (id phoneData in [contactData valueForKey:@"phoneNumbers"]) {
        NSString *label = [phoneData valueForKey:@"label"];
        NSString *number = [phoneData valueForKey:@"number"];
        
        CNLabeledValue *phone;
        if ([label isEqual: @"main"]){
            phone = [[CNLabeledValue alloc] initWithLabel:CNLabelPhoneNumberMain value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        else if ([label isEqual: @"mobile"]){
            phone = [[CNLabeledValue alloc] initWithLabel:CNLabelPhoneNumberMobile value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        else if ([label isEqual: @"iPhone"]){
            phone = [[CNLabeledValue alloc] initWithLabel:CNLabelPhoneNumberiPhone value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        else{
            phone = [[CNLabeledValue alloc] initWithLabel:label value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        
        [phoneNumbers addObject:phone];
    }
    contact.phoneNumbers = phoneNumbers;
    
    
    NSMutableArray *urls = [[NSMutableArray alloc]init];
    
    for (id urlData in [contactData valueForKey:@"urlAddresses"]) {
        NSString *label = [urlData valueForKey:@"label"];
        NSString *url = [urlData valueForKey:@"url"];
        
        if(label && url) {
            [urls addObject:[[CNLabeledValue alloc] initWithLabel:label value:url]];
        }
    }
    
    contact.urlAddresses = urls;
    
    
    NSMutableArray *emails = [[NSMutableArray alloc]init];
    
    for (id emailData in [contactData valueForKey:@"emailAddresses"]) {
        NSString *label = [emailData valueForKey:@"label"];
        NSString *email = [emailData valueForKey:@"email"];
        
        if(label && email) {
            [emails addObject:[[CNLabeledValue alloc] initWithLabel:label value:email]];
        }
    }
    
    contact.emailAddresses = emails;
    
    NSMutableArray *postalAddresses = [[NSMutableArray alloc]init];
    
    for (id addressData in [contactData valueForKey:@"postalAddresses"]) {
        NSString *label = [addressData valueForKey:@"label"];
        NSString *street = [addressData valueForKey:@"street"];
        NSString *postalCode = [addressData valueForKey:@"postCode"];
        NSString *city = [addressData valueForKey:@"city"];
        NSString *country = [addressData valueForKey:@"country"];
        NSString *state = [addressData valueForKey:@"state"];
        
        if(label && street) {
            CNMutablePostalAddress *postalAddr = [[CNMutablePostalAddress alloc] init];
            postalAddr.street = street;
            postalAddr.postalCode = postalCode;
            postalAddr.city = city;
            postalAddr.country = country;
            postalAddr.state = state;
            [postalAddresses addObject:[[CNLabeledValue alloc] initWithLabel:label value: postalAddr]];
        }
    }
    
    contact.postalAddresses = postalAddresses;
    
    NSMutableArray<CNLabeledValue<CNInstantMessageAddress*>*> *imAddresses = [[NSMutableArray alloc] init];
    
    for (id imData in [contactData valueForKey:@"imAddresses"]) {
        NSString *service = [imData valueForKey:@"service"];
        NSString *username = [imData valueForKey:@"username"];
        
        if(service && username) {
            CNLabeledValue *imAddress = [[CNLabeledValue alloc] initWithLabel: @"instantMessageAddress" value: [[CNInstantMessageAddress alloc] initWithUsername: username service: service]];
            [imAddresses addObject: imAddress];
        }
    }
    
    contact.instantMessageAddresses = imAddresses;
    
    NSString *thumbnailPath = [contactData valueForKey:@"thumbnailPath"];
    
    if(thumbnailPath && [thumbnailPath rangeOfString:@"rncontacts_"].location == NSNotFound) {
        contact.imageData = [TboxContacts imageData:thumbnailPath];
    }
}

+ (NSData*) imageData:(NSString*)sourceUri
{
    NSURL *url = [NSURL URLWithString:sourceUri];
    
    if([sourceUri hasPrefix:@"assets-library"]){
        return [TboxContacts loadImageAsset:[NSURL URLWithString:sourceUri]];
    } else if (url && url.scheme && url.host) {
        return [NSData dataWithContentsOfURL:url];
    } else if ([sourceUri isAbsolutePath]) {
        return [NSData dataWithContentsOfFile:sourceUri];
    } else {
        sourceUri = [[NSBundle mainBundle] pathForResource:[sourceUri stringByDeletingPathExtension] ofType:[sourceUri pathExtension]];
        return [NSData dataWithContentsOfFile:sourceUri];
    }
}

enum { WDASSETURL_PENDINGREADS = 1, WDASSETURL_ALLFINISHED = 0};

+ (NSData*) loadImageAsset:(NSURL*)assetURL {
    //thanks to http://www.codercowboy.com/code-synchronous-alassetlibrary-asset-existence-check/
    
    __block NSData *data = nil;
    __block NSConditionLock * albumReadLock = [[NSConditionLock alloc] initWithCondition:WDASSETURL_PENDINGREADS];
    //this *MUST* execute on a background thread, ALAssetLibrary tries to use the main thread and will hang if you're on the main thread.
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        ALAssetsLibrary * assetLibrary = [[ALAssetsLibrary alloc] init];
        [assetLibrary assetForURL:assetURL
                      resultBlock:^(ALAsset *asset) {
            ALAssetRepresentation *rep = [asset defaultRepresentation];
            
            Byte *buffer = (Byte*)malloc(rep.size);
            NSUInteger buffered = [rep getBytes:buffer fromOffset:0.0 length:rep.size error:nil];
            data = [NSData dataWithBytesNoCopy:buffer length:buffered freeWhenDone:YES];
            
            [albumReadLock lock];
            [albumReadLock unlockWithCondition:WDASSETURL_ALLFINISHED];
        } failureBlock:^(NSError *error) {
            RCTLog(@"asset error: %@", [error localizedDescription]);
            
            [albumReadLock lock];
            [albumReadLock unlockWithCondition:WDASSETURL_ALLFINISHED];
        }];
    });
    
    [albumReadLock lockWhenCondition:WDASSETURL_ALLFINISHED];
    [albumReadLock unlock];
    
    RCTLog(@"asset lookup finished: %@ %@", [assetURL absoluteString], (data ? @"exists" : @"does not exist"));
    
    return data;
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

@end
