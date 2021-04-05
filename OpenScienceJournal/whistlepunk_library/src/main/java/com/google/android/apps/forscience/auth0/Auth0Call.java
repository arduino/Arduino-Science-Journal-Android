package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.apps.forscience.utils.URLUtils;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Authenticator;
import com.google.android.apps.forscience.whistlepunk.remote.HttpCall;
import com.google.android.apps.forscience.whistlepunk.remote.Method;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

abstract class Auth0Call<RES> extends HttpCall<RES, Exception> {

    protected final String mDomain;

    protected final String mClientId;

    protected Auth0Call(@NonNull Context context) {
        super(context);
        mDomain = context.getString(R.string.config_auth0_domain);
        mClientId = context.getString(R.string.config_auth0_client_id);
    }

    @Override
    protected String buildQueueId() {
        return "AUTH0";
    }

    @Override
    protected final Method buildMethod() {
        return Method.POST;
    }

    @Override
    protected String buildUrl() {
        return "https://" + URLUtils.encode(mDomain) + buildEndpoint();
    }

    @Override
    protected final Authenticator buildAuthenticator() {
        return null;
    }

    @Override
    protected final String buildRequestContentType() {
        return "application/x-www-form-urlencoded";
    }

    @Override
    protected final byte[] buildRequestBody() {
        final Map<String, String> params = new HashMap<>();
        params.put("client_id", mClientId);
        buildRequest(params);
        final String body = URLUtils.encodeParameters(params);
        return body.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void onCompleted(int code, String contentType, String contentEncoding, Map<String, String> headers, byte[] body) {
        if (body == null || body.length == 0) {
            onCompleted(code, null);
        } else {
            if (contentEncoding == null) {
                contentEncoding = "UTF-8";
            }
            JSONObject response = null;
            try {
                JSONTokener tokener = new JSONTokener(new String(body, contentEncoding));
                Object obj = tokener.nextValue();
                if (obj instanceof JSONObject) {
                    response = (JSONObject) obj;
                } else if (obj instanceof JSONArray) {
                    response = new JSONObject();
                    response.put("response", obj);
                } else {
                    throw new Exception("unsupported JSON response type in: " + obj);
                }
            } catch (Exception e) {
                onError(e);
            }
            if (response != null) {
                onCompleted(code, response);
            }
        }
    }

    @Override
    protected void onError(Exception e) {
        notifyFailure(e);
    }

    protected abstract String buildEndpoint();

    protected abstract void buildRequest(final Map<String, String> params);

    protected abstract void onCompleted(int code, JSONObject response);

}
