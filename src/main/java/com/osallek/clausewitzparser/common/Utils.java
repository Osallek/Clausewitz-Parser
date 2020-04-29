package com.osallek.clausewitzparser.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Utils {
    private Utils() {
    }

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.M.d");

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

    public static String dateToString(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static Date stringToDate(String s) throws ParseException {
        return DATE_FORMAT.parse(s);
    }

    public static boolean hasQuotes(String s) {
        return '"' == s.charAt(0) && '"' == s.charAt(s.length() - 1);
    }

    public static String addQuotes(String s) {
        if ('"' != s.charAt(0)) {
            s = "\"" + s;
        }

        if ('"' != s.charAt(s.length() - 1)) {
            s = s + "\"";
        }

        return s;
    }

    public static String removeQuotes(String s) {
        if ('"' == s.charAt(0)) {
            s = s.substring(1);
        }

        if (s.length() >= 1 && '"' == s.charAt(s.length() - 1)) {
            s = s.substring(0, s.length() - 1);
        }

        return s;
    }

    public static boolean hasAtLeast(String s, char c, int atLeast) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;

                if (count == atLeast) {
                    return true;
                }
            }
        }

        return false;
    }
}
