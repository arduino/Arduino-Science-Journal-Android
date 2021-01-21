package com.google.android.apps.forscience.whistlepunk.gdrivesync.api;

import android.content.Context;

import com.google.android.apps.forscience.whistlepunk.remote.AuthenticationException;
import com.google.android.apps.forscience.whistlepunk.remote.Authenticator;
import com.google.android.apps.forscience.whistlepunk.remote.NetworkException;

import java.net.HttpURLConnection;

class GDriveAuthenticator implements Authenticator {

    private final String mToken;

    GDriveAuthenticator(final String token) {
        this.mToken = token;
    }

    @Override
    public void addAuthentication(Context context, HttpURLConnection connection) throws NetworkException, AuthenticationException, InterruptedException {
        connection.setRequestProperty("Authorization", "Bearer " + mToken);
    }

    @Override
    public void handleAuthenticationResult(Context context, int statusCode) throws AuthenticationException {
        if (statusCode == 401) {
            throw new AuthenticationException("Auth token has been refused");
        }
    }

}
