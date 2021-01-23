package fr.osallek.clausewitzparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LuaParser {

    private LuaParser() {}

    public static Map<String, Object> parse(File file) throws IOException {
        return parse(file, false);
    }

    public static Map<String, Object> parse(File file, boolean retryCharset) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), retryCharset ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8)) {
            return parse(reader, new LinkedHashMap<>(), false);
        } catch (CharacterCodingException e) {
            if (!retryCharset) {
                return parse(file, true);
            } else {
                throw e;
            }
        }
    }

    public static Map<String, Object> parse(BufferedReader reader, Map<String, Object> map, boolean isDot) throws IOException {
        if (reader == null) {
            return map;
        }

        String key = null;
        boolean isEquals = false;

        int c;
        while ((c = reader.read()) > -1) {
            if ('\n' == c) {
                if (isDot) {
                    return map;
                }
            }

            if (Character.isWhitespace(c)) {
                continue;
            }

            if ('"' == c) {
                if (isEquals) {
                    map.put(key, readQuoted(reader));
                    isEquals = false;
                    continue;
                }
            }

            if ('{' == c) {
                if (isEquals) {
                    map.put(key, parse(reader, (Map<String, Object>) map.getOrDefault(key, new LinkedHashMap<>()), false));
                    isEquals = false;
                    continue;
                }
            }

            if ('.' == c) {
                map.put(key, parse(reader, (Map<String, Object>) map.getOrDefault(key, new LinkedHashMap<>()), true));

                if (isDot) {
                    return map;
                } else {
                    continue;
                }
            }

            if ('}' == c) {
                return map;
            }

            if ('-' == c) {
                reader.mark(1);

                if ('-' == reader.read()) {
                    readEndOfLine(reader);
                    continue;
                } else {
                    reader.reset();

                    if (isEquals) {
                        map.put(key, readNumber(reader, c));
                        isEquals = false;
                        continue;
                    }
                }
            }

            if ('=' == c) {
                isEquals = true;
                continue;
            }

            if (Character.isDigit(c)) {
                if (isEquals) {
                    map.put(key, readNumber(reader, c));
                    isEquals = false;
                    continue;
                }
            }

            key = readString(reader, c);
        }

        return map;
    }

    private static String readQuoted(BufferedReader reader) throws IOException {
        int c;
        StringBuilder quotedString = new StringBuilder();

        while ((c = reader.read()) > -1 && c != '"') {
            quotedString.append((char) c);
        }

        return quotedString.toString();
    }

    private static String readString(BufferedReader reader, int firstChar) throws IOException {
        int c;
        StringBuilder quotedString = new StringBuilder();
        quotedString.append((char) firstChar);
        reader.mark(1);

        while ((c = reader.read()) > -1) {
            if (Character.isWhitespace(c)) {
                break;
            }


            if ('_' == c || Character.isLetterOrDigit(c)) {
                quotedString.append((char) c);
                reader.mark(1);
                continue;
            }

            reader.reset();
            break;
        }

        return quotedString.toString();
    }

    private static void readEndOfLine(BufferedReader reader) throws IOException {
        int c;
        reader.mark(1);

        while ((c = reader.read()) > -1 && '\n' != c) {
            reader.mark(1);
        }

        reader.reset();
    }

    private static Number readNumber(BufferedReader reader, int firstChar) throws IOException {
        int c;
        boolean isDouble = false;
        StringBuilder numberString = new StringBuilder();
        numberString.append((char) firstChar);

        reader.mark(1);
        while ((c = reader.read()) > -1 && (Character.isDigit(c) || '.' == c)) {
            numberString.append((char) c);

            if ('.' == c) {
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
}
