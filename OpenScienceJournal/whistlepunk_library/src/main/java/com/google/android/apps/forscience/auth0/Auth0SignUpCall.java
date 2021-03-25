package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.Map;

public class Auth0SignUpCall extends Auth0Call<Auth0SignUpCall.Response> {

    private final String mUsername;
    private final String mEmail;
    private final String mPassword;

    private final boolean mAcceptPrivacy;
    private final boolean mAcceptTerms;
    private final boolean mAcceptMarketing;
    private final boolean mAcceptNewsletter;
    private final boolean mAcceptTracking;

    public Auth0SignUpCall(final @NonNull Context context,
                           final @NonNull String username,
                           final @NonNull String email,
                           final @NonNull String password,
                           final boolean acceptPrivacy,
                           final boolean acceptTerms,
                           final boolean acceptMarketing,
                           final boolean acceptNewsletter,
                           final boolean acceptTracking) {
        super(context);
        mUsername = username;
        mEmail = email;
        mPassword = password;
        mAcceptPrivacy = acceptPrivacy;
        mAcceptTerms = acceptTerms;
        mAcceptMarketing = acceptMarketing;
        mAcceptNewsletter = acceptNewsletter;
        mAcceptTracking = acceptTracking;
    }

    @Override
    protected String buildEndpoint() {
        return "/dbconnections/signup";
    }

    @Override
    protected void buildRequest(Map<String, String> params) {
        params.put("connection", mConnection);
        params.put("username", mUsername);
        params.put("email", mEmail);
        params.put("password", mPassword);
        params.put("user_metadata[privacy_approval]", mAcceptPrivacy ? "true" : "false");
        params.put("user_metadata[terms_and_conditions]", mAcceptTerms ? "true" : "false");
        params.put("user_metadata[marketing_approval]", mAcceptMarketing ? "true" : "false");
        params.put("user_metadata[newsletter_approval]", mAcceptNewsletter ? "true" : "false");
        params.put("user_metadata[tracking_approval]", mAcceptTracking ? "true" : "false");
    }

    @Override
    protected void onCompleted(int code, JSONObject json) {
        if (code == 200) {
            final Response response = new Response();
            response.success = true;
            notifyResponse(response);
        } else if (code == 400) {
            final Response response = new Response();
            response.success = false;
            final String error = json.optString("code", "");
            switch (error) {
                case "user_exists":
                    response.code = Response.Code.USER_EXISTS;
                    break;
                case "invalid_password":
                    response.code = Response.Code.INVALID_PASSWORD;
                    break;
                case "consent":
                    response.code = Response.Code.CONSENT;
                    break;
                default:
                    response.code = Response.Code.UNKNOWN;
                    break;
            }
            notifyResponse(response);
        } else {
            notifyFailure(new Exception("Unexpected status code " + code + " with body: " + json));
        }
    }

    public static class Response {

        public boolean success;

        public Code code;

        public enum Code {

            USER_EXISTS, INVALID_PASSWORD, CONSENT, UNKNOWN

        }

    }

}
