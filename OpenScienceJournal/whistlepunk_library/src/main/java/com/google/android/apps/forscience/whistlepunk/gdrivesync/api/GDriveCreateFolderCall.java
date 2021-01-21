package com.google.android.apps.forscience.whistlepunk.gdrivesync.api;

import android.content.Context;

import com.google.android.apps.forscience.whistlepunk.remote.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class GDriveCreateFolderCall extends GDriveAPICall<String, Exception> {

    private final String mName;

    private final String mParentFolderId;

    public GDriveCreateFolderCall(final Context context, final String token, final String name, final String parentFolderId) {
        super(context, token);
        mName = name;
        mParentFolderId = parentFolderId;
    }

    @Override
    protected Method buildMethod() {
        return Method.POST;
    }

    @Override
    protected String buildUrl() {
        return BASE_URL + "/files";
    }

    @Override
    protected JSONObject buildRequestObject() throws JSONException {
        final JSONArray jsonParents = new JSONArray();
        jsonParents.put(mParentFolderId);
        final JSONObject json = new JSONObject();
        json.put("mimeType", Constants.MIME_TYPE_FOLDER);
        json.put("name", mName);
        json.put("parents", jsonParents);
        return json;
    }

    @Override
    protected void onCompleted(int code, Map<String, String> headers, JSONObject response) {
        if (code != 200) {
            notifyFailure(new Exception("invalid response code: " + code));
            return;
        }
        final String result;
        try {
            result = response.getString("id");
        } catch (Exception e) {
            notifyFailure(new Exception("unexpected server response structure", e));
            return;
        }
        notifyResponse(result);
    }

    @Override
    protected void onError(Exception e) {
        notifyFailure(e);
    }

}
