package org.scribe.oauth;

import android.util.Log;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.*;

public class OAuth20PostServiceImpl extends OAuth20ServiceImpl{

    private static final String VERSION = "2.0";
    private final DefaultApi20 api;
    private final OAuthConfig config;

    public OAuth20PostServiceImpl(DefaultApi20 api, OAuthConfig config) {
        super(api, config);
        this.api = api;
        this.config = config;
    }

    @Override
    public Token getAccessToken(Token requestToken, Verifier verifier)
    {
        OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
        request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
        request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
        request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
        request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
        request.addBodyParameter("grant_type", "authorization_code");
        if(config.hasScope()) request.addBodyParameter(OAuthConstants.SCOPE, config.getScope());

        Log.d("Oauth20PostImple", request.getBodyContents());

        Response response = request.send();
        return api.getAccessTokenExtractor().extract(response.getBody());
    }

}
