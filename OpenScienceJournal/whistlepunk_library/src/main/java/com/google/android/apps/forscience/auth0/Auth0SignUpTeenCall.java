package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Map;

public class Auth0SignUpTeenCall extends Auth0SignUpCall {

    private final String mParentEmail;

    public Auth0SignUpTeenCall(final @NonNull Context context,
                               final @NonNull String username,
                               final @NonNull String email,
                               final @NonNull String password,
                               final @NonNull String birthday,
                               final @NonNull String parentEmail) {
        super(context, username, email, password, birthday);
        mParentEmail = parentEmail;
    }

    @Override
    protected void completeBuildRequest(Map<String, String> params) {
        params.put("user_metadata[parent_email]", mParentEmail);
    }

}
