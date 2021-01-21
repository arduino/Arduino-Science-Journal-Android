package com.google.android.apps.forscience.whistlepunk.gdrivesync.api;

import android.content.Context;

import com.google.android.apps.forscience.whistlepunk.remote.Method;
import com.google.android.apps.forscience.whistlepunk.remote.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class GDriveBrowseCall extends GDriveAPICall<GDriveBrowseCall.Result, Exception> {

    private final String mFolderId;

    private final boolean mOnlyFolders;

    private final String mPageToken;

    public GDriveBrowseCall(final Context context, final String token, final String folderId, final boolean onlyFolders, final String pageToken) {
        super(context, token);
        mFolderId = folderId;
        mOnlyFolders = onlyFolders;
        mPageToken = pageToken;
    }

    @Override
    protected Method buildMethod() {
        return Method.GET;
    }

    @Override
    protected String buildUrl() {
        return BASE_URL + "/files";
    }

    @Override
    protected Map<String, String> buildRequestParams() {
        final Map<String, String> params = new HashMap<>();
        params.put("corpora", "user");
        params.put("orderBy", "folder,name");
        params.put("q", "\"" + mFolderId + "\" in parents and trashed = false" + (mOnlyFolders ? " and mimeType = '" + Constants.MIME_TYPE_FOLDER + "'" : ""));
        params.put("fields", "incompleteSearch,nextPageToken,files(id,name,mimeType,iconLink)");
        if (mPageToken != null) {
            params.put("pageToken", mPageToken);
        }
        return params;
    }

    @Override
    protected void onCompleted(int code, Map<String, String> headers, JSONObject response) {
        if (code != 200) {
            notifyFailure(new Exception("invalid response code: " + code));
            return;
        }
        final Result result = new Result();
        try {
            result.nextPageToken = StringUtils.nullIfEmpty(response.optString("nextPageToken", ""));
            final JSONArray jsonFiles = response.optJSONArray("files");
            final int size = jsonFiles != null ? jsonFiles.length() : 0;
            result.files = new GDriveFile[size];
            for (int i = 0; i < size; i++) {
                final JSONObject jsonFile = jsonFiles.getJSONObject(i);
                result.files[i] = new GDriveFile();
                result.files[i].id = jsonFile.getString("id");
                result.files[i].name = jsonFile.getString("name");
                result.files[i].mimeType = jsonFile.getString("mimeType");
                result.files[i].iconLink = jsonFile.getString("iconLink");
                result.files[i].isFolder = Constants.MIME_TYPE_FOLDER.equals(result.files[i].mimeType);
            }
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

    public static class Result {

        public String nextPageToken;

        public GDriveFile[] files;

    }

}
