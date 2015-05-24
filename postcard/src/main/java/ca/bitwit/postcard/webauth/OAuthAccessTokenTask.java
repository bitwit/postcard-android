package ca.bitwit.postcard.webauth;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import ca.bitwit.postcard.PostcardAdaptor;


public class OAuthAccessTokenTask extends AsyncTask<String, Long, Boolean> {

    private OAuthService service;
    private PostcardAdaptor plugin;
    private Token requestToken;
    private String verifier;
    private String authUrl;

    public void prepare(OAuthService theService, PostcardAdaptor thePlugin, Token theRequestToken, String theVerifier) {
        service = theService;
        plugin = thePlugin;
        requestToken = theRequestToken;
        verifier = theVerifier;
    }

    protected Boolean doInBackground(String... urls) {
        Verifier verifierObj = new Verifier(verifier);

        Log.d("CordovaWebAuth", service.toString());
        Log.d("CordovaWebAuth", verifierObj.toString());
        if(requestToken != null){
            Log.d("CordovaWebAuth", requestToken.toString());
        }

        Token accessToken = service.getAccessToken(requestToken, verifierObj);

        Log.d("OAuthAccessTokenTask", "original token ->" + accessToken);

        JSONObject networkObject = new JSONObject();

        try {
            networkObject.put("networkId", plugin.serviceName);
            networkObject.put("title", plugin.serviceName + " Network");
            networkObject.put("token", accessToken.getToken());
            networkObject.put("tokenSecret", accessToken.getSecret());
        } catch(JSONException ex){
            ex.printStackTrace();
        }

        plugin.accessToken = accessToken;
        plugin.createNetworkAccount(networkObject);

        return true;
    }

    protected void onProgressUpdate(Long... progress) {

    }

    protected void onPostExecute(Boolean success) {

    }
}