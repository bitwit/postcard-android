/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package ca.bitwit.postcard;

import android.app.ActionBar;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import ca.bitwit.postcard.camera.CameraHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

import java.lang.Exception;
import java.lang.Override;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Properties;

/* Android */
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.net.Uri;

/* Scribe */
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.BufferApi;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.LinkedIn20Api;
import org.scribe.builder.api.TumblrApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

/* Postcard classes */
import ca.bitwit.postcard.data.*;
import ca.bitwit.postcard.network.*;
import ca.bitwit.postcard.webauth.*;

public class PostcardAdaptor {

    public Properties properties;

    public NetworkAccountDataSource networkAccountDataSource;
    public PersonDataSource personDataSource;
    public TagDataSource tagDataSource;

    public OAuthService oAuthService = null;
    public String serviceName = null;
    public Token requestToken = null;
    public Token accessToken = null;

    public Activity activity;
    public WebView webView;

    public CameraHandler camera;

    /**
     * Cordova Plugin Overrides
     */

    public PostcardAdaptor(Activity activity, WebView webView) {
        Log.d("PostcardApplication", "Initialized");
        NetworksManager.INSTANCE.adaptor = this;
        this.activity = activity;
        this.webView = webView;

        Context context = activity.getApplicationContext();

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "PostcardApplication");
        webView.loadUrl("file:///android_asset/www/index.html");

        this.properties = new Properties();
        String propFileName = "postcard.properties";

