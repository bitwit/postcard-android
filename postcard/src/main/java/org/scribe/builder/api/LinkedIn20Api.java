package org.scribe.builder.api;

import org.scribe.extractors.*;
import org.scribe.model.*;

import org.scribe.oauth.OAuth20PostServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.*;

/**
 *

 String redirectUri = "http://www.postcardsocial.net";
 String tokenUrl = "https://www.linkedin.com/uas/oauth2/";
 String tokenPath = "accessToken";
 String authorizationPath = "authorization";
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

 try {
 JSONObject tokenPostData = new JSONObject();
 tokenPostData.put("client_id", LINKEDIN_CLIENT_ID);
 tokenPostData.put("client_secret", LINKEDIN_CLIENT_SECRET);
 tokenPostData.put("redirect_uri", redirectUri);
 tokenPostData.put("grant_type", "authorization_code");

 JSONObject params = new JSONObject();
 params.put("auth_url", authUrl);
 params.put("auth_type", "oauth2");
 params.put("code_title", "code");
 params.put("base_token_url", tokenUrl);
 params.put("token_path", tokenPath);
 params.put("token_method", "post");
 params.put("token_data", tokenPostData);
 params.put("token_title", "access_token");
 params.put("state", state);
 launchWebAuthActivity(params);
 } catch (JSONException ex) {
 ex.printStackTrace();
 }

 *
 */

public class LinkedIn20Api extends DefaultApi20
{
    private static final String AUTHORIZE_URL = "https://www.linkedin.com/uas/oauth2/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=1234";
    private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=r_fullprofile%20r_network%20rw_nus"; //+ "&scope=%s";

    @Override
    public OAuthService createService(OAuthConfig config)
    {
        return new OAuth20PostServiceImpl(this, config);
    }

    @Override
    public String getAccessTokenEndpoint()
    {
        return "https://www.linkedin.com/uas/oauth2/accessToken";
    }

    @Override
    public Verb getAccessTokenVerb()
    {
        return Verb.POST;
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config)
    {
        Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback. LinkedIn does not support OOB");

        // Append scope if present
        if(config.hasScope())
        {
            return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope()));
        }
        else
        {
            return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
        }
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor()
    {
        return new JsonTokenExtractor();
    }
}