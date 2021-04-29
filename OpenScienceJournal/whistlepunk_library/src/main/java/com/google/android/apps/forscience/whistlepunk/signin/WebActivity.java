package com.google.android.apps.forscience.whistlepunk.signin;

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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.StringUtils;

@SuppressLint("SetJavaScriptEnabled")
public class WebActivity extends AppCompatActivity {

    public static final String EXTRA_KEY_TITLE = "title";

    public static final String EXTRA_KEY_URL = "url";

    private static final String LOG_TAG = "WebActivity";

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private WebView mWebView;

    private ProgressBar mWebLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retrieve params
        final Intent intent = getIntent();
        final String title = intent.getStringExtra(EXTRA_KEY_TITLE);
        final String url = intent.getStringExtra(EXTRA_KEY_URL);
        // configure layout
        setContentView(R.layout.activity_arduino_auth_web);
        findViewById(R.id.iv_back_action).setOnClickListener(v -> finish());
        if (!StringUtils.isEmpty(title)) {
            ((TextView) findViewById(R.id.tv_title)).setText(title);
        }
        mWebView = findViewById(R.id.wv_webview);
        mWebLoader = findViewById(R.id.pb_webloader);
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
        // load url
        loadUrl(url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
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

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
            Log.e(LOG_TAG, "onReceivedError for URL '" + failingUrl + "':" + errorCode + " " + description);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e(LOG_TAG, "onReceivedError for URL '" + request.getUrl() + "':" + error.getErrorCode() + " " + error.getDescription());
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