        try {
            InputStream inputStream = context.getAssets().open(propFileName);
            if (inputStream != null) {
                this.properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.networkAccountDataSource = new NetworkAccountDataSource(context);
        this.networkAccountDataSource.open();

        this.personDataSource = new PersonDataSource(context);
        this.personDataSource.open();

        this.tagDataSource = new TagDataSource(context);
        this.tagDataSource.open();
    }

    /**
     * Postcard Specific methods
     */

    @JavascriptInterface
    public void showBackButton() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ActionBar actionBar = activity.getActionBar();
                if(actionBar != null){
                    actionBar.setHomeButtonEnabled(true);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        });
    }

    @JavascriptInterface
    public void hideBackButton() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ActionBar actionBar = activity.getActionBar();
                if(actionBar != null){
                    actionBar.setHomeButtonEnabled(false);
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
            }
        });
    }

    @JavascriptInterface
    public void loadNetworkAccounts() {
        try {
            final JSONArray jsonArray = this.networkAccountDataSource.getAllNetworkAccounts();
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:window.PostcardUI.networkAccountsDidChange(" + jsonArray.toString() + ");");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void loadNetworks() {
        try {
            String jsonString = this.loadJSONFile("www/data/networks.json");
            final JSONArray jsonArray = new JSONArray(jsonString);
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:window.PostcardUI.networksDidChange(" + jsonArray.toString() + ");");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void updateNetworkAccount(String networkAccountData){
        JSONObject networkAccount;
        try {
            networkAccount = new JSONObject(networkAccountData);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        this.networkAccountDataSource.updateNetworkAccount(networkAccount);
    }

    @JavascriptInterface
    public void deleteNetworkAccount(String networkAccountData){
        JSONObject networkAccount;
        try {
            networkAccount = new JSONObject(networkAccountData);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        this.networkAccountDataSource.deleteNetworkAccount(networkAccount);
    }

    @JavascriptInterface
    public void captureMedia(){
        if(camera == null){
            camera = new CameraHandler(this.activity);
        }
        camera.captureImage();
    }

    //TODO: Expose as JS Interface
    @JavascriptInterface
    public void postActivity(String data) {
        Log.d("Postcard", "post Activity -> " + data);

        JSONObject object;
        SocialActivity activity;
        try {
            object = new JSONObject(data);
            activity = new SocialActivity();
                activity.message = object.getString("message");
                activity.date = object.getString("date");
            Log.d("CordovaPostcard", "Json data pulled and set in object");

            if (object.has("messageMedia") && !object.isNull("messageMedia")) {
                Log.d("CordovaPostcard", "adding media");
                JSONObject messageMedia = object.getJSONObject("messageMedia");
                MessageMedia media = new MessageMedia();
                media.imageLocation = messageMedia.getString("imageLocation");
                activity.messageMedia = media;
            }

            Log.d("CordovaPostcard", "Done checking for media");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        try {
            Class<?> clazz = null;
            JSONArray networkAccounts = object.getJSONArray("networks");
            for (int i = 0; i < networkAccounts.length(); i++) {
                JSONObject networkAccount = networkAccounts.getJSONObject(i);
                String networkId = networkAccount.getString("networkId");
                String networkName = networkId.substring(0, 1).toUpperCase() + networkId.substring(1);
                clazz = Class.forName("ca.bitwit.postcard.network." + networkName + "Network");
                Constructor<?> constructor = clazz.getConstructor(PostcardAdaptor.class, JSONObject.class);
                Network instance = (Network)constructor.newInstance(this, networkAccount);
                activity.networks.add(instance);
            }
            NetworksManager.INSTANCE.postActivity(activity);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Log.d("CordovaPostcard", "Network postActivity Start");
    }

    /**
     * Oauth and network creation
     */

    @JavascriptInterface
    public void grantAccess(String networkName) {
        //Reset values for new network auth process
        serviceName = null;
        requestToken = null;
        accessToken = null;

        setupService(networkName);
        if (networkName.equals("linkedin")) {
            final String verifierTitle = "code";
            OAuthTwoRequestTokenTask task = new OAuthTwoRequestTokenTask();
            task.prepare(oAuthService, PostcardAdaptor.this, verifierTitle);
            task.execute();

        } else if (networkName.equals("buffer")) {
            final String verifierTitle = "code";
            OAuthTwoRequestTokenTask task = new OAuthTwoRequestTokenTask();
            task.prepare(oAuthService, PostcardAdaptor.this, verifierTitle);
            task.execute();
        } else if (networkName.equals("facebook")) {
            final String verifierTitle = "code";
            OAuthTwoRequestTokenTask task = new OAuthTwoRequestTokenTask();
            task.prepare(oAuthService, PostcardAdaptor.this, verifierTitle);
            task.execute();
        } else if (networkName.equals("tumblr")) {
            final String verifierTitle = "oauth_verifier";
            OAuthOneRequestTokenTask task = new OAuthOneRequestTokenTask();
            task.prepare(oAuthService, PostcardAdaptor.this, verifierTitle);
            task.execute();
        } else if (networkName.equals("twitter")) {
            final String verifierTitle = "oauth_verifier";
            OAuthOneRequestTokenTask task = new OAuthOneRequestTokenTask();
            task.prepare(oAuthService, PostcardAdaptor.this, verifierTitle);
            task.execute();
        }
    }

    public void setupService(String networkName) {
        if (networkName.equals("facebook")) {
            String FACEBOOK_CLIENT_ID = this.properties.getProperty("FACEBOOK_CLIENT_ID");
            String FACEBOOK_CLIENT_SECRET = this.properties.getProperty("FACEBOOK_CLIENT_SECRET");
            final String redirectUri = "http://www.postcardsocial.net/";
            oAuthService = new ServiceBuilder()
                    .provider(FacebookApi.class)
                    .apiKey(FACEBOOK_CLIENT_ID)
                    .apiSecret(FACEBOOK_CLIENT_SECRET)
                    .callback(redirectUri)
                    .build();
        } else if (networkName.equals("buffer")) {
            final String BUFFER_CLIENT_ID = this.properties.getProperty("BUFFER_CLIENT_ID");
            final String BUFFER_CLIENT_SECRET = this.properties.getProperty("BUFFER_CLIENT_SECRET");

            final String redirectUri = "http://www.postcardsocial.net";
            oAuthService = new ServiceBuilder()
                    .provider(BufferApi.class)
                    .apiKey(BUFFER_CLIENT_ID)
                    .apiSecret(BUFFER_CLIENT_SECRET)
                    .callback(redirectUri)
                    .build();
        } else if (networkName.equals("linkedin")) {
            String LINKEDIN_CLIENT_ID = this.properties.getProperty("LINKEDIN_CLIENT_ID");
            String LINKEDIN_CLIENT_SECRET = this.properties.getProperty("LINKEDIN_CLIENT_SECRET");
            final String redirectUri = "http://www.postcardsocial.net";
            oAuthService = new ServiceBuilder()
                    .provider(LinkedIn20Api.class)
                    .apiKey(LINKEDIN_CLIENT_ID)
                    .apiSecret(LINKEDIN_CLIENT_SECRET)
                    .callback(redirectUri)
                    .build();
        } else if (networkName.equals("tumblr")) {
            String TUMBLR_CLIENT_ID = this.properties.getProperty("TUMBLR_CLIENT_ID");
            String TUMBLR_CLIENT_SECRET = this.properties.getProperty("TUMBLR_CLIENT_SECRET");
            final String redirectUri = "postcard://auth_ok";

            oAuthService = new ServiceBuilder()
                    .provider(TumblrApi.class)
                    .apiKey(TUMBLR_CLIENT_ID)
                    .apiSecret(TUMBLR_CLIENT_SECRET)
                    .callback(redirectUri)
                    .build();
        } else if (networkName.equals("twitter")) {
            String TWITTER_CLIENT_ID = this.properties.getProperty("TWITTER_CLIENT_ID");
            String TWITTER_CLIENT_SECRET = this.properties.getProperty("TWITTER_CLIENT_SECRET");
            final String redirectUri = "postcard://auth_ok";

            oAuthService = new ServiceBuilder()
                    .provider(TwitterApi.Authenticate.class)
                    .apiKey(TWITTER_CLIENT_ID)
                    .apiSecret(TWITTER_CLIENT_SECRET)
                    .callback(redirectUri)
                    .build();
        }

        serviceName = networkName;
    }

    public void createNetworkAccount(JSONObject newNetworkAccount) {
        Log.d("CordovaPostcard", "Attempting to create a new network account");
        try {
            JSONObject newObject = this.networkAccountDataSource.createNetworkAccount(newNetworkAccount);
            Log.d("CordovaPostcard", newObject.toString());
            TwitterNetwork networkAccount = new TwitterNetwork(this, newObject);
            networkAccount.onCreate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void networkAccountCreationComplete(final Network network){
        networkAccountDataSource.updateNetworkAccount(network.data);
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:window.PostcardUI.networkAccountsDidChange(" + network.data.toString() + ");");
            }
        });
    }

    public void mediaSaved(final Uri fileUri, final String type){
        webView.post(new Runnable() {
            @Override
            public void run() {
            webView.loadUrl("javascript:window.PostcardUI.mediaAdded('" + fileUri + "', '" + type + "');");
            }
        });
    }

    public void postUIMessage(final String message, final String data){
        Log.d("PostcardAdaptor", "javascript:window.PostcardUI." + message + "(" + data + ");");
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:window.PostcardUI." + message + "(" + data + ");");
            }
        });
    }

    public String loadJSONFile(String file) {
        try {
            String jsonString;

            Context context = this.activity.getApplicationContext();

            InputStream is = context.getAssets().open(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");

            return jsonString;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}