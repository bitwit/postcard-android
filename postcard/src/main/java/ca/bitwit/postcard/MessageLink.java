
package ca.bitwit.postcard;

import ca.bitwit.postcard.MessageAttachment;


public class MessageLink extends MessageAttachment{

    public String url;
    public String title;
    public String description;
    public String imageLocation;

    private String originalUrlSubmitted;
    private String contentType;
    // private Dictionary metaMap;

    public MessageLink(){
        /*
            self.metaMap = @{
                @"title" : @[
                        @"og:title",
                        @"twitter:title"
                ],
                @"description" : @[
                        @"og:description",
                        @"twitter:description"
                ],
                @"imageURL" : @[
                        @"og:image",
                        @"twitter:image",
                ]
            };
        */
    }

    public void setMessageLinkUrl(String urlString){
        this.originalUrlSubmitted = urlString;
        this.progress = 0.0;
        this.url = urlString; //TODO: clean URL [PCURLMaker validURLStringForString:url withBaseURL:nil];
        this.setParseProgress(0.1);
        this.parseUrlForMeta();
    }

    public String originalUrl(){
        return this.originalUrlSubmitted;
    }

    public void parseUrlForMeta(){

    }

    public void handleContentType(){

    }

    public void parseHTMLPageForMeta(){

    }

    public void setParseProgress(Double progress){

    }

    public void completeLinkParsing(){

    }

    public void downloadImage(){

    }

}

/*


- (void)parseURLForMeta {
    NSURL *url = [NSURL URLWithString:_url];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];

    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *op, id responseObject) {
        BWNetLog(@"Operation Succeeded --> %@", op);
        NSDictionary *headers = op.response.allHeaderFields;
        self.contentType = headers[@"Content-Type"];
        BWLog(@"url content type -> %@", _contentType);
        [self setParseProgress:0.5f];
        [self handleContentType];
    }                                failure:^(AFHTTPRequestOperation *op, NSError *failure) {
        BWNetLog(@"Operation failed -- %@", [op responseString]);
    }];
    [operation start];
}

- (void)handleContentType {
    if ([_contentType rangeOfString:@"text/html"].location != NSNotFound) {
        [self parseHTMLPageForMeta];
        return;
    } else {
        self.title = [[NSURL URLWithString:_url] lastPathComponent];
        self.description = self.contentType;
        if([_contentType rangeOfString:@"image/png"].location != NSNotFound || [_contentType rangeOfString:@"image/jpg"].location != NSNotFound || [_contentType rangeOfString:@"image/gif"].location != NSNotFound){
            self.imageURL = _url;
        }
        [self completeLinkParsing];
    }
}

- (void)parseHTMLPageForMeta {
    NSURL *url = [NSURL URLWithString:_url];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];

    [request setValue:@"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.69 Safari/537.36" forHTTPHeaderField:@"User-Agent"];

    BWNetLog(@"Request Headers %@", request.allHTTPHeaderFields);

    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *op, id responseObject) {
        NSString *html = [[NSString alloc] initWithData:responseObject encoding:NSUTF8StringEncoding];
        BWNetLog(@"Operation Succeeded --> %@ || %@", op, html);
        NSError *error;
        HTMLParser *parser = [[HTMLParser alloc] initWithString:html error:&error];
        HTMLNode *head = parser.head;
        for (HTMLNode *node in [head findChildTags:@"meta"]) {
            if ([[node getAttributeNamed:@"name"] isEqualToString:@"description"] && _description == nil) {
                self.description = [node getAttributeNamed:@"content"];
            }
            for (NSString *property in _metaMap.allKeys) {
                for (NSString *propertyName in _metaMap[property]) {
                    if ([[node getAttributeNamed:@"property"] isEqualToString:propertyName]) {
                        [self setValue:[node getAttributeNamed:@"content"] forKey:property];
                        break;
                    }
                }
            }
        }

        if (_title == nil) {
            HTMLNode *titleNode = [head findChildTag:@"title"];
            self.title = titleNode.contents;
        }

        if(_imageURL == nil){
            HTMLNode *mainImg = [parser.body findChildWithAttribute:@"id" matchingName:@"main_image" allowPartial:YES];
            HTMLNode *postImg = [parser.body findChildWithAttribute:@"class" matchingName:@"wp-post-image" allowPartial:YES];
            if(mainImg != nil && [mainImg.tagName isEqualToString:@"img"]){
                self.imageURL = [mainImg getAttributeNamed:@"src"];
            } else if(postImg != nil){
                self.imageURL = [postImg getAttributeNamed:@"src"];
            } else {
                NSArray *nodes = [parser.body findChildTags:@"img"];
                for(HTMLNode *imgNode in nodes){
                    NSString *imgTitle = [imgNode getAttributeNamed:@"title"];
                    if( imgTitle != nil){
                        self.imageURL = [imgNode getAttributeNamed:@"src"];
                    }
                }
                //last resort, just take the first image on the page
                if(_imageURL == nil && nodes.count > 0){
                    self.imageURL = [(HTMLNode *)nodes.firstObject getAttributeNamed:@"src"];
                }
            }
        }

        if(_imageURL != nil){
            self.imageURL = [PCURLMaker validURLStringForString:_imageURL withBaseURL:url.host];
        }

        [self completeLinkParsing];

    } failure:^(AFHTTPRequestOperation *op, NSError *failure) {
        BWNetLog(@"Operation failed -- %@", [op responseString]);
    }];
    [operation setDownloadProgressBlock:^(NSUInteger bytesRead, long long int totalBytesRead, long long int totalBytesExpectedToRead) {
        double dlProgress = (double)totalBytesRead / totalBytesExpectedToRead * 0.5f;
        [self setParseProgress:0.5f + dlProgress];
    }];

    [operation start];
}

-(void)setParseProgress:(double)progress {
    self.progress.completedUnitCount = (int)(50 * progress);
}

- (void)completeLinkParsing {
    BWLog(@"\nurl : %@ \ntitle : %@ \ndescription : %@ \nimage : %@", _url, _title, _description, _imageURL);
    if(_imageURL != nil){
        [self setParseProgress:1.0f];
        [self downloadImage];
    } else {
        self.progress.completedUnitCount = 100;
    }
    [[NSNotificationCenter defaultCenter] postNotificationName:@"MessageLinkParsingComplete" object:nil];
}

-(void)downloadImage{
    NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:_imageURL]];
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *op, id responseObject) {
        BWNetLog(@"Success");
        self.image = [UIImage imageWithData:op.responseData];
        BWLog(@"Image downloaded");
        self.progress.completedUnitCount = 100;
    } failure:^(AFHTTPRequestOperation *op, NSError *error) {
        BWNetLog(@"Failure -- %@", error.description);
        self.progress.completedUnitCount = 100;
    }];

    [operation setDownloadProgressBlock:^(NSUInteger bytesRead, long long int totalBytesRead, long long int totalBytesExpectedToRead) {
        int completion = 50 + (int)(totalBytesExpectedToRead/totalBytesExpectedToRead * 50); //up to 90% of the completion
        self.progress.completedUnitCount = completion;
    }];

    [operation start];
}

*/