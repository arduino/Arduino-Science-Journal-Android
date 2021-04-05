package com.google.android.apps.forscience.auth0;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Authenticator;
import com.google.android.apps.forscience.whistlepunk.remote.JSONCall;
import com.google.android.apps.forscience.whistlepunk.remote.Method;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

@SuppressLint("SimpleDateFormat")
public class JuniorPasswordResetCall extends JSONCall<Boolean, Exception> {

    private final String mParentEmail;
    private final String mUsername;

    public JuniorPasswordResetCall(@NonNull Context context, final String parentEmail, final String username) {
        super(context);
        this.mParentEmail = parentEmail;
        this.mUsername = username;
    }

    @Override
    protected Method buildMethod() {
        return Method.POST;
    }

    @Override
    protected String buildUrl() {
        return "https://" + getContext().getString(R.string.config_arduino_api_domain) + "/users/v1/children/help";
    }

    @Override
    protected JSONObject buildRequestObject() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put("parent_email", mParentEmail);
        json.put("username", mUsername);
        return json;
    }

    @Override
    protected Authenticator buildAuthenticator() {
        return null;
    }

    @Override
    protected void onCompleted(int code, Map<String, String> headers, JSONObject response) {
        if (code == 204) {
            notifyResponse(true);
        } else if (code == 404) {
            notifyResponse(false);
        } else {
            onError(new Exception("unexpected response: " + response.toString()));
        }
    }

    @Override
    protected void onError(Exception e) {
        notifyFailure(e);
    }

}
