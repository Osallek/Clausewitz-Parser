package fr.osallek.clausewitzparser.parser;

import java.io.BufferedReader;
import java.io.IOException;

final class ParserUtils {

    private ParserUtils() {
    }

    static String readQuoted(BufferedReader reader, boolean keepQuotes) throws IOException {
        int letter;
        StringBuilder quotedString = new StringBuilder();

        while ((letter = reader.read()) > -1 && '"' != letter) {
            quotedString.append((char) letter);
        }

        return keepQuotes ? "\"" + quotedString.toString() + "\"" : quotedString.toString();
    }

    static String readString(BufferedReader reader, int firstChar) throws IOException {
        int letter;
        StringBuilder quotedString = new StringBuilder();
        quotedString.append((char) firstChar);
        reader.mark(1);

        while ((letter = reader.read()) > -1) {
            if (Character.isWhitespace(letter)) {
                break;
            }

            if ('_' == letter || Character.isLetterOrDigit(letter)) {
                quotedString.append((char) letter);
                reader.mark(1);
                continue;
            }

            reader.reset();
            break;
        }

        return quotedString.toString();
    }

    static String readStringOrNumber(BufferedReader reader, int firstChar) throws IOException {
        int letter;
        StringBuilder quotedString = new StringBuilder();
        quotedString.append((char) firstChar);
        reader.mark(1);

        while ((letter = reader.read()) > -1) {
            if (Character.isWhitespace(letter)) {
                break;
            }

            if ('.' == letter || '_' == letter || '-' == letter|| Character.isLetterOrDigit(letter)) {
                quotedString.append((char) letter);
                reader.mark(1);
                continue;
            }

            reader.reset();
            break;
        }

        return quotedString.toString();
    }

    static void readEndOfLine(BufferedReader reader) throws IOException {
        readTillNext(reader, '\n', true);
    }

    static Number readNumber(BufferedReader reader, int firstChar) throws IOException {
        int letter;
        boolean isDouble = false;
        StringBuilder numberString = new StringBuilder();
        numberString.append((char) firstChar);

        reader.mark(1);
        while ((letter = reader.read()) > -1 && (Character.isDigit(letter) || '.' == letter)) {
            numberString.append((char) letter);

            if ('.' == letter) {
                isDouble = true;
            }

            reader.mark(1);
        }

        reader.reset();

        if (isDouble) {
            return Double.parseDouble(numberString.toString());
        } else {
            return Integer.parseInt(numberString.toString());
        }
    }

    static void readTillNext(BufferedReader reader, int stopChar, boolean stopEndOfLine) throws IOException {
        int letter;
        reader.mark(1);

        while ((letter = reader.read()) > -1) {
            if (stopEndOfLine && '\n' == letter) {
                break;
            }

            if (stopChar == letter) {
                break;
            }

            reader.mark(1);
        }

        reader.reset();
    }
}