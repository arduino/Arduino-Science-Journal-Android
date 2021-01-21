package com.google.android.apps.forscience.whistlepunk.remote;

@SuppressWarnings("WeakerAccess")
public abstract class Callback<R, F> {

    protected CallController mCallController;

    public abstract void onResponse(R response);

    public abstract void onFailure(F failure);

    public boolean onNetworkError() {
        return false;
    }

    public boolean onAuthenticationError() {
        return false;
    }

    public boolean onCancel() {
        return false;
    }

}
