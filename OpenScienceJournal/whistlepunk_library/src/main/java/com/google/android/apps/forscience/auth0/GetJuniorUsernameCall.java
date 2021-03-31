package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.apps.forscience.utils.StringUtils;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Authenticator;
import com.google.android.apps.forscience.whistlepunk.remote.JSONCall;
import com.google.android.apps.forscience.whistlepunk.remote.Method;

import org.json.JSONObject;

import java.util.Map;

public class GetJuniorUsernameCall extends JSONCall<String, Exception> {

    public GetJuniorUsernameCall(@NonNull Context context) {
        super(context);
    }

    @Override
    protected Method buildMethod() {
        return Method.GET;
    }

    @Override
    protected String buildUrl() {
        return "https://" + getContext().getString(R.string.config_arduino_api_domain) + "/users/v1/children/username";
    }

    @Override
    protected Authenticator buildAuthenticator() {
        return null;
    }

    @Override
    protected void onCompleted(int code, Map<String, String> headers, JSONObject response) {
        final String username;
        try {
            username = response.getString("response");
        } catch (Exception e) {
            onError(e);
            return;
        }
        if (StringUtils.isEmpty(username)) {
            onError(new Exception("Empty username returned"));
        }
        notifyResponse(username);
    }

    @Override
    protected void onError(Exception e) {
        notifyFailure(e);
    }

}
