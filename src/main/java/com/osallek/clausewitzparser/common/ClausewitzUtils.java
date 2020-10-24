package com.osallek.clausewitzparser.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ClausewitzUtils {

    private ClausewitzUtils() {
    }

    public static final Charset CHARSET = Charset.forName("windows-1252");

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.M.d");

    public static final Pattern DATE_PATTERN = Pattern.compile("^\\d{1,4}\\.\\d{1,2}\\.\\d{1,2}$");

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

    public static String doubleToString(double value) {
        return String.format(Locale.ENGLISH, "%.3f", value);
    }

    public static String dateToString(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static Date stringToDate(String s) throws ParseException {
        return DATE_FORMAT.parse(s);
    }

    public static boolean hasQuotes(String s) {
        return s != null && ('"' == s.charAt(0) && '"' == s.charAt(s.length() - 1));
    }

    public static String addQuotes(String s) {
        if (s == null) {
            return null;
        }

        if ('"' != s.charAt(0)) {
            s = "\"" + s;
        }

        if ('"' != s.charAt(s.length() - 1)) {
            s = s + "\"";
        }

        return s;
    }

    public static String removeQuotes(String s) {
        if (s == null) {
            return null;
        }

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

    public static boolean hasOnlyOne(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;

                if (count == 2) {
                    return false;
                }
            }
        }

        return count != 0;
    }

    public static void printTabs(BufferedWriter bufferedWriter, int depth) throws IOException {
        bufferedWriter.write(new String(new char[depth]).replace('\0', '\t'));
    }

    public static void printEqualsOpen(BufferedWriter bufferedWriter) throws IOException {
        printEquals(bufferedWriter);
        printOpen(bufferedWriter);
    }

    public static void printEquals(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("=");
    }

    public static void printOpen(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("{");
    }

    public static void printClose(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("}");
    }

    public static void printSpace(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(" ");
    }

}
