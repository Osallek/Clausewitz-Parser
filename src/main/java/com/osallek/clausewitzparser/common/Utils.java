package com.osallek.clausewitzparser.common;

public final class Utils {
    private Utils() {
    }

    /*Copied from Apache commons*/
    public static boolean isBlank(final CharSequence cs) {
        int strLen;

        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean hasQuotes(String s) {
        return '"' != s.charAt(0) && '"' != s.charAt(s.length() - 1);
    }
}
