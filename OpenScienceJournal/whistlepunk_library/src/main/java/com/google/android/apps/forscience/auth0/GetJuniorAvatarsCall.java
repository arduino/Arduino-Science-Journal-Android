package com.google.android.apps.forscience.auth0;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Authenticator;
import com.google.android.apps.forscience.whistlepunk.remote.JSONCall;
import com.google.android.apps.forscience.whistlepunk.remote.Method;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetJuniorAvatarsCall extends JSONCall<GetJuniorAvatarsCall.Avatar[], Exception> {

    public GetJuniorAvatarsCall(@NonNull Context context) {
        super(context);
    }

    @Override
    protected Method buildMethod() {
        return Method.GET;
    }

    @Override
    protected String buildUrl() {
        return "https://" + getContext().getString(R.string.config_arduino_api_domain) + "/users/v1/children/avatars";
    }

    @Override
    protected Authenticator buildAuthenticator() {
        return null;
    }

    @Override
    protected void onCompleted(int code, Map<String, String> headers, JSONObject response) {
        final List<Avatar> avatars = new ArrayList<>();
        try {
            final JSONArray jsonAvatars = response.getJSONArray("response");
            for (int i = 0; i < jsonAvatars.length(); i++) {
                final JSONObject jsonAvatar = jsonAvatars.getJSONObject(i);
                final Avatar avatar = new Avatar();
                avatar.id = jsonAvatar.getString("id");
                avatar.data = Base64.decode(jsonAvatar.getString("data"), Base64.DEFAULT);
                avatars.add(avatar);
            }
        } catch (Exception e) {
            onError(e);
            return;
        }
        notifyResponse(avatars.toArray(new Avatar[0]));
    }

    @Override
    protected void onError(Exception e) {
        notifyFailure(e);
    }

    public static class Avatar {

        public String id;

        public byte[] data;

    }

}
