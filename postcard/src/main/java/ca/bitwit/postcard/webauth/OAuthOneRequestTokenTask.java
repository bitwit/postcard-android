package ca.bitwit.postcard.webauth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import ca.bitwit.postcard.PostcardAdaptor;

public class OAuthOneRequestTokenTask extends AsyncTask<String, Long, Boolean> {

    private OAuthService service;
    private PostcardAdaptor plugin;
    private String verifierTitle;
    private String authUrl;

    public void prepare(OAuthService theService, PostcardAdaptor thePlugin, String theVerifierTitle) {
        service = theService;
        plugin = thePlugin;
        verifierTitle = theVerifierTitle;
    }

    protected Boolean doInBackground(String... urls) {
        Token requestToken = service.getRequestToken();
        plugin.requestToken = requestToken;
        authUrl = service.getAuthorizationUrl(requestToken);
        Log.d("CordovaWebAuth", "auth url ->" + authUrl);
        return true;
    }


    protected void onProgressUpdate(Long... progress) {

    }

    protected void onPostExecute(Boolean success) {
        if(success){
            plugin.activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(authUrl)));
        }
    }
}