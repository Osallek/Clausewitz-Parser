package fr.osallek.clausewitzparser.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ClausewitzUtils {

    private ClausewitzUtils() {
    }

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("y.M.d");

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

    public static String dateToString(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    public static LocalDate stringToDate(String s) {
        s = ClausewitzUtils.removeQuotes(s);
        if (DATE_PATTERN.matcher(s).matches()) {
            return LocalDate.parse(s, DATE_FORMAT);
        } else {
            long dateLong = Long.parseLong(s);
            dateLong /= 24;

            int year = (int) ((dateLong / 365) - 5000);
            LocalDate date = LocalDate.of(year, 1, 1);
            return date.withDayOfYear((int) (dateLong % 365 + 1));
        }
    }

    public static boolean hasQuotes(String s) {
        return s != null && ('"' == s.charAt(0) && '"' == s.charAt(s.length() - 1));
    }

    public static String addQuotes(String s) {
        if (s == null || s.length() < 1) {
            return s;
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
        if (s == null || s.length() < 1) {
            return s;
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
        printEqualsOpen(bufferedWriter, false);
    }

    public static void printEqualsOpen(BufferedWriter bufferedWriter, boolean spaced) throws IOException {
        printEquals(bufferedWriter, spaced);
        printOpen(bufferedWriter);
    }

    public static void printEquals(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("=");
    }

    public static void printEquals(BufferedWriter bufferedWriter, boolean spaced) throws IOException {
        if (spaced) {
            printSpace(bufferedWriter);
            printEquals(bufferedWriter);
            printSpace(bufferedWriter);
        } else {
            printEquals(bufferedWriter);
        }
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
