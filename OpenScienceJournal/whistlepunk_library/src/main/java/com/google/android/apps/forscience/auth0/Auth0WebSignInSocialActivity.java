package com.google.android.apps.forscience.auth0;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.apps.forscience.utils.StringUtils;
import com.google.android.apps.forscience.utils.URLUtils;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Callback;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class Auth0WebSignInSocialActivity extends AppCompatActivity {

    public static final String ACTION_CALLBACK = "asj.auth0.websignin.CALLBACK";

    public static final String EXTRA_KEY_SOCIAL = "social";

    public static final String EXTRA_VALUE_SOCIAL_GITHUB = "github";

    public static final String EXTRA_VALUE_SOCIAL_GOOGLE = "google";

    public static final String EXTRA_VALUE_SOCIAL_APPLE = "apple";

    public static final String RESULT_KEY = "asj.websignin.CALLBACK.result";

    public static final String RESULT_VALUE_CANCELLED = "cancelled";

    public static final String RESULT_VALUE_ERROR = "error";

    public static final String RESULT_VALUE_SUCCESS = "success";

    public static final String TOKEN_KEY = "asj.websignin.CALLBACK.token";

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private WebView mWebView;

    private ProgressBar mWebLoader;

    private View mLoader;

    private String mRedirectUri;

    private String mStateValue;

    private String mChallenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String social = intent.getStringExtra(EXTRA_KEY_SOCIAL);
        // configure layout
        setContentView(R.layout.activity_arduino_auth_social);
        findViewById(R.id.iv_back_action).setOnClickListener(v -> finishWithResult(RESULT_VALUE_CANCELLED, null));
        mWebView = findViewById(R.id.wv_webview);
        mWebLoader = findViewById(R.id.pb_webloader);
        mLoader = findViewById(R.id.loader);
        // user agent
        String userAgent = null;
        try {
            userAgent = WebSettings.getDefaultUserAgent(this);
        } catch (Throwable ignored) {
        }
        if (userAgent == null) {
            try {
                userAgent = new WebView(this).getSettings().getUserAgentString();
            } catch (Exception e) {
                userAgent = "ASJ";
            }
        }
        userAgent = userAgent.replaceAll("; wv", "");
        // web settings
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(false);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUserAgentString(userAgent);
        // start url
        final String domain = getString(R.string.config_auth0_domain);
        mRedirectUri = getString(R.string.config_auth0_callback_uri);
        mStateValue = randomStateValue(32);
        mChallenge = randomStateValue(64);
        final Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", getString(R.string.config_auth0_client_id));
        if (EXTRA_VALUE_SOCIAL_GITHUB.equals(social)) {
            params.put("connection", "github");
        } else if (EXTRA_VALUE_SOCIAL_GOOGLE.equals(social)) {
            params.put("connection", "google-oauth2");
        } else if (EXTRA_VALUE_SOCIAL_APPLE.equals(social)) {
            params.put("connection", "apple");
        } else {
            finishWithResult(RESULT_VALUE_ERROR, null);
            return;
        }
        params.put("redirect_uri", mRedirectUri);
        params.put("state", mStateValue);
        params.put("code_challenge_method", "S256");
        params.put("code_challenge", Auth0Commons.sha256(mChallenge));
        params.put("scope", getString(R.string.config_auth0_scope));
        params.put("audience", getString(R.string.config_auth0_audience));
        final String url = "https://" + URLUtils.encode(domain) + "/authorize?" + URLUtils.encodeParameters(params);
        loadUrl(url);
    }

    private String randomStateValue(int length) {
        final StringBuilder b = new StringBuilder();
        final String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            b.append(alphabet.charAt((int) Math.floor(Math.random() * alphabet.length())));
        }
        return b.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.destroy();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finishWithResult(RESULT_VALUE_CANCELLED, null);
        }
    }

    private void finishWithResult(final String result, final Auth0Token token) {
        final Intent intent = new Intent(ACTION_CALLBACK);
        intent.putExtra(RESULT_KEY, result);
        if (token != null) {
            intent.putExtra(TOKEN_KEY, token);
        }
        sendBroadcast(intent);
        finish();
    }

    protected void loadUrl(String url) {
        final CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(value -> mWebView.loadUrl(url));
        } else {
            cookieManager.removeAllCookie();
            sHandler.postDelayed(() -> mWebView.loadUrl(url), 1000);
        }
    }

    private void onAuth0WebResponse(final Map<String, String> params) {
        final String state = params.get("state");
        if (!mStateValue.equals(state)) {
            finishWithResult(RESULT_VALUE_ERROR, null);
            return;
        }
        final String code = params.get("code");
        if (StringUtils.isEmpty(code)) {
            finishWithResult(RESULT_VALUE_ERROR, null);
            return;
        }
        mLoader.setVisibility(View.VISIBLE);
        new Auth0CodeTokenCall(this, code, mChallenge, mRedirectUri).execute(new Callback<Auth0Token, Exception>() {
            @Override
            public void onResponse(Auth0Token token) {
                finishWithResult(RESULT_VALUE_SUCCESS, token);
            }

            @Override
            public void onFailure(Exception failure) {
                Log.e(Auth0Commons.LOG_TAG, "Error", failure);
                finishWithResult(RESULT_VALUE_ERROR, null);
            }
        });
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(mRedirectUri + "?")) {
                String query = url.substring(mRedirectUri.length() + 1);
                int hash = query.indexOf('#');
                if (hash != -1) {
                    query = query.substring(0, hash);
                }
                final Map<String, String> params = URLUtils.decodeParameters(query);
                onAuth0WebResponse(params);
                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mWebLoader.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mWebLoader.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.e(Auth0Commons.LOG_TAG, "onReceivedError for URL '" + failingUrl + "':" + errorCode + " " + description);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e(Auth0Commons.LOG_TAG, "onReceivedError for URL '" + request.getUrl() + "':" + error.getErrorCode() + " " + error.getDescription());
            }
        }

    }

    public class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mWebLoader.setProgress(newProgress);
        }

    }

}
