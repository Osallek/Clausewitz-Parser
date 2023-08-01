package fr.osallek.clausewitzparser.parser;

import fr.osallek.clausewitzparser.common.ClausewitzParseException;
import fr.osallek.clausewitzparser.ic4j.CharsetDetector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
            return parse(reader, new LinkedHashMap<>(), false);
        }
    }

    public static Map<String, Object> parse(BufferedReader reader, Map<String, Object> map, boolean isDot) throws IOException {
        if (reader == null) {
            return map;
        }

        String key = null;
        boolean isEquals = false;

        int letter;
        while ((letter = reader.read()) > -1) {
            if ('\n' == letter && isDot) {
                return map;
            }

            if (Character.isWhitespace(letter)) {
                continue;
            }

            if ('"' == letter && isEquals) {
                map.put(key, ParserUtils.readQuoted(reader, false));
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
                reader.mark(1);

                if ('-' == reader.read()) {
                    ParserUtils.readEndOfLine(reader);
                    continue;
                } else {
                    reader.reset();

                    if (isEquals) {
                        map.put(key, ParserUtils.readNumber(reader, letter));
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
                map.put(key, ParserUtils.readNumber(reader, letter));
                isEquals = false;
                continue;
            }

            key = ParserUtils.readString(reader, letter);
        }

        return map;
    }
}
