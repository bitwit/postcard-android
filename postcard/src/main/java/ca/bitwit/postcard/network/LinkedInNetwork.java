package ca.bitwit.postcard.network;

import ca.bitwit.postcard.PostcardAdaptor;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Boolean;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class LinkedInNetwork extends Network{

    public LinkedInNetwork(PostcardAdaptor adaptor, JSONObject data) {
        super(adaptor, data);
        this.initializeLinkedIn();
    }

    public void initializeLinkedIn(){

    }

    public JSONObject grantAccess(){
        String LINKEDIN_CLIENT_ID = "5xkynu4pltw6";
        String LINKEDIN_CLIENT_SECRET = "zKWWqJrJK6X1X2bA";

        String state = this.generateState(16);

        String redirectUri = "http://www.postcardsocial.net";
        String tokenUrl = "https://www.linkedin.com/uas/oauth2/";
        String tokenPath = "accessToken";
        String authorizationPath = "authorization";
        //String accessType = ["r_fullprofile", "r_network", "rw_nus"]
        String authUrl =
                tokenUrl +
                authorizationPath +
                "?response_type=code&client_id=" +
                LINKEDIN_CLIENT_ID +
                "&scope=" +
                "r_fullprofile%20r_network%20rw_nus" +
                "&state=" +
                state +
                "&redirect_uri=" +
                redirectUri;

        try{
            JSONObject tokenPostData = new JSONObject();
            tokenPostData.put("client_id", LINKEDIN_CLIENT_ID);
            tokenPostData.put("client_secret", LINKEDIN_CLIENT_SECRET);
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
        } catch (JSONException ex){
            ex.printStackTrace();
            return null;
        }
    }

}