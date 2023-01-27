package fr.osallek.clausewitzparser.parser;

import fr.osallek.clausewitzparser.common.ClausewitzParseException;
import fr.osallek.clausewitzparser.model.BinaryToken;
import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.clausewitzparser.model.ClausewitzObject;
import fr.osallek.clausewitzparser.model.ClausewitzPObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                return parse(file, skip, listeners, StandardCharsets.UTF_8);
            } else {
                throw e;
            }
        }
    }

    public static ClausewitzItem parse(File file, int skip, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners, Charset charset) {
        ClausewitzItem root;
        Instant start = Instant.now();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
            root = parse(reader, skip, listeners);
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

        try (InputStream stream = zipFile.getInputStream(zipEntry);
             InputStreamReader inputStreamReader = new InputStreamReader(stream, charset);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            root = parse(reader, skip, listeners);
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read entry {} from file {}: {} !", zipEntry.getName(), zipFile.getName(), e.getMessage(), e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Time to read entry {} of {}: {}ms !", zipEntry, zipFile.getName(), Duration.between(start, Instant.now()).toMillis());
        }

        return root;
    }

    private static ClausewitzItem parse(BufferedReader reader, int skip, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException {
        for (int i = 1; i <= skip; i++) {
            reader.readLine();
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

    public static ClausewitzObject readSingleObject(File file, int skip, String objectName, Charset charset) {
        if (objectName == null) {
            throw new NullPointerException("objectName is null");
        }

        ClausewitzItem root = new ClausewitzItem();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
            readSingleObject(reader, skip, root, objectName);
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read file {}: {} !", file.getAbsolutePath(), e.getMessage(), e);
            throw new ClausewitzParseException(e);
        }

        return root.isEmpty() ? null : root.getAllOrdered().get(0);
    }

    public static ClausewitzObject readSingleObjectBinary(ZipFile zipFile, String entryName, int skip, String objectName, Charset charset,
                                                          Map<Integer, String> tokens) {
        if (objectName == null) {
            throw new NullPointerException("objectName is null");
        }

        if (zipFile == null) {
            throw new NullPointerException("zipFile null");
        }

        ZipEntry zipEntry = zipFile.getEntry(entryName);
        if (zipEntry == null) {
            throw new NullPointerException("zipFile null");
        }

        try (InputStream stream = zipFile.getInputStream(zipEntry);
             InputStreamReader inputStreamReader = new InputStreamReader(stream, charset);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            return convertBinary(reader, charset, skip, tokens, objectName, new HashMap<>());
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read entry {} from file {}: {} !", zipEntry.getName(), zipFile.getName(), e.getMessage(), e);
            throw new ClausewitzParseException(e);
        }
    }

    public static ClausewitzObject readSingleObject(ZipFile zipFile, String entryName, int skip, String objectName) {
        try {
            return readSingleObject(zipFile, entryName, skip, objectName, StandardCharsets.ISO_8859_1);
        } catch (ClausewitzParseException e) {
            if (CharacterCodingException.class.equals(e.getCause().getClass())) {
                return readSingleObject(zipFile, entryName, skip, objectName, StandardCharsets.UTF_8);
            } else {
                throw e;
            }
        }
    }

    private static ClausewitzObject readSingleObject(ZipFile zipFile, String entryName, int skip, String objectName, Charset charset) {
        if (objectName == null) {
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

        try (InputStream stream = zipFile.getInputStream(zipEntry);
             InputStreamReader inputStreamReader = new InputStreamReader(stream, charset);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            readSingleObject(reader, skip, root, objectName);
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read entry {} from file {}: {} !", zipEntry.getName(), zipFile.getName(), e.getMessage(), e);
        }

        return root.isEmpty() ? null : root.getAllOrdered().get(0);
    }

    private static void readSingleObject(BufferedReader reader, int skip, ClausewitzItem root, String objectName) throws IOException {
        for (int i = 1; i <= skip; i++) {
            reader.readLine();
        }

        String currentLine;

        while (true) {
            reader.mark(10000); //Mark the current char to be able to return here afterward
            currentLine = reader.readLine();

            if (currentLine == null) {
                return;
            } else if (currentLine.trim().startsWith(objectName)) {
                reader.reset();
                break;
            }
        }

        readObject(root, reader, new HashMap<>(), true);
    }

    private static void readObject(ClausewitzPObject currentNode, BufferedReader reader, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners,
                                   boolean readOnlyOneObject) throws IOException {
        if (currentNode == null) {
            throw new NullPointerException("node is null");
        }

        int letter;
        List<String> strings = new ArrayList<>();
        boolean isEquals = false;
        int nbNewLine = 0;

        while ((letter = reader.read()) > -1) {
            if (Character.isWhitespace(letter)) {
                if ('\n' == letter) {
                    nbNewLine++;
                }

                continue;
            }

            if ('#' == letter) {
                ParserUtils.readTillNext(reader, '#', true);
                continue;
            }

            if ('"' == letter) {
                if (isEquals) {
                    ((ClausewitzItem) currentNode).addVariable(strings.get(0), ParserUtils.readQuoted(reader, true));
                    isEquals = false;
                    strings.clear();
                    continue;
                } else {
                    strings.add(ParserUtils.readQuoted(reader, true).trim());
                    continue;
                }
            }

            if ('=' == letter) {
                isEquals = true;
                continue;
            }

            if ('{' == letter) {
                currentNode = new ClausewitzItem((ClausewitzItem) currentNode, strings.isEmpty() ? "" : strings.get(strings.size() - 1), 0, isEquals);
                ClausewitzPObject finalCurrentNode = currentNode;
                listeners.entrySet()
                         .stream()
                         .filter(entry -> entry.getKey().test(finalCurrentNode))
                         .forEach(entry -> entry.getValue().accept(finalCurrentNode.getName()));
                readObject(currentNode, reader, listeners, false);

                currentNode = currentNode.getParent();
                isEquals = false;

                if (!strings.isEmpty()) {
                    strings.remove(strings.size() - 1);
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
                                                     .changeChildToList(previousItem.getOrder(), currentNode.getName(),
                                                                        strings.size() > 1 && nbNewLine <= 2, strings);
                        } else {
                            previousItem.addList("", strings.size() > 1 && nbNewLine <= previousItem.getNbObjects() * 2 + 2, false, strings);
                        }
                    } else {
                        currentNode = currentNode.getParent().addList(currentNode.getName(), strings.size() > 1 && nbNewLine <= 2, strings);
                    }
                }

                if (nbNewLine <= 2 && ClausewitzItem.class.equals(currentNode.getClass()) && ((ClausewitzItem) currentNode).getNbChildren() == 0
                    && ((ClausewitzItem) currentNode).getNbLists() == 0 && ((ClausewitzItem) currentNode).getNbVariables() > 1) {
                    ((ClausewitzItem) currentNode).setSameLine(true);
                }

                return;
            }

            if (isEquals) { //Value
                if (!strings.isEmpty()) {
                    ((ClausewitzItem) currentNode).addVariable(strings.get(0), ParserUtils.readStringOrNumber(reader, letter).trim());
                }
                isEquals = false;
                strings.clear();
            } else { //Key
                strings.add(ParserUtils.readStringOrNumber(reader, letter).trim());
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

        try (InputStream stream = zipFile.getInputStream(zipEntry);
             InputStreamReader inputStreamReader = new InputStreamReader(stream, charset);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            return convertBinary(reader, charset, skip, tokens, null, listeners);
        }
    }

    public static ClausewitzItem convertBinary(BufferedReader reader, Charset charset, int skip, Map<Integer, String> tokens) throws IOException {
        return convertBinary(reader, charset, skip, tokens, null, new HashMap<>());
    }

    public static ClausewitzItem convertBinary(BufferedReader reader, Charset charset, int skip, Map<Integer, String> tokens, String objectName,
                                               Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException {
        byte[] ch;
        boolean isEquals = false;
        ClausewitzPObject currentNode = new ClausewitzItem();
        List<String> strings = new ArrayList<>();

        char[] skipped = new char[skip]; //Skip leader XXXbin
        reader.read(skipped);

        while ((ch = readToken(reader)) != null) {
            short token = ByteBuffer.wrap(ch).order(ByteOrder.LITTLE_ENDIAN).getShort();

            Optional<BinaryToken> binaryToken = BinaryToken.ofToken(token);

            if (binaryToken.isPresent()) {
                switch (binaryToken.get()) {
                    case QUOTED_STRING -> {
                        String string = readBinaryString(reader, charset).trim();
                        strings.add("\"" + string + "\"");
                    }
                    case NOT_QUOTED_STRING -> {
                        String string = readBinaryString(reader, charset).trim();
                        strings.add(string);
                    }
                    case UNSIGNED_INT -> {
                        long value = readBinaryUnsignedInt(reader, charset);
                        strings.add(Long.toString(value));
                    }
                    case UNSIGNED_LONG -> {
                        String value = readBinaryUnsignedLong(reader, charset);
                        strings.add(value);
                    }
                    case INT -> {
                        long value = readBinaryInt(reader, charset);
                        strings.add(Long.toString(value));
                    }
                    case FLOAT -> {
                        float value = readBinaryFloat(reader, charset);
                        strings.add(Float.toString(value));
                    }
                    case DOUBLE -> {
                        double value = readBinaryDouble(reader, charset);
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
                        currentNode = ((ClausewitzItem) currentNode).addChild(strings.isEmpty() ? "" : strings.get(strings.size() - 1), isEquals);
                        if (!strings.isEmpty()) {
                            strings.remove(strings.size() - 1);
                        }
                        isEquals = false;
                        ClausewitzPObject finalCurrentNode = currentNode;
                        listeners.entrySet()
                                 .stream()
                                 .filter(entry -> entry.getKey().test(finalCurrentNode))
                                 .forEach(entry -> entry.getValue().accept(finalCurrentNode.getName()));
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

                        if (objectName != null && objectName.equals(currentNode.getName())) {
                            return (ClausewitzItem) currentNode;
                        }

                        currentNode = currentNode.getParent();
                        isEquals = false;
                    }
                }
            } else {
                String s = tokens.get((int) token);

                if (s == null) {
                    s = new String(ch, charset);
                }

                strings.add(s);
            }

            if (isEquals) { //Value
                ((ClausewitzItem) currentNode).addVariable(strings.get(0), strings.get(1));
                isEquals = false;
                strings.clear();
            }
        }

        while (currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }

        return (ClausewitzItem) currentNode;
    }

    private static byte[] readToken(BufferedReader reader) throws IOException {
        byte first = (byte) reader.read();
        byte second = (byte) reader.read();

        if (first == -1 && second == -1) {
            return null;
        }

        return new byte[] {first, second};
    }

    private static String readBinaryString(BufferedReader reader, Charset charset) throws IOException {
        char[] lenBytes = new char[2];
        reader.read(lenBytes);
        short len = ByteBuffer.wrap(new String(lenBytes).getBytes(charset)).order(ByteOrder.LITTLE_ENDIAN).getShort();

        char[] string = new char[len];
        reader.read(string);
        return new String(string);
    }

    private static boolean readBinaryBool(BufferedReader reader) throws IOException {
        return reader.read() == 0;
    }

    private static long readBinaryUnsignedInt(BufferedReader reader, Charset charset) throws IOException {
        char[] bytes = new char[4];
        reader.read(bytes);

        return Integer.toUnsignedLong(ByteBuffer.wrap(new String(bytes).getBytes(charset)).order(ByteOrder.LITTLE_ENDIAN).getInt());
    }

    private static long readBinaryInt(BufferedReader reader, Charset charset) throws IOException {
        char[] bytes = new char[4];
        reader.read(bytes);

        return ByteBuffer.wrap(new String(bytes).getBytes(charset)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private static float readBinaryFloat(BufferedReader reader, Charset charset) throws IOException {
        char[] bytes = new char[4];
        reader.read(bytes);

        return ByteBuffer.wrap(new String(bytes).getBytes(charset)).order(ByteOrder.LITTLE_ENDIAN).getInt() / 1_000f;
    }

    private static double readBinaryDouble(BufferedReader reader, Charset charset) throws IOException {
        char[] bytes = new char[8];
        reader.read(bytes);

        return ByteBuffer.wrap(new String(bytes).getBytes(charset)).order(ByteOrder.LITTLE_ENDIAN).getInt() / 65536d * 2;
    }

    private static String readBinaryUnsignedLong(BufferedReader reader, Charset charset) throws IOException {
        char[] bytes = new char[8];
        reader.read(bytes);

        return Long.toUnsignedString(ByteBuffer.wrap(new String(bytes).getBytes(charset)).order(ByteOrder.LITTLE_ENDIAN).getLong());
    }

    private static String readBinaryColor(BufferedReader reader) throws IOException {
        char[] bytes = new char[22];
        reader.read(bytes);

        return new String(bytes); //Fix me not sure what to do
    }
}
