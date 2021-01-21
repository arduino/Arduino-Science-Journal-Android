package com.google.android.apps.forscience.whistlepunk.remote;

@SuppressWarnings({"WeakerAccess", "unused", "RedundantSuppression"})
public class StringUtils {

    public static String emptyIfNull(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }

    public static String nullIfEmpty(String str) {
        if (str != null && str.length() == 0) {
            return null;
        }
        return str;
    }

    public static String nullSafeTrim(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }

    public static boolean nullSafeEquals(String str1, String str2) {
        return str1 == null && str2 == null || !(str1 == null || str2 == null) && str1.equals(str2);
    }

    public static boolean nullSafeEqualsIgnoreCase(String str1, String str2) {
        return str1 == null && str2 == null || !(str1 == null || str2 == null) && str1.equalsIgnoreCase(str2);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    public static String firstNotEmpty(String... strs) {
        for (final String str : strs) {
            if (!StringUtils.isEmpty(str)) {
                return str;
            }
        }
        return "";
    }

    public static String initials(String str) {
        final StringBuilder b = new StringBuilder();
        if (str != null) {
            final int length = str.length();
            boolean add = true;
            for (int i = 0; i < length; i++) {
                char c = str.charAt(i);
                if (c == ' ' || c == '_' || c == '-') {
                    add = true;
                } else if (add) {
                    b.append(c);
                    add = false;
                }
            }
        }
        return b.toString();
    }

}
