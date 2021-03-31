package com.google.android.apps.forscience.whistlepunk.signin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.google.android.apps.forscience.auth0.Auth0Token;
import com.google.android.apps.forscience.auth0.Auth0WebSignInSocialActivity;
import com.google.android.apps.forscience.utils.StringUtils;

class Commons {

    static boolean isUsernameValid(final String username) {
        if (StringUtils.isEmpty(username) || username.length() < 3) {
            return false;
        }
        for (int i = 0; i < username.length(); i++) {
            final char c = username.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '_') {
                if (i == 0 && (c == '-' || c == '_')) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    static boolean isEmailValid(final String email) {
        return !StringUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    static boolean isPasswordValid(final String password) {
        return !StringUtils.isEmpty(password) && password.length() >= 8;
    }


    public static void doGitHubSignIn(final @NonNull Context context, final @NonNull SocialSignInCallback callback) {
        doSocialSignIn(context, Auth0WebSignInSocialActivity.EXTRA_VALUE_SOCIAL_GITHUB, callback);
    }

    public static void doGoogleSignIn(final @NonNull Context context, final @NonNull SocialSignInCallback callback) {
        doSocialSignIn(context, Auth0WebSignInSocialActivity.EXTRA_VALUE_SOCIAL_GOOGLE, callback);
    }

    public static void doAppleSignIn(final @NonNull Context context, final @NonNull SocialSignInCallback callback) {
        doSocialSignIn(context, Auth0WebSignInSocialActivity.EXTRA_VALUE_SOCIAL_APPLE, callback);
    }

    private static void doSocialSignIn(final @NonNull Context context, final @NonNull String social, final @NonNull SocialSignInCallback callback) {
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                final String result = intent.getStringExtra(Auth0WebSignInSocialActivity.RESULT_KEY);
                if (Auth0WebSignInSocialActivity.RESULT_VALUE_SUCCESS.equals(result)) {
                    final Auth0Token token = intent.getParcelableExtra(Auth0WebSignInSocialActivity.TOKEN_KEY);
                    if (token != null) {
                        callback.onSuccess(token);
                    } else {
                        callback.onError(new Exception("social login failure: no token"));
                    }
                } else if (Auth0WebSignInSocialActivity.RESULT_VALUE_CANCELLED.equals(result)) {
                    callback.onCancel();
                } else {
                    callback.onError(new Exception("social login failure with code: " + result));
                }
            }
        };
        context.registerReceiver(receiver, new IntentFilter(Auth0WebSignInSocialActivity.ACTION_CALLBACK));
        final Intent intent = new Intent(context, Auth0WebSignInSocialActivity.class);
        intent.putExtra(Auth0WebSignInSocialActivity.EXTRA_KEY_SOCIAL, social);
        context.startActivity(intent);
    }

    public interface SocialSignInCallback {

        void onError(Exception exception);

        void onCancel();

        void onSuccess(Auth0Token token);

    }

    public static void openPrivacy(final Context context) {
        //TODO
    }

    public static void openTerms(final Context context) {
        //TODO
    }

}
