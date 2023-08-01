package fr.osallek.clausewitzparser.parser;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;

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

        return keepQuotes ? ClausewitzUtils.addQuotes(quotedString.toString()) : quotedString.toString();
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

            if ('\'' == letter || '/' == letter || '.' == letter || '_' == letter || '-' == letter || ':' == letter || Character.isLetterOrDigit(letter)) {
                quotedString.append((char) letter);
                reader.mark(1);
                continue;
            }

            break;
        }

        reader.reset();

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

    static String readTillNextNoMark(BufferedReader reader, int stopChar, boolean stopEndOfLine) throws IOException {
        int letter;
        StringBuilder stringBuilder = new StringBuilder();

        while ((letter = reader.read()) > -1) {
            stringBuilder.append((char) letter);
            if (stopEndOfLine && '\n' == letter) {
                break;
            }

            if (stopChar == letter) {
                break;
            }
        }

        return stringBuilder.toString();
    }
}
