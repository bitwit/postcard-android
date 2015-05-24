
package ca.bitwit.postcard.network;

import ca.bitwit.postcard.PostcardAdaptor;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Boolean;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class BufferNetwork extends Network {

    public BufferNetwork(PostcardAdaptor adaptor, JSONObject data) {
        super(adaptor, data);
        this.initializeBuffer();
    }

    public void initializeBuffer() {

    }

    public JSONObject grantAccess() {
        String BUFFER_CLIENT_ID = "52374e39ef17dc2e21000029";
        String BUFFER_CLIENT_SECRET = "5ba2ab9e9166e3a5785b9aab7cd44360";

        String state = this.generateState(16);

        String redirectUri = "http://www.postcardsocial.net";
        String tokenUrl = "https://api.bufferapp.com/1/oauth2/";
        String tokenPath = "token.json";
        String authorizationPath = "authorization";
        String authUrl = "https://bufferapp.com/oauth2/authorize?response_type=code" +
                "&client_id=" +
                BUFFER_CLIENT_ID +
                "&redirect_uri=" +
                redirectUri;

        try {
            JSONObject tokenPostData = new JSONObject();
            tokenPostData.put("client_id", BUFFER_CLIENT_ID);
            tokenPostData.put("client_secret", BUFFER_CLIENT_SECRET);
            tokenPostData.put("redirect_uri", redirectUri);
            tokenPostData.put("grant_type", "authorization_code");

            JSONObject params = new JSONObject();
            params.put("auth_url", authUrl);
            params.put("code_title", "code");
            params.put("base_token_url", tokenUrl);
            params.put("token_path", tokenPath);
            params.put("token_method", "post");
            params.put("token_data", tokenPostData);
            params.put("token_title", "access_token");
            params.put("state", state);

            return params;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}