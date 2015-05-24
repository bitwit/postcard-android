package ca.bitwit.postcard.webauth;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.Boolean;
import java.lang.String;
import java.lang.System;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.json.JSONException;


public class WebAuthClient extends WebViewClient{

    JSONObject parameters;
    WebAuthActivity webAuthActivity;

    public WebAuthClient(JSONObject parameters, WebAuthActivity webAuthActivity){
        super();
        this.parameters = parameters;
        this.webAuthActivity = webAuthActivity;
    }

    public JSONObject parametersFromUrl(String urlString){
        String[] firstSplit = urlString.split("\\?");
        String query = firstSplit[1];
        String[] queryElements = query.split("&");
        try{
            JSONObject parameters = new JSONObject();
            for(int i = 0; i < queryElements.length; i++){
                String[] keyVal = queryElements[i].split("=");
                if(keyVal.length > 0){
                    String variableKey = keyVal[0];
                    String variableVal = "";
                    for(int j = 1; j < keyVal.length; j++){
                        variableVal += keyVal[j];
                        if(j != keyVal.length - 1){
                            variableVal += "=";
                        }
                    }
                    //TODO: May need url decode for value
                    parameters.put(variableKey, variableVal);
                }
            }
            Log.d("CordovaWebAuth", "queryelement count -> " + queryElements.length + " parameters -> " + parameters.toString());
            return parameters;
        } catch(JSONException ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void getAccessToken(String code){
        Log.d("CordovaWebAuth", "Getting access token");
        try {
            if(this.parameters.getString("auth_type").equals("oauth1")){
                onAccessTokenRequestComplete(true, code);
            } else {
                TokenRequestTask task = new TokenRequestTask();
                task.prepare(code, this.parameters, this);
                task.execute();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onAccessTokenRequestComplete(false, "JSON exception getting auth_type");
        }
    }

    public void onAccessTokenRequestComplete(Boolean success, String token){
        this.webAuthActivity.onWebAuthRequestCompleted(success, token);
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        boolean shouldOverride = super.shouldOverrideUrlLoading(view, url);
        Log.d("CordovaWebAuth", "shouldOverrideUrlLoading -> " + url);

        String verifierTitle;
        String verifierString;
        try{
            verifierTitle = parameters.getString("verifier_title");
            verifierString = verifierTitle + "=";
        } catch(JSONException ex){
            ex.printStackTrace();
            return shouldOverride;
        }

        if(url.contains(verifierString)){
            Log.d("CordovaWebAuth", "Verifier response found");
            JSONObject responseParams = this.parametersFromUrl(url);
            try{
                String verifier = responseParams.getString(verifierTitle);
                onAccessTokenRequestComplete(true, verifier);
            } catch(JSONException ex){
                ex.printStackTrace();
                return shouldOverride;
            }
            return true;
        }

        return shouldOverride;
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Log.d("CordovaWebAuth", "onPageStarted -> " + url);
    }

    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Log.d("CordovaWebAuth", "onPageFinished -> " + url);
    }

    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        Log.d("CordovaWebAuth", "onLoadResource -> " + url);
    }

    public void onReceivedError( WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        Log.d("CordovaWebAuth", "onReceivedError -> " + description);
    }

    public class TokenRequestTask extends AsyncTask<String, Long, Boolean> {

        private String code;
        private JSONObject parameters;
        private WebAuthClient webClient;
        private String token; //the final token

        public void prepare(String code, JSONObject parameters, WebAuthClient webClient){
            this.code = code;
            this.parameters = parameters;
            this.webClient = webClient;
        }

        protected Boolean doInBackground(String... urls) {

            HttpClient httpClient = new DefaultHttpClient();

            String method;
            String baseUrl;
            String path;
            StringEntity body;
            ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();

            try{
                method = this.parameters.getString("token_method");
                baseUrl = this.parameters.getString("base_token_url");
                path = this.parameters.getString("token_path");
                JSONObject tokenData = this.parameters.getJSONObject("token_data");
                tokenData.put(this.parameters.getString("code_title"), URLDecoder.decode(this.code, "UTF-8"));
                String postData = tokenData.toString();

                Iterator<String> iterator = tokenData.keys();

                while(iterator.hasNext()){
                    String key = iterator.next();
                    String val = tokenData.getString(key);
                    parameters.add(new BasicNameValuePair(key,val));
                }

            } catch(JSONException ex){
                ex.printStackTrace();
                return false;
            } catch (UnsupportedEncodingException ex){
                ex.printStackTrace();
                return false;
            }

            if(method.equals("post")){
                HttpPost httpPost = new HttpPost(baseUrl + path);
                httpPost.setHeader("Accept", "application/json");

                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(parameters));
                    HttpResponse response = httpClient.execute(httpPost);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";
                    String responseString = "";
                    while ((line = rd.readLine()) != null) {
                        System.out.println(line);
                        responseString += line;
                    }

                    JSONObject responseObject;
                    try{
                        responseObject = new JSONObject(responseString);
                        this.token = responseObject.getString(this.parameters.getString("token_title"));
                    }catch (JSONException ex){
                        ex.printStackTrace();
                        return false;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return false;
                }
            } else {

                String url = baseUrl + path;
                String paramString = URLEncodedUtils.format(parameters, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                Log.d("CordovaWebAuth", "get url -> " + url);

                try {
                    HttpResponse response = httpClient.execute(httpGet);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";
                    String responseString = "";
                    while ((line = rd.readLine()) != null) {
                        System.out.println(line);
                        responseString += line;
                    }

                    JSONObject responseObject;
                    try{
                        Uri uri = Uri.parse("http://www.fake.com/?" + responseString);
                        String token = uri.getQueryParameter(this.parameters.getString("token_title"));
                        Log.d("CordovaWebAuth", "token -> " + token);
                        this.token = token; // responseObject.getString(this.parameters.getString("token_title"));
                    }catch (JSONException ex){
                        ex.printStackTrace();
                        return false;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }


            return true;

        }

        protected void onProgressUpdate(Long... progress) {
            Log.d("CordovaPostcard", "Downloaded bytes: " + progress[0]);
        }

        protected void onPostExecute(Boolean success) {
            this.webClient.onAccessTokenRequestComplete(success, this.token);
        }
    }

}