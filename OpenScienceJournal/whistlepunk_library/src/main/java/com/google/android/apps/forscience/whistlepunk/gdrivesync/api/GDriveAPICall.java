package com.google.android.apps.forscience.whistlepunk.gdrivesync.api;

import android.content.Context;

import com.google.android.apps.forscience.whistlepunk.remote.Authenticator;
import com.google.android.apps.forscience.whistlepunk.remote.JSONCall;

abstract class GDriveAPICall<R, E> extends JSONCall<R, E> {

    protected final String BASE_URL = "https://www.googleapis.com/drive/v3";

    private final String mToken;

    protected GDriveAPICall(Context context, final String token) {
        super(context);
        mToken = token;
    }

    @Override
    protected Authenticator buildAuthenticator() {
        return new GDriveAuthenticator(mToken);
    }

    @Override
    protected String buildQueueId() {
        return "GDRIVE-API";
    }

}
