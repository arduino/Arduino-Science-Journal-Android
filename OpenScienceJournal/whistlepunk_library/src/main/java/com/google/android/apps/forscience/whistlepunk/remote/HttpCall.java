package com.google.android.apps.forscience.whistlepunk.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Map;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public abstract class HttpCall<R, F> {

    protected Context mContext;

    Callback<R, F> mCallback;

    protected HttpCall(@NonNull Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }

    protected abstract Method buildMethod();

    protected abstract String buildUrl();

    protected abstract Authenticator buildAuthenticator();

    protected Map<String, String> buildRequestParams() {
        return null;
    }

    protected Map<String, String> buildRequestHeaders() {
        return null;
    }

    protected String buildRequestContentType() {
        return null;
    }

    protected byte[] buildRequestBody() {
        return null;
    }

    protected String buildQueueId() {
        return "DEFAULT";
    }

    protected void notifyResponse(R response) {
        if (mCallback != null) {
            mCallback.onResponse(response);
        }
    }

    protected void notifyFailure(F failure) {
        if (mCallback != null) {
            mCallback.onFailure(failure);
        }
    }

    private boolean notifyNetworkError() {
        return mCallback != null && mCallback.onNetworkError();
    }

    private boolean notifyAuthenticationError() {
        return mCallback != null && mCallback.onAuthenticationError();
    }

    private boolean notifyCancel() {
        return mCallback != null && mCallback.onCancel();
    }

    public final CallController execute(Callback<R, F> callback) {
        mCallback = callback;
        return CallQueues.enqueue(buildQueueId(), this);
    }

    protected abstract void onCompleted(int code, String contentType, String contentEncoding, Map<String, String> headers, byte[] body);

    protected abstract void onError(Exception e);

    protected boolean onNetworkError(Exception e) {
        return notifyNetworkError();
    }

    protected boolean onAuthenticationError() {
        return notifyAuthenticationError();
    }

    protected boolean onCancel() {
        return notifyCancel();
    }

}
