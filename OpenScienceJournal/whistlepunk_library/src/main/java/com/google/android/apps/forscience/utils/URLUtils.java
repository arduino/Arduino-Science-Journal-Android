package com.google.android.apps.forscience.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@SuppressWarnings({"WeakerAccess", "unused", "RedundantSuppression"})
public class URLUtils {

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported UTF-8", e);
        }
    }

    public static String decode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported UTF-8", e);
        }
    }

    public static String encodeParameters(Map<String, ?> map) {
        if (map == null) {
            return null;
        }
        if (map.size() == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            final String key = encode(entry.getKey());
            final Object value = entry.getValue();
            if (value instanceof List) {
                List<?> values = (List) value;
                for (Object aux : values) {
                    appendParameter(sb, key, aux);
                }
            } else if (value != null) {
                appendParameter(sb, key, value);
            }
        }
        return sb.toString();
    }

    private static void appendParameter(StringBuilder sb, String key, Object value) {
        if (sb.length() > 0) {
            sb.append('&');
        }
        sb.append(key);
        sb.append('=');
        sb.append(encode(value.toString()));
    }

    public static Map<String, String> decodeParameters(String query) {
        final Map<String, String> map = new HashMap<>();
        if (!StringUtils.isEmpty(query)) {
            final StringTokenizer st = new StringTokenizer(query, "&");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                final int i = token.indexOf('=');
                if (i != -1) {
                    final String key = URLUtils.decode(token.substring(0, i));
                    if (!StringUtils.isEmpty(key)) {
                        map.put(key, URLUtils.decode(token.substring(i + 1)));
                    }
                }
            }
        }
        return map;
    }

}
