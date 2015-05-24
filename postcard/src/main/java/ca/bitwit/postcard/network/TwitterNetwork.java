
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

/* Scribe */


public class TwitterNetwork extends Network {

    public TwitterNetwork(PostcardAdaptor adaptor, JSONObject data) {
        super(adaptor, data);
        this.name = "twitter";
        this.charLimit = 140;
        this.initializeTwitter();
    }

    public void initializeTwitter() {
        try {
            networkAccountId = data.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onCreate(){
        TwitterCredentialsTask task = new TwitterCredentialsTask();
        task.prepare(this);
        task.execute();
    }

    public void postUpdate(SocialActivity activity) {
        TwitterPostTask task = new TwitterPostTask();
        task.prepare(activity);
        task.execute();
    }

    public void synchronizePeople(String cursor) {
        TwitterPeopleSyncTask task = new TwitterPeopleSyncTask();
        task.prepare(cursor);
        task.execute();

    }

    public class TwitterCredentialsTask extends AsyncTask<String, Long, Boolean> {

        private Network network;

        public void prepare(Network network) {
            this.network = network;
        }

        protected Boolean doInBackground(String... urls) {
            try {
                String requestUrl = "https://api.twitter.com/1.1/account/verify_credentials.json";
                adaptor.setupService("twitter");
                OAuthRequest request = new OAuthRequest(Verb.GET, requestUrl);
                Token accessToken = null;

                try {
                    accessToken = new Token(data.getString("token"), data.getString("tokenSecret"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adaptor.oAuthService.signRequest(accessToken, request);
                Response response = request.send();
                Log.d(this.getClass().getName(),"Credentials:");
                System.out.println(response.getBody());
                JSONObject responseObject = new JSONObject(response.getBody());
                network.data.put("title", "@" + responseObject.getString("screen_name"));
                adaptor.networkAccountCreationComplete(network);
                return true;
            } catch (Exception exception) {
                Log.d(this.getClass().getName(), exception.getMessage());
                return null;
            }
        }

        protected void onProgressUpdate(Long... progress) {
        }

        protected void onPostExecute(Boolean success) {
            if (success != null && success){
                Log.d(this.getClass().getName(), "Successful credentials grab");
            }
            else
                Log.d(this.getClass().getName(), "Credentials call failed");
        }
    }

    public class TwitterPeopleSyncTask extends AsyncTask<String, Long, Boolean> {
        private String cursor;

        public void prepare(String cursor) {
            this.cursor = cursor;
        }

        protected Boolean doInBackground(String... urls) {
            try {
                String requestUrl = "https://api.twitter.com/1.1/friends/list.json";

                adaptor.setupService("twitter");
                OAuthRequest request = new OAuthRequest(Verb.POST, requestUrl);
                Token accessToken = null;
                try {
                    accessToken = new Token(data.getString("token"), data.getString("tokenSecret"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                request.addBodyParameter("count", "200");
                request.addBodyParameter("skip_status", "1");

                if (cursor != null) {
                    request.addBodyParameter("cursor", cursor);
                }

                adaptor.oAuthService.signRequest(accessToken, request);
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
            if (success != null && success){
                Log.d("CordovaPostcard", "Sucessful post");
            }
            else
                Log.d("CordovaPostcard", "Posting failed");
        }
    }

    public class TwitterPostTask extends AsyncTask<String, Long, Boolean> {

        private SocialActivity activity;

        public void prepare(SocialActivity activity) {
            Log.d("CordovaPostcard", "Preparing Twitter Network Activity Post Task");
            this.activity = activity;
        }

        protected Boolean doInBackground(String... urls) {
            try {
                adaptor.setupService("twitter");

                String postUrl = "https://api.twitter.com/1.1/statuses/update.json";

                Token accessToken = new Token(data.getString("token"), data.getString("tokenSecret"));

                String message = activity.message;
                ProgressOAuthRequest request = null;


                //if (activity.messageLink != null) {
                //    message = activity.messageWithLinkFittingCharacterLimit(charLimit);
                //}

                //if (activity.isFromAHost && activity.hostNetwork.equals("twitter")) {
                //    postUrl = "https://api.twitter.com/1.1/statuses/retweet/" + activity.hostId + ".json";
                //    request = new OAuthRequest(Verb.POST, postUrl);
                //} else

                // if activity.messageLink == null &&
                if ( activity.messageMedia != null && activity.messageMedia.imageLocation != null) {
                    Log.d("Postcard", "sending a picture message to Twitter");
                    postUrl = "https://api.twitter.com/1.1/statuses/update_with_media.json";
                    request = new ProgressOAuthRequest(Verb.POST, postUrl);
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addTextBody("status", activity.message);
                    //TODO: Needs type maybe? i.e. image/jpg
                    Log.d("Postcard", "image location -> " + activity.messageMedia.imageLocation);
                    builder.addBinaryBody("media[]", new File(activity.messageMedia.imageLocation.replace("file://", "")));
                    HttpEntity entity = builder.build();

                    // Now, pull out the contents of everything you've added and set it as the payload
                    ByteArrayOutputStream bos = new ByteArrayOutputStream((int) entity.getContentLength());
                    entity.writeTo(bos);
                    request.addPayload(bos.toByteArray());
                    request.addHeader(entity.getContentType().getName(), entity.getContentType().getValue());

                } else {
                    Log.d("Postcard", "Is just a text status - " + message);
                    request = new ProgressOAuthRequest(Verb.POST, postUrl);
                    request.addBodyParameter("status", message);
                }

                adaptor.oAuthService.signRequest(accessToken, request);
                request.uploadProgressCallback = new TwitterPostUploadProgress();

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
                Log.d("CordovaPostcard", "Sucessful post");
                adaptor.postUIMessage("networkPostComplete", "{id:" + networkAccountId + "}");
            }
            else
                Log.d("CordovaPostcard", "Posting failed");
        }
    }

    public class TwitterPostUploadProgress implements UploadProgressCallback {
        public void progress(double progress){
            adaptor.postUIMessage("networkProgress", "{id:" + networkAccountId + ", progress:" + progress + "}");
        }
    }

}

/*
*
*
- (void)postUpdate:(PCSocialActivity *)activity {
    [super postUpdate:activity];

    SLRequest *postRequest;
    if (activity.isFromAHost && activity.hostNetwork == self.tag) {
        NSString *stringUrl = [NSString stringWithFormat:@"https://api.twitter.com/1.1/statuses/retweet/%@.json", activity.hostId];
        NSURL *requestURL = [NSURL URLWithString:stringUrl];
        postRequest = [SLRequest
                requestForServiceType:SLServiceTypeTwitter
                        requestMethod:SLRequestMethodPOST
                                  URL:requestURL parameters:nil];

    } else {
        NSURL *requestURL = [NSURL URLWithString:@"https://api.twitter.com/1.1/statuses/update.json"];
        NSString *message = activity.message;

        if (activity.messageLink) {
            message = [activity messageWithLinkFittingCharacterLimit:(NSUInteger) self.charLimit];
        } else if (activity.messageMedia.image) {
            requestURL = [NSURL URLWithString:@"https://api.twitter.com/1.1/statuses/update_with_media.json"];
        }

        postRequest = [SLRequest
                requestForServiceType:SLServiceTypeTwitter
                        requestMethod:SLRequestMethodPOST
                                  URL:requestURL parameters:@{@"status" : message}];

        if (activity.messageLink == nil && activity.messageMedia.imageData) {
            [postRequest addMultipartData:activity.messageMedia.imageData withName:@"media[]" type:@"image/jpg" filename:nil];
        }
    }

    postRequest.account = _twitterAccount;

    AFJSONRequestOperation *operation = [[AFJSONRequestOperation alloc] initWithRequest:postRequest.preparedURLRequest];
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *op, id responseObject) {
        BWNetLog(@"Twitter Network message response %@", responseObject);
        [[NetworksManager sharedInstance] network:self didCompletePostingWithInfo:@{
                @"id" : responseObject[@"id_str"],
                @"permalink" : [NSString stringWithFormat:@"https://twitter.com/%@/status/%@", responseObject[@"user"][@"screen_name"], responseObject[@"id_str"]]
        }];
    }                                failure:^(AFHTTPRequestOperation *op, NSError *failure) {
        BWNetLog(@"Twitter Network error - %@ %@", failure, op.responseData.stringWithoutURLEncoding);
        NSError *responseError = [NSError errorWithDomain:failure.description code:op.response.statusCode userInfo:nil];
        [Flurry logEvent:@"Twitter Post Error" withParameters:@{
                @"response" : (op.responseString != nil) ? op.responseString : @"(Null response)",
                @"error": failure.localizedDescription,
                @"wasHandled" : @(NO)
        }];
        @try {
            id responseObject = [NSJSONSerialization JSONObjectWithData:op.responseData options:0 error:nil];
            [[NetworksManager sharedInstance] network:self didFailWithError:responseError];
            [self handlePostingErrorCode:[responseObject[@"errors"][0][@"code"] integerValue] withSuccessSelector:nil];
        }
        @catch (NSException *e) {
            BWLog(@"Couldn't evaluate error from twitter");
            [[NetworksManager sharedInstance] network:self didFailWithError:failure];
        }
    }];

    [operation setUploadProgressBlock:^(NSUInteger bytesWritten, long long totalBytesWritten, long long totalBytesExpectedToWrite) {
        BWNetLog(@"Sent %lld of %lld bytes", totalBytesWritten, totalBytesExpectedToWrite);
        double fraction = (double) totalBytesWritten / totalBytesExpectedToWrite;
        fraction *= 0.9;
        [[NetworksManager sharedInstance] network:self updatedProgress:fraction];
    }];
    [operation start];
}
*
* */