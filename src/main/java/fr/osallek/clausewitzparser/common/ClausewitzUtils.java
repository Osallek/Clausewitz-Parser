package fr.osallek.clausewitzparser.common;

import com.ibm.icu.text.CharsetDetector;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ClausewitzUtils {

    private ClausewitzUtils() {
    }

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("y.M.d");

    public static final Pattern DATE_PATTERN = Pattern.compile("^\\d{1,4}\\.\\d{1,2}\\.\\d{1,2}$");

    public static Charset getCharset(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file); BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
            return Charset.forName(new CharsetDetector().setText(bufferedInputStream).detect().getName());
        }
    }

    public static Charset getCharset(ZipFile zipFile, String entryName) throws IOException {
        ZipEntry zipEntry = zipFile.getEntry(entryName);

        try (InputStream stream = zipFile.getInputStream(zipEntry); BufferedInputStream reader = new BufferedInputStream(stream)) {
            return Charset.forName(new CharsetDetector().setText(reader).detect().getName());
        }
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

    public static String doubleToString(double value) {
        return String.format(Locale.ENGLISH, "%.3f", value);
    }

    public static String dateToString(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    public static LocalDate stringToDate(String s) {
        return LocalDate.parse(s, DATE_FORMAT);
    }

    public static boolean hasQuotes(String s) {
        return s != null && ('"' == s.charAt(0) && '"' == s.charAt(s.length() - 1));
    }

    public static String addQuotes(String s) {
        if (s == null || s.length() < 1) {
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
