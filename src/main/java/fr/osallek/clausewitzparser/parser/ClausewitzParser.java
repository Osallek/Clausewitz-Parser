package fr.osallek.clausewitzparser.parser;

import fr.osallek.clausewitzparser.common.ClausewitzParseException;
import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.clausewitzparser.model.ClausewitzList;
import fr.osallek.clausewitzparser.model.ClausewitzObject;
import fr.osallek.clausewitzparser.model.ClausewitzPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return parse(file, skip, listeners, false);
    }

    private static ClausewitzItem parse(File file, int skip, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners, boolean retryCharset) {
        ClausewitzItem root;
        Instant start = Instant.now();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), retryCharset ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1)) {
            root = parse(reader, skip, listeners);
        } catch (CharacterCodingException e) {
            if (!retryCharset) {
                return parse(file, skip, listeners, true);
            } else {
                throw new ClausewitzParseException(e);
            }
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
        return parse(zipFile, entryName, skip, listeners, false);
    }

    private static ClausewitzItem parse(ZipFile zipFile, String entryName, int skip, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners, boolean retryCharset) {
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
             InputStreamReader inputStreamReader = new InputStreamReader(stream, retryCharset ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            root = parse(reader, skip, listeners);
        } catch (CharacterCodingException e) {
            if (!retryCharset) {
                return parse(zipFile, entryName, skip, listeners, true);
            } else {
                throw new ClausewitzParseException(e);
            }
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
        return readSingleObject(file, skip, objectName, false);
    }

    private static ClausewitzObject readSingleObject(File file, int skip, String objectName, boolean retryCharset) {
        if (objectName == null) {
            throw new NullPointerException("objectName is null");
        }

        ClausewitzItem root = new ClausewitzItem();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), retryCharset ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8)) {
            readSingleObject(reader, skip, root, objectName);
        } catch (CharacterCodingException e) {
            if (!retryCharset) {
                return readSingleObject(file, skip, objectName, true);
            } else {
                throw new ClausewitzParseException(e);
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read file {}: {} !", file.getAbsolutePath(), e.getMessage(), e);
            throw new ClausewitzParseException(e);
        }

        return root.isEmpty() ? null : root.getAllOrdered().get(0);
    }

    public static ClausewitzObject readSingleObject(ZipFile zipFile, String entryName, int skip, String objectName) {
        return readSingleObject(zipFile, entryName, skip, objectName, false);
    }

    private static ClausewitzObject readSingleObject(ZipFile zipFile, String entryName, int skip, String objectName, boolean retryCharset) {
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
             InputStreamReader inputStreamReader = new InputStreamReader(stream, retryCharset ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            readSingleObject(reader, skip, root, objectName);
        } catch (CharacterCodingException e) {
            if (!retryCharset) {
                return readSingleObject(zipFile, entryName, skip, objectName, true);
            } else {
                throw new ClausewitzParseException(e);
            }
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
                            currentNode = currentNode.getParent().changeChildToList(previousItem.getOrder(), currentNode.getName(), strings.size() > 1 && nbNewLine <= 2, strings);
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
                ((ClausewitzItem) currentNode).addVariable(strings.get(0), ParserUtils.readStringOrNumber(reader, letter).trim());
                isEquals = false;
                strings.clear();
            } else { //Key
                strings.add(ParserUtils.readStringOrNumber(reader, letter).trim());
            }
        }
    }
}
