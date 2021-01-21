package com.google.android.apps.forscience.whistlepunk.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class JSONCall<R, E> extends HttpCall<R, E> {

    protected JSONCall(@NonNull Context context) {
        super(context);
    }

    @Override
    protected String buildRequestContentType() {
        return "application/json";
    }

    protected JSONObject buildRequestObject() throws JSONException {
        return null;
    }

    @Override
    protected byte[] buildRequestBody() {
        JSONObject obj;
        try {
            obj = buildRequestObject();
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected JSONException?!?", e);
        }
        if (obj != null) {
            return obj.toString().getBytes(StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    @Override
    protected void onCompleted(int code, String contentType, String contentEncoding, Map<String, String> headers, byte[] body) {
        if (body == null || body.length == 0) {
            onCompleted(code, headers, null);
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
                onCompleted(code, headers, response);
            }
        }
    }

    protected abstract void onCompleted(int code, Map<String, String> headers, JSONObject response);

}
