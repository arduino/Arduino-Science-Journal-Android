package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.Map;

class Auth0PasswordResetCall extends Auth0Call<Boolean> {

    private final String mEmail;

    Auth0PasswordResetCall(final @NonNull Context context, final @NonNull String email) {
        super(context);
        mEmail = email;
    }

    @Override
    protected String buildEndpoint() {
        return "/dbconnections/change_password";
    }

    @Override
    protected void buildRequest(Map<String, String> params) {
        params.put("email", mEmail);
        params.put("connection", mConnection);
    }

    @Override
    protected void onCompleted(int code, String contentType, String contentEncoding, Map<String, String> headers, byte[] body) {
        if (code == 200) {
            notifyResponse(Boolean.TRUE);
        } else {
            notifyFailure(new Exception("Unexpected status code " + code));
        }
    }

    @Override
    protected void onCompleted(int code, JSONObject json) {
        // not used
    }

}
