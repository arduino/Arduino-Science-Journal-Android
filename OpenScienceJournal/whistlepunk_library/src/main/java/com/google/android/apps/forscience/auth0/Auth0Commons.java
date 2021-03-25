package com.google.android.apps.forscience.auth0;

import android.util.Base64;

import com.google.android.apps.forscience.utils.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Auth0Commons {

    static final String LOG_TAG = "Auth0";

    public static String sha256(final String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        final MessageDigest d;
        try {
            d = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA256 not supported", e);
        }
        d.update(s.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(d.digest(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

}
