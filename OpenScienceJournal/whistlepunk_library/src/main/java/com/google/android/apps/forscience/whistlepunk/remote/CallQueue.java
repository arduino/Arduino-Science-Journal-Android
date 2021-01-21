package com.google.android.apps.forscience.whistlepunk.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.apps.forscience.whistlepunk.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

class CallQueue {

    private static final String LOG_TAG = "RemoteQueue";

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private final List<HttpCall<?, ?>> mQueue = new ArrayList<>();

    private Thread mCallThread;

    private HttpCall<?, ?> mCurrentCall;

    CallQueue() {
    }

    CallController enqueue(final HttpCall<?, ?> call) {
        final CallController controller = () -> {
            synchronized (mQueue) {
                if (mCurrentCall == call) {
                    if (mCallThread != null) {
                        mCallThread.interrupt();
                    }
                } else {
                    if (mQueue.remove(call)) {
                        sHandler.post(call::onCancel);
                    }
                }
            }
        };
        if (call.mCallback != null) {
            call.mCallback.mCallController = controller;
        }
        synchronized (mQueue) {
            mQueue.add(call);
            mQueue.notifyAll();
            if (mCallThread == null) {
                mCallThread = new Thread(new QueueRoutine());
                mCallThread.start();
            }
        }
        return controller;
    }

    private Response call(Context context,
                          Method method,
                          String url,
                          Authenticator authenticator,
                          Map<String, String> queryParams,
                          Map<String, String> requestHeaders,
                          String requestContentType,
                          byte[] requestBody
    ) throws InterruptedException, NetworkException, AuthenticationException, IOException {
        String query = null;
        if (queryParams != null && queryParams.size() > 0) {
            StringBuilder aux = new StringBuilder();
            for (Map.Entry<String, String> param : queryParams.entrySet()) {
                if (aux.length() > 0) {
                    aux.append('&');
                }
                aux.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                aux.append('=');
                aux.append(URLEncoder.encode(param.getValue(), "UTF-8"));
            }
            query = aux.toString();
        }
        if (query != null) {
            int qm = url.indexOf('?');
            if (qm == -1) {
                url = url + '?' + query;
            } else if (qm == url.length() - 1) {
                url = url + query;
            } else {
                url = url + '&' + query;
            }
        }
        boolean doOutput = !Method.GET.equals(method) && requestBody != null;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        connection.setRequestMethod(method.name());
        connection.setDoOutput(doOutput);
        connection.setDoInput(true);
        // TODO connection.setRequestProperty("User-Agent", Globals.USER_AGENT);
        // TODO connection.setRequestProperty("Accept-Language", Globals.LANGUAGE);
        connection.setRequestProperty("Cache-Control", "no-cache");
        if (authenticator != null) {
            authenticator.addAuthentication(context, connection);
        }
        if (requestContentType != null && doOutput) {
            connection.addRequestProperty("Content-Type", requestContentType);
        } else {
            connection.addRequestProperty("Content-Type", "");
        }
        if (requestHeaders != null && requestHeaders.size() > 0) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (doOutput) {
            OutputStream outputStream = connection.getOutputStream();
            try {
                outputStream.write(requestBody);
                outputStream.flush();
            } finally {
                IOUtils.safeClose(outputStream);
            }
        }
        checkInterruption();
        int code = connection.getResponseCode();
        checkInterruption();
        Response response = new Response();
        response.code = code;
        response.contentType = StringUtils.nullIfEmpty(StringUtils.nullSafeTrim(connection.getContentType()));
        if (response.contentType != null) {
            StringTokenizer st1 = new StringTokenizer(response.contentType, ";");
            response.contentType = st1.nextToken();
            while (st1.hasMoreTokens()) {
                String tk = st1.nextToken().trim();
                StringTokenizer st2 = new StringTokenizer(tk, "=");
                if (st2.countTokens() >= 2) {
                    String key = st2.nextToken().trim();
                    String value = st2.nextToken().trim();
                    if ("charset".equalsIgnoreCase(key) && value.length() > 0) {
                        response.contentEncoding = value;
                        break;
                    }
                }
            }
        }
        if (response.contentEncoding == null) {
            response.contentEncoding = connection.getContentEncoding();
        }
        response.headers = new HashMap<>();
        Map<String, List<String>> responseHeaders = connection.getHeaderFields();
        if (responseHeaders != null) {
            for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                if (values != null && values.size() > 0) {
                    String value = values.get(values.size() - 1);
                    response.headers.put(key, value);
                }
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            inputStream = connection.getErrorStream();
        }
        if (inputStream != null) {
            try {
                byte[] b = new byte[1024];
                int l;
                while ((l = inputStream.read(b)) != -1) {
                    baos.write(b, 0, l);
                    checkInterruption();
                }
            } finally {
                IOUtils.safeClose(inputStream);
            }
            baos.close();
            response.body = baos.toByteArray();
        }
        checkInterruption();
        if (BuildConfig.DEBUG) {
            if (response.body != null && response.body.length > 0) {
                String type = StringUtils.emptyIfNull(response.contentType);
                if (type.startsWith("text/") || type.equals("application/json")) {
                    String encoding = response.contentEncoding;
                    if (encoding == null) {
                        encoding = "UTF-8";
                    }
                    String aux = new String(response.body, encoding);
                    Log.v(LOG_TAG, "Server response: CODE " + code + " BODY " + aux);
                } else {
                    Log.v(LOG_TAG, "Server response: CODE " + code + " TYPE " + type + " LENGTH " + response.body.length);
                }
            } else {
                Log.v(LOG_TAG, "Server response: CODE " + code + " BODY <EMPTY>");
            }
        }
        if (authenticator != null) {
            authenticator.handleAuthenticationResult(context, code);
        }
        return response;
    }

