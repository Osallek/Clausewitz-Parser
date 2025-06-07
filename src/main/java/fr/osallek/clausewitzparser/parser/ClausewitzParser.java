package fr.osallek.clausewitzparser.parser;

import fr.osallek.clausewitzparser.common.ClausewitzParseException;
import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.clausewitzparser.ic4j.CharsetDetector;
import fr.osallek.clausewitzparser.model.BinaryToken;
import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.clausewitzparser.model.ClausewitzObject;
import fr.osallek.clausewitzparser.model.ClausewitzPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClausewitzParser {

    private ClausewitzParser() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ClausewitzParser.class);

    public static ClausewitzItem parse(File file, int skip) {
        return parse(file, skip, new HashMap<>());
    }

    public static ClausewitzItem parse(File file, int skip, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) {
        try {
            return parse(file, skip, listeners, StandardCharsets.ISO_8859_1);
        } catch (ClausewitzParseException e) {
            if (CharacterCodingException.class.equals(e.getCause().getClass())) {
                try {
                    return parse(file, skip, listeners, StandardCharsets.UTF_8);
                } catch (ClausewitzParseException e1) {
                    try {
                        if (CharacterCodingException.class.equals(e1.getCause().getClass())) {
                            return parse(file, skip, listeners, CharsetDetector.detect(new BufferedInputStream(new FileInputStream(file))));
                        } else {
                            throw e1;
                        }
                    } catch (IOException e2) {
                        LOGGER.error("An error occurred while trying to read file {}: {} !", file.getAbsolutePath(), e2.getMessage(), e2);
                        throw e;
                    }
                }
            } else {
                throw e;
            }
        }
    }

    public static ClausewitzItem parse(File file, int skip, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners, Charset charset) {
        ClausewitzItem root;
        Instant start = Instant.now();

        try {
            root = parse(new CharArray(file, charset), skip, listeners);
        } catch (CharacterCodingException e) {
            throw new ClausewitzParseException(e);
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read file {}: {} !", file.getAbsolutePath(), e.getMessage(), e);
            throw new ClausewitzParseException(e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Time to read {}: {}ms !", file.getName(), Duration.between(start, Instant.now()).toMillis());
        }

        return root;
    }

    public static ClausewitzItem parse(ZipFile zipFile, String entryName, int skip) {
        return parse(zipFile, entryName, skip, new HashMap<>());
    }

    public static ClausewitzItem parse(ZipFile zipFile, String entryName, int skip, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) {
        try {
            return parse(zipFile, entryName, skip, listeners, StandardCharsets.ISO_8859_1);
        } catch (ClausewitzParseException e) {
            if (CharacterCodingException.class.equals(e.getCause().getClass())) {
                return parse(zipFile, entryName, skip, listeners, StandardCharsets.UTF_8);
            } else {
                throw e;
            }
        }
    }

    public static ClausewitzItem parse(ZipFile zipFile, String entryName, int skip, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners,
                                       Charset charset) {
        ClausewitzItem root = null;
        Instant start = Instant.now();

        if (zipFile == null) {
            throw new NullPointerException("zipFile null");
        }

        ZipEntry zipEntry = zipFile.getEntry(entryName);

        if (zipEntry == null) {
            LOGGER.error("Can''t find entry {} in file {} !", entryName, zipFile.getName());
            throw new NullPointerException("No entry");
        }

        try (InputStream stream = zipFile.getInputStream(zipEntry)) {
            root = parse(new CharArray(stream, charset), skip, listeners);
        } catch (CharacterCodingException e) {
            throw new ClausewitzParseException(e);
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read entry {} from file {}: {} !", zipEntry.getName(), zipFile.getName(), e.getMessage(), e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Time to read entry {} of {}: {}ms !", zipEntry, zipFile.getName(), Duration.between(start, Instant.now()).toMillis());
        }

        return root;
    }

    private static ClausewitzItem parse(CharArray reader, int skip, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) {
        for (int i = 1; i <= skip; i++) {
            reader.skipLine();
        }

        ClausewitzItem root = new ClausewitzItem();
        readObject(root, reader, listeners, false);

        return root;
    }

    public static ClausewitzObject readSingleObject(File file, int skip, String objectName) {
        try {
            return readSingleObject(file, skip, objectName, StandardCharsets.ISO_8859_1);
        } catch (ClausewitzParseException e) {
            if (CharacterCodingException.class.equals(e.getCause().getClass())) {
                return readSingleObject(file, skip, objectName, StandardCharsets.UTF_8);
            } else {
                throw e;
            }
        }
    }

    public static ClausewitzObject findFirstSingleObject(File file, int skip, List<String> objectNames) {
        try {
            return findFirstSingleObject(file, skip, objectNames, StandardCharsets.ISO_8859_1);
        } catch (ClausewitzParseException e) {
            if (CharacterCodingException.class.equals(e.getCause().getClass())) {
                return findFirstSingleObject(file, skip, objectNames, StandardCharsets.UTF_8);
            } else {
                throw e;
            }
        }
    }

    public static ClausewitzObject readSingleObject(File file, int skip, String objectName, Charset charset) {
        return findFirstSingleObject(file, skip, List.of(objectName), charset);
    }

    public static ClausewitzObject findFirstSingleObject(File file, int skip, List<String> objectNames, Charset charset) {
        if (objectNames == null) {
            throw new NullPointerException("objectName is null");
        }

        ClausewitzItem root = new ClausewitzItem();

        try {
            readSingleObject(new CharArray(file, charset), skip, root, objectNames);
        } catch (CharacterCodingException e) {
            throw new ClausewitzParseException(e);
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read file {}: {} !", file.getAbsolutePath(), e.getMessage(), e);
            throw new ClausewitzParseException(e);
        }

        return root.isEmpty() ? null : root.getAllOrdered().getFirst();
    }

    public static ClausewitzObject readSingleObjectBinary(ZipFile zipFile, String entryName, int skip, String objectName, Charset charset,
                                                          Map<Integer, String> tokens) {
        return readSingleObjectBinary(zipFile, entryName, skip, List.of(objectName), charset, tokens);
    }

    public static ClausewitzObject readSingleObjectBinary(ZipFile zipFile, String entryName, int skip, List<String> objectNames, Charset charset,
                                                          Map<Integer, String> tokens) {
        if (objectNames == null) {
            throw new NullPointerException("objectName is null");
        }

        if (zipFile == null) {
            throw new NullPointerException("zipFile null");
        }

        ZipEntry zipEntry = zipFile.getEntry(entryName);
        if (zipEntry == null) {
            throw new NullPointerException("zipFile null");
        }

        try (InputStream stream = zipFile.getInputStream(zipEntry)) {
            return convertBinary(new CharArray(stream, charset), charset, skip, tokens, objectNames, new HashMap<>());
        } catch (CharacterCodingException e) {
            throw new ClausewitzParseException(e);
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read entry {} from file {}: {} !", zipEntry.getName(), zipFile.getName(), e.getMessage(), e);
            throw new ClausewitzParseException(e);
        }
    }

    public static ClausewitzObject readSingleObject(ZipFile zipFile, String entryName, int skip, String objectName) {
        try {
            return readSingleObject(zipFile, entryName, skip, List.of(objectName), StandardCharsets.ISO_8859_1);
        } catch (ClausewitzParseException e) {
            if (CharacterCodingException.class.equals(e.getCause().getClass())) {
                return readSingleObject(zipFile, entryName, skip, List.of(objectName), StandardCharsets.UTF_8);
            } else {
                throw e;
            }
        }
    }

    public static ClausewitzObject findFirstSingleObject(ZipFile zipFile, String entryName, int skip, List<String> objectNames) {
        try {
            return readSingleObject(zipFile, entryName, skip, objectNames, StandardCharsets.ISO_8859_1);
        } catch (ClausewitzParseException e) {
            if (CharacterCodingException.class.equals(e.getCause().getClass())) {
                return readSingleObject(zipFile, entryName, skip, objectNames, StandardCharsets.UTF_8);
            } else {
                throw e;
            }
        }
    }

    private static ClausewitzObject readSingleObject(ZipFile zipFile, String entryName, int skip, List<String> objectNames, Charset charset) {
        if (objectNames == null) {
            throw new NullPointerException("objectName is null");
        }

        if (zipFile == null) {
            throw new NullPointerException("zipFile null");
        }

        ClausewitzItem root = new ClausewitzItem();
        ZipEntry zipEntry = zipFile.getEntry(entryName);

        if (zipEntry == null) {
            LOGGER.error("Can't find entry {} in file {}!", entryName, zipFile.getName());
            throw new NullPointerException("No entry");
        }

        try (InputStream stream = zipFile.getInputStream(zipEntry);) {
            readSingleObject(new CharArray(stream, charset), skip, root, objectNames);
        } catch (CharacterCodingException e) {
            throw new ClausewitzParseException(e);
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read entry {} from file {}: {} !", zipEntry.getName(), zipFile.getName(), e.getMessage(), e);
        }

        return root.isEmpty() ? null : root.getAllOrdered().getFirst();
    }

    private static void readSingleObject(CharArray reader, int skip, ClausewitzItem root, List<String> objectNames) {
        for (int i = 1; i <= skip; i++) {
            reader.skipLine();
        }

        String currentLine;

        while (true) {
            int position = reader.position(); //Mark the current char to be able to return here afterward
            currentLine = reader.readLine();

            if (currentLine == null) {
                return;
            }

            boolean found = false;
            currentLine = currentLine.trim();
            for (String objectName : objectNames) {
                if (currentLine.startsWith(objectName)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                reader.position(position);
                break;
            }
        }

        readObject(root, reader, new HashMap<>(), true);
    }

    private static void readObject(ClausewitzPObject currentNode, CharArray reader, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners,
                                   boolean readOnlyOneObject) {
        if (currentNode == null) {
            throw new NullPointerException("node is null");
        }

        int letter;
        List<String> strings = new ArrayList<>(2);
        boolean isEquals = false;
        int nbNewLine = 0;

        while ((letter = reader.read()) >= 0) {
            if (0 == letter) {
                return;
            }

            if ('\n' == letter) {
                nbNewLine++;
                continue;
            }

            if (letter == ' ' || letter == '\t' || letter == '\r') {
                continue;
            }

            if ('#' == letter) {
                reader.skipTillNext('#', true);
                continue;
            }

            if ('"' == letter) {
                if (isEquals) {
                    ((ClausewitzItem) currentNode).addVariable(strings.getFirst(), reader.readQuoted(true));
                    isEquals = false;
                    strings.clear();
                } else {
                    strings.add(reader.readQuoted(true).trim());
                }

                continue;
            }

            if ('=' == letter) {
                isEquals = true;
                continue;
            }

            if ('{' == letter) {
                currentNode = new ClausewitzItem((ClausewitzItem) currentNode, strings.isEmpty() ? "" : strings.getLast(), 0, isEquals);
                ClausewitzPObject finalCurrentNode = currentNode;
                if (!listeners.isEmpty()) {
                    listeners.entrySet()
                             .stream()
                             .filter(entry -> entry.getKey().test(finalCurrentNode))
                             .forEach(entry -> entry.getValue().accept(finalCurrentNode.getName()));
                }
                readObject(currentNode, reader, listeners, false);

                currentNode = currentNode.getParent();
                isEquals = false;

                if (!strings.isEmpty()) {
                    strings.removeLast();
                }

                if (readOnlyOneObject && (currentNode.getParent() == null)) { //Is root node and just read one object
                    break;
                }

                continue;
            }

            if ('}' == letter) {
                if (!strings.isEmpty()) {
                    ClausewitzItem previousItem = currentNode.getParent().getLastChild(currentNode.getName());

                    if (previousItem != null) {
                        if (previousItem.getAllOrdered().isEmpty()) {
                            currentNode = currentNode.getParent()
                                                     .changeChildToList(previousItem.getOrder(), currentNode.getName(), strings.size() > 1 && nbNewLine <= 2,
                                                                        strings);
                        } else {
                            previousItem.addList("", strings.size() > 1 && nbNewLine <= previousItem.getNbObjects() * 2 + 2, false, strings);
                        }
                    } else {
                        currentNode = currentNode.getParent().addList(currentNode.getName(), strings.size() > 1 && nbNewLine <= 2, strings);
                    }
                }

                if (nbNewLine <= 2 && ClausewitzItem.class.equals(currentNode.getClass()) && ((ClausewitzItem) currentNode).getNbChildren() == 0 &&
                    ((ClausewitzItem) currentNode).getNbLists() == 0 && ((ClausewitzItem) currentNode).getNbVariables() > 1) {
                    ((ClausewitzItem) currentNode).setSameLine(true);
                }

                return;
            }

            if (isEquals) { //Value
                if (!strings.isEmpty()) {
                    ((ClausewitzItem) currentNode).addVariable(strings.getFirst(), reader.readStringOrNumber(letter));
                }
                isEquals = false;
                strings.clear();
            } else { //Key
                strings.add(reader.readStringOrNumber(letter));
            }
        }
    }

    public static ClausewitzItem convertBinary(ZipFile zipFile, String entryName, int skip, Map<Integer, String> tokens,
                                               Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners, Charset charset) throws IOException {
        ZipEntry zipEntry = zipFile.getEntry(entryName);

        if (zipEntry == null) {
            LOGGER.error("Can't find entry {} in file {}!", entryName, zipFile.getName());
            throw new NullPointerException("No entry");
        }

        try (InputStream stream = zipFile.getInputStream(zipEntry)) {
            return (ClausewitzItem) convertBinary(new CharArray(stream, charset), charset, skip, tokens, null, listeners);
        }
    }

    public static ClausewitzItem convertBinary(CharArray reader, Charset charset, int skip, Map<Integer, String> tokens) {
        return (ClausewitzItem) convertBinary(reader, charset, skip, tokens, null, new HashMap<>());
    }

    public static ClausewitzObject convertBinary(CharArray reader, Charset charset, int skip, Map<Integer, String> tokens, List<String> objectNames,
                                                 Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) {
        Short token;
        boolean isEquals = false;
        ClausewitzPObject currentNode = new ClausewitzItem();
        List<String> strings = new ArrayList<>();

        char[] skipped = new char[skip]; //Skip leader XXXbin
        reader.read(skipped);

        while ((token = readToken(reader)) != null) {
            BinaryToken binaryToken = BinaryToken.ofToken(token);

            if (binaryToken != null) {
                switch (binaryToken) {
                    case QUOTED_STRING -> {
                        String string = readBinaryString(reader).trim();
                        strings.add(ClausewitzUtils.QUOTE + string + ClausewitzUtils.QUOTE);
                    }
                    case NOT_QUOTED_STRING -> {
                        String string = readBinaryString(reader).trim();
                        strings.add(string);
                    }
                    case UNSIGNED_INT -> {
                        long value = readBinaryUnsignedInt(reader);
                        strings.add(Long.toString(value));
                    }
                    case UNSIGNED_LONG -> {
                        String value = readBinaryUnsignedLong(reader);
                        strings.add(value);
                    }
                    case INT -> {
                        int value = readBinaryInt(reader);
                        strings.add(Integer.toString(value));
                    }
                    case FLOAT -> {
                        float value = readBinaryFloat(reader);
                        strings.add(Float.toString(value));
                    }
                    case DOUBLE -> {
                        double value = readBinaryDouble(reader);
                        strings.add(Double.toString(value));
                    }
                    case BOOL -> {
                        boolean value = readBinaryBool(reader);
                        strings.add(value ? "yes" : "no");
                    }
                    case COLOR -> {
                        String value = readBinaryColor(reader);
                        strings.add(value);
                    }
                    case EQUALS -> {
                        isEquals = true;
                        continue;
                    }
                    case OPEN -> {
                        currentNode = ((ClausewitzItem) currentNode).addChild(strings.isEmpty() ? "" : strings.getLast(), isEquals);
                        if (!strings.isEmpty()) {
                            strings.removeLast();
                        }
                        isEquals = false;
                        ClausewitzPObject finalCurrentNode = currentNode;

                        for (Entry<Predicate<ClausewitzPObject>, Consumer<String>> entry : listeners.entrySet()) {
                            if (entry.getKey().test(finalCurrentNode)) {
                                entry.getValue().accept(finalCurrentNode.getName());
                            }
                        }
                    }
                    case END -> {
                        if (!strings.isEmpty()) {
                            ClausewitzItem previousItem = currentNode.getParent().getLastChild(currentNode.getName());

                            if (previousItem != null) {
                                if (previousItem.getAllOrdered().isEmpty()) {
                                    currentNode = currentNode.getParent()
                                                             .changeChildToList(previousItem.getOrder(), currentNode.getName(), strings.size() > 1, strings);
                                } else {
                                    previousItem.addList("", strings.size() > 1, false, strings);
                                }
                            } else {
                                currentNode = currentNode.getParent().addList(currentNode.getName(), strings.size() > 1, strings);
                            }

                            strings.clear();
                        }

                        if (objectNames != null && objectNames.contains(currentNode.getName())) {
                            return currentNode;
                        }

                        currentNode = currentNode.getParent();
                        isEquals = false;
                    }
                }
            } else {
                String s = tokens.get((int) token);

                if (s == null) {
                    s = new String(tokenToBytes(token), charset);
                }

                strings.add(s);
            }

            if (isEquals) { //Value
                String key = strings.get(0);
                ((ClausewitzItem) currentNode).addVariable(key, strings.get(1));
                isEquals = false;
                strings.clear();

                if (objectNames != null && objectNames.contains(key)) {
                    return ((ClausewitzItem) currentNode).getVar(key);
                }
            }
        }

        while (currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }

        return currentNode;
    }

    private static Short readToken(CharArray reader) {
        byte first = (byte) reader.read();
        byte second = (byte) reader.read();

        if (first == -1 && second == -1) {
            return null;
        }

        return (short) (((second & 0xFF) << 8) + (first & 0xFF));
    }

    private static short readShortLittle(CharArray reader) {
        byte first = (byte) reader.read();
        byte second = (byte) reader.read();

        return (short) (((second & 0xFF) << 8) + (first & 0xFF));
    }

    private static int readIntLittle(CharArray reader) {
        byte first = (byte) reader.read();
        byte second = (byte) reader.read();
        byte third = (byte) reader.read();
        byte forth = (byte) reader.read();

        return (first & 0xFF) | ((second & 0xFF) << 8) | ((third & 0xFF) << 16) | ((forth & 0xFF) << 24);
    }

    private static long readLongLittle(CharArray reader) {
        byte first = (byte) reader.read();
        byte second = (byte) reader.read();
        byte third = (byte) reader.read();
        byte forth = (byte) reader.read();
        byte fifth = (byte) reader.read();
        byte sixth = (byte) reader.read();
        byte seventh = (byte) reader.read();
        byte eighth = (byte) reader.read();

        return ((long) first & 0xFF) | (((long) second & 0xFF) << 8) | (((long) third & 0xFF) << 16) | (((long) forth & 0xFF) << 24) |
               (((long) fifth & 0xFF) << 32) | (((long) sixth & 0xFF) << 40) | (((long) seventh & 0xFF) << 48) | (((long) eighth & 0xFF) << 56);
    }

    private static byte[] tokenToBytes(short token) {
        return new byte[] {(byte) (token & 0xff), (byte) ((token >> 8) & 0xff)};
    }

    private static String readBinaryString(CharArray reader) {
        short len = readShortLittle(reader);

        char[] string = new char[len];
        reader.read(string);
        return new String(string);
    }

    private static boolean readBinaryBool(CharArray reader) {
        return reader.read() == 0;
    }

    private static long readBinaryUnsignedInt(CharArray reader) {
        return Integer.toUnsignedLong(readIntLittle(reader));
    }

    private static int readBinaryInt(CharArray reader) {
        return readIntLittle(reader);
    }

    private static float readBinaryFloat(CharArray reader) {
        return readIntLittle(reader) / 1_000f;
    }

    private static double readBinaryDouble(CharArray reader) {
        double d = readIntLittle(reader) / 65_536d * 2;

        reader.read(4, 0); //for double need to read 8 bytes

        return d;
    }

    private static String readBinaryUnsignedLong(CharArray reader) {
        return Long.toUnsignedString(readLongLittle(reader));
    }

    private static String readBinaryColor(CharArray reader) {
        char[] bytes = new char[22];
        reader.read(bytes);

        return new String(bytes); //Fixme not sure what to do
    }
}
