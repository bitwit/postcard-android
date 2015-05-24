package ca.bitwit.postcard.network;

import android.os.AsyncTask;
import android.util.Log;
import ca.bitwit.postcard.PostcardAdaptor;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.Boolean;

public class FacebookNetwork extends Network {

    PostcardAdaptor adaptor;
    JSONObject data;
    int networkAccountId;

    public FacebookNetwork(PostcardAdaptor adaptor, JSONObject data) {
        super(adaptor, data);
        this.name = "facebook";
        this.initializeFacebook();
    }

    public void initializeFacebook() {
        try {
            networkAccountId = data.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void willDelete(){}

    public void postUpdate(SocialActivity activity){
        FacebookPostTask task = new FacebookPostTask();
        task.prepare(activity);
        task.execute();
    }

    public class FacebookPostTask extends AsyncTask<String, Long, Boolean> {

        private SocialActivity activity;

        public void prepare(SocialActivity activity) {
            Log.d("CordovaPostcard", "Preparing Facebook Network Activity Post Task");
            this.activity = activity;
        }

        protected Boolean doInBackground(String... urls) {
            try {
                adaptor.setupService("facebook");
                String postUrl = "https://graph.facebook.com/me/feed";
                Token accessToken = new Token( data.getString("token"), data.getString("tokenSecret"));
                ProgressOAuthRequest request = null;

                // if activity.messageLink == null &&
                if ( activity.messageMedia != null && activity.messageMedia.imageLocation != null) {
                    Log.d("Postcard", "sending a picture message to Twitter");
                    postUrl = "https://graph.facebook.com/me/photos";
                    request = new ProgressOAuthRequest(Verb.POST, postUrl);
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addTextBody("message", activity.message);
                    //TODO: Needs type maybe? i.e. image/jpg
                    Log.d("Postcard", "image location -> " + activity.messageMedia.imageLocation);
                    builder.addBinaryBody("picture", new File(activity.messageMedia.imageLocation.replace("file://", "")));
                    HttpEntity entity = builder.build();

                    // Now, pull out the contents of everything you've added and set it as the payload
                    ByteArrayOutputStream bos = new ByteArrayOutputStream((int) entity.getContentLength());
                    entity.writeTo(bos);
                    request.addPayload(bos.toByteArray());
                    request.addHeader(entity.getContentType().getName(), entity.getContentType().getValue());

                } else {
                    request = new ProgressOAuthRequest(Verb.POST, postUrl);
                    request.addBodyParameter("message", activity.message);
                }



                adaptor.oAuthService.signRequest(accessToken, request);
                request.uploadProgressCallback = new FacebookPostUploadProgress();

                Response response = request.send();
                System.out.println(response.getBody());
                return true;
            } catch (Exception exception) {
                Log.d("CordovaPostcard", exception.getMessage());
                return null;
            }
        }

        protected void onProgressUpdate(Long... progress) {
            Log.d("CordovaPostcard", "Downloaded bytes: " + progress[0]);
        }

        protected void onPostExecute(Boolean success) {
            if (success != null){
                Log.d("CordovaPostcard", "Successful post");
                adaptor.postUIMessage("networkPostComplete", "{id:" + networkAccountId + "}");
            }
            else
                Log.d("CordovaPostcard", "Posting failed");
        }
    }

    public class FacebookPostUploadProgress implements UploadProgressCallback {
        public void progress(double progress){
            adaptor.postUIMessage("networkProgress", "{id:" + networkAccountId + ", progress:" + progress + "}");
        }
    }


}

/*
*


- (void)buildActivityParameters {
    ACAccountType *facebookAccountType = [_accountsManager.accountStore
            accountTypeWithAccountTypeIdentifier:ACAccountTypeIdentifierFacebook];
    // Specify App ID and permissions
    NSDictionary *options = @{
            ACFacebookAppIdKey : FACEBOOK_APP_KEY,
            ACFacebookPermissionsKey : @[@"publish_stream", @"publish_actions"],
            ACFacebookAudienceKey : ACFacebookAudienceEveryone
    };

    [_accountsManager.accountStore requestAccessToAccountsWithType:facebookAccountType
                                                           options:options completion:^(BOOL granted, NSError *e) {
        if (granted) {
            NSArray *accounts = [_accountsManager.accountStore
                    accountsWithAccountType:facebookAccountType];
            self.facebookAccount = [accounts lastObject];
            self.currentParameters = [NSMutableDictionary dictionary];

            NSString *accountId;
            BWLog(@"Credentials -- Account ID %@ Token %@", _accountId, _token);
            if (_accountId != nil) {
                //Is a FB Page
                accountId = _accountId;
                _currentParameters[@"access_token"] = _token;
            } else {
                //Is a personal FB feed
                accountId = [[self.facebookAccount valueForKey:@"properties"] valueForKey:@"uid"];
                switch(self.privacyLevel.integerValue){
                    case FacebookPrivacyLevelMe:
                        _currentParameters[@"privacy"] = @"{\"value\":\"SELF\"}";
                        break;

                    case FacebookPrivacyLevelFriends:
                        _currentParameters[@"privacy"] = @"{\"value\":\"ALL_FRIENDS\"}";
                        break;

                    case FacebookPrivacyLevelExtended:
                        _currentParameters[@"privacy"] = @"{\"value\":\"FRIENDS_OF_FRIENDS\"}";
                        break;

                    case FacebookPrivacyLevelPublic:
                    default:
                        _currentParameters[@"privacy"] = @"{\"value\":\"EVERYONE\"}";
                        break;
                }
            }

            self.currentRequestUrl = [NSString stringWithFormat:@"https://graph.facebook.com/%@/feed", accountId];
            _currentParameters[@"message"] = (_shouldRemoveLink.boolValue) ? _currentActivity.message : _currentActivity.messageWithLink;
            if (_currentActivity.messageLink) {
                _currentParameters[@"link"] = _currentActivity.messageLink.url;
            } else if (_currentActivity.messageMedia.videoData) {
                self.currentRequestUrl = [NSString stringWithFormat:@"https://graph.facebook.com/%@/videos", accountId];
                [_currentParameters removeObjectForKey:@"message"];
                _currentParameters[@"title"] = _currentActivity.message;
                _currentParameters[@"description"] = _currentActivity.message;
                _currentParameters[@"contentType"] = @"video/mp4";
            } else if (_currentActivity.messageMedia.image) {
                self.currentRequestUrl = [NSString stringWithFormat:@"https://graph.facebook.com/%@/photos", accountId];
            }

            BWLog(@"Facebook parameters -> %@", _currentParameters);

            [self buildActivityRequest];
        }
        else {
            BWLog(@"FacebookNetwork post error-> %@", [e description]);
            [[NetworksManager sharedInstance] network:self didFailWithError:e];
        }
    }];
}

- (void)buildActivityRequest {
    NSURL *feedURL = [NSURL URLWithString:_currentRequestUrl];
    self.currentActivityRequest = [SLRequest
            requestForServiceType:SLServiceTypeFacebook
                    requestMethod:SLRequestMethodPOST
                              URL:feedURL
                       parameters:_currentParameters];


    if (_currentActivity.messageLink == nil && _currentActivity.messageMedia.videoData) {
        [_currentActivityRequest addMultipartData:_currentActivity.messageMedia.videoData
                                         withName:@"video.mp4"
                                             type:@"video/mp4"
                                         filename:@"video.mp4"];
    } else if (_currentActivity.messageLink == nil && _currentActivity.messageMedia.imageData) {
        [_currentActivityRequest addMultipartData:_currentActivity.messageMedia.imageData withName:@"picture" type:@"image/jpg" filename:nil];
    }

    if (_accountId == nil) {
        _currentActivityRequest.account = self.facebookAccount;
    }

    [self sendActivityRequest];
}

- (void)sendActivityRequest {
    AFJSONRequestOperation *operation = [[AFJSONRequestOperation alloc] initWithRequest:_currentActivityRequest.preparedURLRequest];
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *op, id responseObject) {
        BWNetLog(@"FacebookNetwork message response %@", responseObject);
        [_currentActivity.postIds setValue:[responseObject valueForKey:@"id"] forKey:@"facebook"];
        NSString *accountId;
        if (_accountId != nil) {
            accountId = _accountId;
        } else {
            accountId = [[self.facebookAccount valueForKey:@"properties"] valueForKey:@"uid"];
        }
        NSString *postId = [responseObject[@"id"] stringByReplacingOccurrencesOfString:[NSString stringWithFormat:@"%@_", accountId]
                                                                            withString:@""];
        [[NetworksManager sharedInstance] network:self didCompletePostingWithInfo:@{
                @"id" : postId,
                @"permalink" : [NSString stringWithFormat:@"http://www.facebook.com/%@/posts/%@", accountId, postId]
        }];
        self.currentActivity = nil;
        self.currentRequestUrl = nil;
        self.currentActivityRequest = nil;
        self.currentParameters = nil;
    }                                failure:^(AFHTTPRequestOperation *op, NSError *failure) {

        NSError *parseError;
        NSDictionary *errorJson = [NSJSONSerialization JSONObjectWithData:op.responseData options:NSJSONReadingMutableContainers error:&parseError];

        BOOL handledError = NO;
        if (parseError == nil && [[errorJson valueForKeyPath:@"error.code"] isEqualToNumber:@1500]) {
            BWNetLog(@"Facebook URL Error, retrying with link in message");
            [_currentParameters removeObjectForKey:@"link"];
            _currentParameters[@"message"] = _currentActivity.messageWithLink;
            [self buildActivityRequest];
            handledError = YES;
        } else {
            BWNetLog(@"Operation failed -- status code %d - \n %@ \n %@", op.response.statusCode, [op response].allHeaderFields, [op responseString]);
            [[NetworksManager sharedInstance] network:self didFailWithError:failure];
            self.currentActivity = nil;
            self.currentRequestUrl = nil;
            self.currentActivityRequest = nil;
            self.currentParameters = nil;
        }
        [Flurry logEvent:@"Faceboook Post Error" withParameters:@{
                @"response" : op.responseString,
                @"wasHandled" : @(handledError)
        }];
    }];

    [operation setUploadProgressBlock:^(NSUInteger bytesWritten, long long totalBytesWritten, long long totalBytesExpectedToWrite) {
        BWNetLog(@"Sent %lld of %lld bytes", totalBytesWritten, totalBytesExpectedToWrite);
        double fraction = (double) totalBytesWritten / totalBytesExpectedToWrite;
        fraction *= 0.9;
        [[NetworksManager sharedInstance] network:self updatedProgress:fraction];
    }];
    [operation start];
}

* */