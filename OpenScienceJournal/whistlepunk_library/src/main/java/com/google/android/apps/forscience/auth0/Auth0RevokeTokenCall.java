package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.Map;

class Auth0RevokeTokenCall extends Auth0Call<Void> {

    private final String mRefreshToken;

    Auth0RevokeTokenCall(final @NonNull Context context, final @NonNull String refreshToken) {
        super(context);
        mRefreshToken = refreshToken;
    }

    @Override
    protected String buildEndpoint() {
        return "/oauth/revoke";
    }

    @Override
    protected void buildRequest(Map<String, String> params) {
        params.put("token", mRefreshToken);
    }

    @Override
    protected void onCompleted(int code, JSONObject json) {
        if (code == 200) {
            notifyResponse(null);
        } else {
            notifyFailure(new Exception("Unexpected status code " + code + " with body: " + json));
        }
    }

}
