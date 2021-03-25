package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.Map;

class Auth0RefreshTokenCall extends Auth0Call<Auth0Token> {

    private final String mRefreshToken;

    Auth0RefreshTokenCall(final @NonNull Context context, final @NonNull String refreshToken) {
        super(context);
        mRefreshToken = refreshToken;
    }

    @Override
    protected String buildEndpoint() {
        return "/oauth/token";
    }

    @Override
    protected void buildRequest(Map<String, String> params) {
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", mRefreshToken);
    }

    @Override
    protected void onCompleted(int code, JSONObject json) {
        if (code == 200) {
            final Auth0Token token = new Auth0Token();
            try {
                token.accessToken = json.getString("access_token");
                token.refreshToken = mRefreshToken;
                token.idToken = json.getString("id_token");
                token.scope = json.optString("scope", "");
                token.expiresAt = System.currentTimeMillis() + (json.optInt("expires_in", 600) * 1000L);
            } catch (Exception e) {
                notifyFailure(new Exception("Invalid Auth0 token response", e));
                return;
            }
            notifyResponse(token);
        } else if (code == 403) {
            notifyResponse(null);
        } else {
            notifyFailure(new Exception("Unexpected status code " + code + " with body: " + json));
        }
    }

}
