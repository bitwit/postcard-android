package ca.bitwit.postcard.network;

/* Java */

import java.lang.Boolean;
import java.lang.Integer;
import java.util.Map;
import java.util.List;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/* Android */
import android.content.Context;
import android.util.Log;
import android.os.AsyncTask;

import ca.bitwit.postcard.PostcardAdaptor;
import org.json.JSONException;
import org.json.JSONObject;

/* Scribe */
import org.scribe.*;
import org.scribe.model.Request;
import org.scribe.model.Verb;

public class Network {

    public String name;
    public String UUID;

    /* network features */
    public int charLimit;
    public boolean usesTags;
    public boolean acceptsImages;
    public boolean acceptsVideo;
    public boolean canHostContent;

    /*  */
    public PostcardAdaptor adaptor;
    public JSONObject data;
    public int networkAccountId;
    public boolean isHost = false;

    public Network(PostcardAdaptor adaptor, JSONObject data) {
        this.adaptor = adaptor;
        this.data = data;
        this.usesTags = false;
        this.charLimit = 0;
        this.acceptsImages = false;
        this.acceptsVideo = false;
        this.UUID = "test-uuid";
        try{
            this.isHost = data.getBoolean("isHost");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String generateState(Integer length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String randomString = "";
        Random rnd = new Random();
        for (Integer i = 0; i < length; i++) {
            randomString += letters.charAt(rnd.nextInt(letters.length()));
        }
        return randomString;
    }

    public JSONObject grantAccess(){
        return null;
    }

    public void onCreate(){

    }

    public void onDestroy() {

    }

    public void postUpdate(SocialActivity activity) {

    }

    public class SocialActivityPostTask extends AsyncTask<String, Long, Boolean> {

        private String url;
        private Map<String, Object> params;

        public void prepare(String url, Map<String, Object> params) {
            Log.d("CordovaPostcard", "Preparing Social Activity Post Task");
            this.url = url;
            this.params = params;
        }

        protected Boolean doInBackground(String... urls) {


            try {
                Request request = new Request(Verb.POST, this.url);

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