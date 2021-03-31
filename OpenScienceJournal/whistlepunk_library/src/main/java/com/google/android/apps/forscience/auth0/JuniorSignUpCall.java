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
public class JuniorSignUpCall extends JSONCall<Void, Exception> {

    private final String mBirthday;
    private final String mParentEmail;
    private final String mAvatar;
    private final String mUsername;
    private final String mPassword;

    public JuniorSignUpCall(@NonNull Context context, final String birthday, final String parentEmail, final String avatar, final String username, final String password) {
        super(context);
        this.mBirthday = birthday;
        this.mParentEmail = parentEmail;
        this.mAvatar = avatar;
        this.mUsername = username;
        this.mPassword = password;
    }

    @Override
    protected Method buildMethod() {
        return Method.POST;
    }

    @Override
    protected String buildUrl() {
        return "https://" + getContext().getString(R.string.config_arduino_api_domain) + "/users/v1/children";
    }

    @Override
    protected JSONObject buildRequestObject() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put("birthday", mBirthday);
        json.put("parent_email", mParentEmail);
        json.put("avatar", mAvatar);
        json.put("username", mUsername);
        json.put("password", mPassword);
        return json;
    }

    @Override
    protected Authenticator buildAuthenticator() {
        return null;
    }

    @Override
    protected void onCompleted(int code, Map<String, String> headers, JSONObject response) {
        if (code == 201) {
            notifyResponse(null);
        } else {
            onError(new Exception("unexpected response: " + response.toString()));
        }
    }

    @Override
    protected void onError(Exception e) {
        notifyFailure(e);
    }

}
