package org.scribe.builder.api;

import org.scribe.extractors.*;
import org.scribe.model.*;

import org.scribe.oauth.OAuth20PostServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.*;

public class BufferApi extends DefaultApi20
{
    private static final String AUTHORIZE_URL = "https://bufferapp.com/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s";
    private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s";

    @Override
    public OAuthService createService(OAuthConfig config)
    {
        return new OAuth20PostServiceImpl(this, config);
    }

    @Override
    public String getAccessTokenEndpoint()
    {
        return "https://api.bufferapp.com/1/oauth2/token.json";
    }

    @Override
    public Verb getAccessTokenVerb()
    {
        return Verb.POST;
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config)
    {
        Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback. Facebook does not support OOB");

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