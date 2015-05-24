
package ca.bitwit.postcard.network;

import android.os.AsyncTask;
import android.util.Log;

import ca.bitwit.postcard.PostcardAdaptor;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import java.lang.Boolean;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.ByteArrayOutputStream;

/* Scribe */
import org.scribe.*;
import org.scribe.model.Request;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import org.apache.http.*;
import org.apache.http.entity.mime.MultipartEntityBuilder;


public class CustomNetwork extends Network{
    public String siteUrl;
    public String username;
    public String password;

    public String token;
    private Boolean shouldRemoveLink;
    private String placeholderTitle;

    public CustomNetwork(PostcardAdaptor adaptor, JSONObject data){
        super(adaptor, data);
        this.shouldRemoveLink = true;
        this.initializeCustomNetwork();
    }

    public void initializeCustomNetwork(){
        this.name = "custom";
        this.usesTags = true;
        this.acceptsImages = true;
        this.acceptsVideo = true;
        String tokenName = this.UUID + "_" + "custom_access_token";
        //TODO: Get access token
    }

    public void willDelete(){}

    public void loginWithCredentials(String user, String pass){
       String body = "username=" + user + "&password=" + pass;
       String urlString = this.siteUrl + "authenticate";

       //TODO: Create request here

    }

    public void postUpdate(SocialActivity activity){
        String endpoint = "post/add";
        if(activity.messageMedia != null){
            endpoint = "post/add_with_media";
        }
        CustomNetworkPostTask task = new CustomNetworkPostTask();
        task.prepare(this.siteUrl + "&endpoint=" + endpoint, activity, this.token);
        task.execute();
    }

    public class CustomNetworkPostTask extends AsyncTask<String, Long, Boolean> {

        private String url;
        private SocialActivity activity;
        private String token;

        public void prepare(String url, SocialActivity activity, String token) {
            Log.d("CordovaPostcard", "Preparing Custom Network Activity Post Task");
            this.url = url;
            this.activity = activity;
            this.token = token;
        }

        protected Boolean doInBackground(String... urls) {
            try {

                Request request = new Request(Verb.POST, this.url);
                if(activity.messageMedia != null){

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

                    /* example for setting a HttpMultipartMode */
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addTextBody("message", activity.message);
                    builder.addTextBody("date", activity.date);
                    builder.addTextBody("token", this.token);
                    builder.addBinaryBody("image", new File(activity.messageMedia.imageLocation));

                    HttpEntity entity = builder.build();

                    // Now, pull out the contents of everything you've added and set it as the payload
                    ByteArrayOutputStream bos = new ByteArrayOutputStream((int)entity.getContentLength());
                    entity.writeTo(bos);
                    request.addPayload(bos.toByteArray());

                } else {
                    request.addBodyParameter("message", activity.message);
                    request.addBodyParameter("date", activity.date);
                    request.addBodyParameter("token", activity.date);
                }

                Response response = request.send();
                System.out.println(response.getBody());

                return true;

            } catch (Exception exception) {
                Log.d("CordovaPostcard", exception.getMessage());
                return null;
            }

            /*
            try {
                HttpRequest request = HttpRequest.post(this.url);
                request.form(this.params);
                Log.d("Postcard", "response -> " + request.body());
                if (request.ok()) {
                    //file = File.createTempFile("download", ".tmp");
                    //request.receive(file);
                    //publishProgress(file.length());
                    return true;
                } else {
                    return null;
                }
            } catch (HttpRequestException exception) {
                return null;
            }
            */
        }

        protected void onProgressUpdate(Long... progress) {
            Log.d("CordovaPostcard", "Downloaded bytes: " + progress[0]);
        }

        protected void onPostExecute(Boolean success) {
            if (success != null)
                Log.d("CordovaPostcard", "Sucessful post");
            else
                Log.d("CordovaPostcard", "Posting failed");
        }
    }

}