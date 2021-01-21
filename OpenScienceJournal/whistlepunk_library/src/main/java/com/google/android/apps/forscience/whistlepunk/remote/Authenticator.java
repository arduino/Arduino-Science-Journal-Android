package com.google.android.apps.forscience.whistlepunk.remote;

import android.content.Context;

import java.net.HttpURLConnection;

public interface Authenticator {

    void addAuthentication(Context context, HttpURLConnection connection) throws NetworkException, AuthenticationException, InterruptedException;

    void handleAuthenticationResult(Context context, int statusCode) throws NetworkException, AuthenticationException;

}
