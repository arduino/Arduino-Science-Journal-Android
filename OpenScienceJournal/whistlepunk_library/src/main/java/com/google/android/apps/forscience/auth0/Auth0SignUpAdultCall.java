package com.google.android.apps.forscience.auth0;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Map;

public class Auth0SignUpAdultCall extends Auth0SignUpCall {

    private final boolean mAcceptPrivacy;
    private final boolean mAcceptTerms;
    private final boolean mAcceptMarketing;
    private final boolean mAcceptNewsletter;
    private final boolean mAcceptTracking;

    public Auth0SignUpAdultCall(final @NonNull Context context,
                                final @NonNull String username,
                                final @NonNull String email,
                                final @NonNull String password,
                                final String birthday,
                                final boolean acceptPrivacy,
                                final boolean acceptTerms,
                                final boolean acceptMarketing,
                                final boolean acceptNewsletter,
                                final boolean acceptTracking) {
        super(context, username, email, password, birthday);
        mAcceptPrivacy = acceptPrivacy;
        mAcceptTerms = acceptTerms;
        mAcceptMarketing = acceptMarketing;
        mAcceptNewsletter = acceptNewsletter;
        mAcceptTracking = acceptTracking;
    }

    @Override
    protected void completeBuildRequest(Map<String, String> params) {
        params.put("connection", "arduino");
        params.put("user_metadata[privacy_approval]", mAcceptPrivacy ? "true" : "false");
        params.put("user_metadata[terms_and_conditions]", mAcceptTerms ? "true" : "false");
        params.put("user_metadata[marketing_approval]", mAcceptMarketing ? "true" : "false");
        params.put("user_metadata[newsletter_approval]", mAcceptNewsletter ? "true" : "false");
        params.put("user_metadata[tracking_approval]", mAcceptTracking ? "true" : "false");
    }

}
