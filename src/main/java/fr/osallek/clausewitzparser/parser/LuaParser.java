package fr.osallek.clausewitzparser.parser;

import fr.osallek.clausewitzparser.common.ClausewitzParseException;
import fr.osallek.clausewitzparser.ic4j.CharsetDetector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LuaParser {

    private LuaParser() {}


    public static Map<String, Object> parse(File file) throws IOException {
        try {
            return parse(file, CharsetDetector.detect(new BufferedInputStream(new FileInputStream(file))));
        } catch (IOException | ClausewitzParseException ignored) {
            try {
                return parse(file, StandardCharsets.UTF_8);
            } catch (ClausewitzParseException e) {
                if (CharacterCodingException.class.equals(e.getCause().getClass())) {
                    return parse(file, StandardCharsets.ISO_8859_1);
                } else {
                    throw e;
                }
            }
        }
    }

    public static Map<String, Object> parse(File file, Charset charset) throws IOException {
        return parse(new CharArray(file, charset), new LinkedHashMap<>(), false);
    }

    public static Map<String, Object> parse(CharArray reader, Map<String, Object> map, boolean isDot) {
        if (reader == null) {
            return map;
        }

        String key = null;
        boolean isEquals = false;

        int letter;
        while ((letter = reader.read()) >= 0) {
            if ('\n' == letter && isDot) {
                return map;
            }

            if (Character.isWhitespace(letter)) {
                continue;
            }

            if ('"' == letter && isEquals) {
                map.put(key, reader.readQuoted(false));
                isEquals = false;
                continue;
            }

            if ('{' == letter && isEquals) {
                map.put(key, parse(reader, (Map<String, Object>) map.getOrDefault(key, new LinkedHashMap<>()), false));
                isEquals = false;
                continue;
            }

            if ('.' == letter) {
                map.put(key, parse(reader, (Map<String, Object>) map.getOrDefault(key, new LinkedHashMap<>()), true));

                if (isDot) {
                    return map;
                } else {
                    continue;
                }
            }

            if ('}' == letter) {
                return map;
            }

            if ('-' == letter) {
                int c;

                if ((c = reader.read()) == '-') {
                    reader.readEndOfLine();
                    continue;
                } else {
                    if (isEquals) {
                        map.put(key, reader.readNumber(letter, c));
                        isEquals = false;
                        continue;
                    }
                }
            }

            if ('=' == letter) {
                isEquals = true;
                continue;
            }

            if (Character.isDigit(letter) && isEquals) {
                map.put(key, reader.readNumber(letter));
                isEquals = false;
                continue;
            }

            key = reader.readString(letter);
        }

        return map;
    }
}