    private static void checkInterruption() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    private class QueueRoutine implements Runnable {

        @Override
        public void run() {
            do {
                synchronized (mQueue) {
                    mCurrentCall = null;
                    if (mQueue.size() == 0) {
                        try {
                            mQueue.wait(10000);
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                    if (mQueue.size() == 0) {
                        mCallThread = null;
                        break;
                    }
                    mCurrentCall = mQueue.remove(0);
                }
                final HttpCall<?, ?> call = mCurrentCall;
                final Context context = call.getContext();
                final PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                final PowerManager.WakeLock lock;
                if (powerManager != null) {
                    lock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gdrive:remote");
                    if (lock != null) {
                        lock.acquire(60000L);
                    }
                } else {
                    lock = null;
                }
                final Response response;
                try {
                    response = call(context, call.buildMethod(), call.buildUrl(), call.buildAuthenticator(), call.buildRequestParams(), call.buildRequestHeaders(), call.buildRequestContentType(), call.buildRequestBody());
                    sHandler.post(() -> call.onCompleted(response.code, response.contentType, response.contentEncoding, response.headers, response.body));
                } catch (final InterruptedException e) {
                    sHandler.post(() -> {
                        if (!call.onCancel()) {
                            call.onError(e);
                        }
                    });
                } catch (final NetworkException e) {
                    sHandler.post(() -> {
                        if (!call.onNetworkError(e)) {
                            call.onError(e);
                        }
                    });
                } catch (final AuthenticationException e) {
                    sHandler.post(() -> {
                        if (!call.onAuthenticationError()) {
                            call.onError(e);
                        }
                    });
                } catch (final Exception e) {
                    final boolean networkIssue;
                    if (e instanceof UnknownHostException) {
                        networkIssue = true;
                    } else if (e instanceof SocketException) {
                        networkIssue = true;
                    } else if (e instanceof SocketTimeoutException) {
                        networkIssue = true;
                    } else {
                        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = null;
                        if (cm != null) {
                            netInfo = cm.getActiveNetworkInfo();
                        }
                        networkIssue = !(netInfo != null && netInfo.isConnected());
                    }
                    sHandler.post(() -> {
                        if (!networkIssue || !call.onNetworkError(e)) {
                            call.onError(e);
                        }
                    });
                } finally {
                    if (lock != null) {
                        lock.release();
                    }
                }
            } while (true);
        }
    }

    private static class Response {
        int code;
        String contentType;
        String contentEncoding;
        Map<String, String> headers;
        byte[] body;
    }

    private static final int CONNECT_TIMEOUT_MILLIS = 30000;

    private static final int READ_TIMEOUT_MILLIS = 30000;

}
