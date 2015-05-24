package ca.bitwit.postcard.webauth;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.oauth.OAuthService;

import ca.bitwit.postcard.PostcardAdaptor;


public class OAuthTwoRequestTokenTask extends AsyncTask<String, Long, Boolean> {

     private OAuthService service;
     private PostcardAdaptor adaptor;
     private String verifierTitle;
     private String authUrl;

     public void prepare(OAuthService theService, PostcardAdaptor theAdaptor, String theVerifierTitle) {
         service = theService;
         adaptor = theAdaptor;
         verifierTitle = theVerifierTitle;
     }

    protected Boolean doInBackground(String... urls) {
        authUrl = service.getAuthorizationUrl(null);
        Log.d("CordovaWebAuth", "auth url ->" + authUrl);
        return true;
    }

    protected void onProgressUpdate(Long... progress) {

    }

    protected void onPostExecute(Boolean success) {
        if(success){
           Context context = adaptor.activity.getApplicationContext();
            Intent webActivity = new Intent(context, WebAuthActivity.class);

            Bundle bundle = new Bundle();
            JSONObject parameters = new JSONObject();
            try{
                parameters.put("auth_url", authUrl);
                parameters.put("auth_type", "oauth1");
                parameters.put("verifier_title", verifierTitle);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            bundle.putString("parameters", parameters.toString());
            webActivity.putExtras(bundle);

           adaptor.activity.startActivityForResult(webActivity, 100);
        }
    }

}