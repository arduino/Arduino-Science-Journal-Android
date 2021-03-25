package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.apps.forscience.utils.StringUtils;
import com.google.android.apps.forscience.whistlepunk.R;

import org.json.JSONObject;

import java.util.Map;

class Auth0LoginTokenCall extends Auth0Call<Auth0LoginTokenCall.Response> {

    private final String mAudience;

    private final String mScope;

    private final String mUsername;

    private final String mPassword;

    Auth0LoginTokenCall(final @NonNull Context context, final @NonNull String username, final @NonNull String password) {
        super(context);
        mAudience = context.getString(R.string.config_auth0_audience);
        mScope = context.getString(R.string.config_auth0_scope);
        mUsername = username;
        mPassword = password;
    }

    @Override
    protected String buildEndpoint() {
        return "/oauth/token";
    }

    @Override
    protected void buildRequest(Map<String, String> params) {
        if (mAudience != null) {
            params.put("audience", mAudience);
        }
        if (mScope != null) {
            params.put("scope", mScope);
        }
        params.put("grant_type", "password");
        params.put("username", mUsername);
        params.put("password", mPassword);
    }

    @Override
    protected void onCompleted(int code, JSONObject json) {
        if (code == 200) {
            final Auth0Token token = new Auth0Token();
            try {
                token.accessToken = json.getString("access_token");
                token.refreshToken = json.getString("refresh_token");
                token.idToken = json.getString("id_token");
                token.scope = json.optString("scope", "");
                token.expiresAt = System.currentTimeMillis() + (json.optInt("expires_in", 600) * 1000L);
            } catch (Exception e) {
                notifyFailure(new Exception("Invalid Auth0 token response", e));
                return;
            }
            final Response response = new Response();
            response.mfa = null;
            response.token = token;
            notifyResponse(response);
        } else if (code == 403) {
            final String mfa;
            final String error = json.optString("error", "");
            if ("mfa_required".equals(error)) {
                mfa = json.optString("mfa_token", "");
            } else {
                mfa = null;
            }
            if (StringUtils.isEmpty(mfa)) {
                notifyResponse(null);
            } else {
                final Response response = new Response();
                response.mfa = mfa;
                response.token = null;
                notifyResponse(response);
            }
        } else {
            notifyFailure(new Exception("Unexpected status code " + code + " with body: " + json));
        }
    }

    static class Response {

        String mfa;

        Auth0Token token;

    }

}
