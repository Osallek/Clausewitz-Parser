package fr.osallek.clausewitzparser;

import fr.osallek.clausewitzparser.common.ClausewitzParseException;
import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.clausewitzparser.model.ClausewitzLineType;
import fr.osallek.clausewitzparser.model.ClausewitzList;
import fr.osallek.clausewitzparser.model.ClausewitzObject;
import fr.osallek.clausewitzparser.model.ClausewitzPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClausewitzParser {

    private ClausewitzParser() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ClausewitzParser.class);

    private static final int MAX_RETRY = 3;

    public static ClausewitzItem parse(File file, int skip) {
        return ClausewitzParser.parse(file, skip, ClausewitzUtils.CHARSET);
    }

    public static ClausewitzItem parse(File file, int skip, Charset charset) {
        return parse(file, skip, charset, new HashMap<>());
    }

    public static ClausewitzItem parse(File file, int skip, Charset charset, Map<Predicate<ClausewitzItem>, Consumer<String>> listeners) {
        return parse(file, skip, charset, listeners, 0);
    }

    private static ClausewitzItem parse(File file, int skip, Charset charset, Map<Predicate<ClausewitzItem>, Consumer<String>> listeners, int retry) {
        ClausewitzItem root = null;

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
            for (int i = 1; i <= skip; i++) {
                reader.readLine();
            }

            root = new ClausewitzItem();
            readObject(root, null, reader, listeners);
        } catch (CharacterCodingException e) {
            if (retry < MAX_RETRY) {
                return parse(file,
                             skip,
                             charset.equals(ClausewitzUtils.CHARSET) ? StandardCharsets.UTF_8 : ClausewitzUtils.CHARSET,
                             listeners,
                             ++retry);
            } else {
                throw new ClausewitzParseException(e);
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read file {}: {} !", file.getAbsolutePath(), e.getMessage(), e);
            throw new ClausewitzParseException(e);
        }

        return root;
    }

    public static ClausewitzItem parse(ZipFile zipFile, String entryName, int skip) {
        return parse(zipFile, entryName, skip, ClausewitzUtils.CHARSET, new HashMap<>());
    }

    public static ClausewitzItem parse(ZipFile zipFile, String entryName, int skip, Charset charset, Map<Predicate<ClausewitzItem>, Consumer<String>> listeners) {
        ClausewitzItem root = null;

        if (zipFile == null) {
            throw new NullPointerException("zipFile null");
        }

        ZipEntry zipEntry = zipFile.getEntry(entryName);

        if (zipEntry == null) {
            LOGGER.error("Can''t find entry {} in file {} !", entryName, zipFile.getName());
            throw new NullPointerException("No entry");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry), charset))) {
            for (int i = 1; i <= skip; i++) {
                reader.readLine();
            }

            root = new ClausewitzItem();
            readObject(root, null, reader, listeners);
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to read entry {} from file {}: {} !", zipEntry.getName(), zipFile.getName(), e.getMessage(), e);
        }

        return root;
    }

    private static void readObject(ClausewitzObject currentNode, ClausewitzLineType previousLineType, BufferedReader reader,
                                   Map<Predicate<ClausewitzItem>, Consumer<String>> listeners) throws IOException {
        if (currentNode == null) {
            throw new NullPointerException("node is null");
        }

        String currentLine;

        while (true) {
            reader.mark(10000); //Mark the current char to be able to return here afterward
            currentLine = reader.readLine();

            if (currentLine == null) {
                break;
            }

            //No blank line or comment line
            if (ClausewitzUtils.isNotBlank(currentLine) && '#' != currentLine.trim().charAt(0)) {
                int indexOf;
                int trimmedIndexOf;
                if ((indexOf = currentLine.indexOf('#')) >= 0) {
                    if (ClausewitzUtils.hasAtLeast(currentLine, '#', 2)) {
                        String[] splits = currentLine.split("#");
                        currentLine = "";
                        for (int i = 0; i < splits.length; i += 2) {
                            currentLine += splits[i] + " ";
                        }
                    } else {
                        currentLine = currentLine.substring(0, indexOf);
                    }
                }

                if (ClausewitzUtils.hasOnlyOne(currentLine, '"')) {
                    currentLine += '\n' + reader.readLine();
                }

                String trimmed = currentLine.trim();

                if (trimmed.charAt(trimmed.length() - 1) == '=') {
                    currentLine += '\n' + reader.readLine();
                    trimmed = currentLine.trim();
                }

                if (('{' != trimmed.charAt(trimmed.length() - 1) && (trimmedIndexOf = trimmed.indexOf('{')) >= 0)
                    || ClausewitzUtils.hasAtLeast(trimmed, '{', 2)) {
                    //To prevent object written in a single line ie: key={variable=value}
                    indexOf = currentLine.indexOf('{') + 1; //To keep the char at the end of the line
                    currentLine = currentLine.substring(0, indexOf);
                    reader.reset();
                    reader.skip(currentLine.length());
                } else if (trimmed.length() > 1 && '{' == trimmed.charAt(trimmed.length() - 1) &&
                           '{' == trimmed.charAt(trimmed.length() - 2)) {
                    //To prevent line finishing with {{ (no '=' ie: eu4 save map_are_data
                    currentLine = currentLine.substring(0, trimmed.length() - 1);
                    reader.reset();
                    reader.skip(currentLine.length());
                } else if (ClausewitzUtils.hasAtLeast(trimmed, '=', 2)) {
                    //Get first whitespace index
                    indexOf = -1;

                    int index = currentLine.indexOf('=') + 1;
                    while (Character.isWhitespace(currentLine.charAt(index))) {//Pretrim to prevent 'X = X' vs 'X=X'
                        index++;
                    }

                    for (; index < currentLine.length(); index++) {
                        if (Character.isWhitespace(currentLine.charAt(index))) {
                            indexOf = index;
                            break;
                        }
                    }

                    currentLine = currentLine.substring(0, indexOf);
                    reader.reset();
                    reader.skip(currentLine.length());
                    previousLineType = ClausewitzLineType.SAME_LINE_OBJECT;
                } else if (!"}".equals(trimmed) && (trimmedIndexOf = trimmed.indexOf('}')) >= 0) {
                    indexOf = currentLine.indexOf('}');
                    if (trimmedIndexOf == 0) { //Prevent empty line when char it at pos 0
                        trimmedIndexOf = 1;
                        indexOf += 1;
                    }

                    currentLine = trimmed.substring(0, trimmedIndexOf);
                    reader.reset();
                    reader.skip(indexOf);
                }

                currentLine = currentLine.trim();

                if ('{' == currentLine.charAt(currentLine.length() - 1)) {
                    //New object
                    ClausewitzItem newChild;

                    if ((indexOf = currentLine.indexOf('=')) >= 0) {
                        newChild = ((ClausewitzItem) currentNode).addChild(currentLine.substring(0, indexOf).trim());
                    } else {
                        newChild = ((ClausewitzItem) currentNode).addChild(currentLine.substring(0, currentLine.length() - 1).trim(), false);
                    }

                    previousLineType = ClausewitzLineType.START_OBJECT;
                    listeners.entrySet().stream().filter(entry -> entry.getKey().test(newChild)).forEach(entry -> entry.getValue().accept(newChild.getName()));
                    readObject(newChild, previousLineType, reader, listeners);
                } else if ("}".equals(currentLine)) {
                    //End of object
                    previousLineType = ClausewitzLineType.END_OBJECT;
                    return;
                } else if ((indexOf = currentLine.indexOf('=')) >= 0) {
                    //Variable
                    ((ClausewitzItem) currentNode).addVariable(currentLine.substring(0, indexOf).trim(), currentLine.substring(indexOf + 1).trim());

                    if (ClausewitzLineType.SAME_LINE_OBJECT.equals(previousLineType)) {
                        ((ClausewitzItem) currentNode).setSameLine(true);
                    } else {
                        previousLineType = ClausewitzLineType.VAR;
                    }
                } else {
                    //No distinctive sign, value in a list
                    if ((!ClausewitzUtils.hasQuotes(currentLine) || ClausewitzUtils.hasAtLeast(currentLine, '"', 3))
                        && currentLine.indexOf(' ') >= 0) {
                        //List on a single line
                        if (ClausewitzLineType.LIST_SAME_LINE.equals(previousLineType) || ClausewitzLineType.LIST.equals(previousLineType)) {
                            ((ClausewitzList) currentNode).addAll(splitSameLine(currentLine));
                        } else {
                            ClausewitzItem previousItem = ((ClausewitzItem) ((ClausewitzPObject) currentNode).getParent()).getLastChild(currentNode.getName());

                            if (previousItem != null) {
                                currentNode = ((ClausewitzItem) ((ClausewitzPObject) currentNode).getParent()).changeChildToList(previousItem.getOrder(),
                                                                                                                                 currentNode.getName(),
                                                                                                                                 true,
                                                                                                                                 splitSameLine(currentLine));
                            } else {
                                currentNode = ((ClausewitzItem) ((ClausewitzPObject) currentNode).getParent()).addList(currentNode.getName(),
                                                                                                                       true,
                                                                                                                       splitSameLine(currentLine));
                            }
                        }

                        previousLineType = ClausewitzLineType.LIST_SAME_LINE;
                    } else {
                        //Object list, each line is a value
                        if (ClausewitzLineType.LIST_SAME_LINE.equals(previousLineType) || ClausewitzLineType.LIST.equals(previousLineType)) {
                            //Appending to an existing list
                            currentNode = ((ClausewitzItem) ((ClausewitzPObject) currentNode).getParent()).addToLastExistingList(currentNode.getName(),
                                                                                                                                 splitSameLine(currentLine));
                        } else {
                            //Create a new list in the parent, then delete the current node previously detected and added as an object ie: key={ value }
                            ClausewitzItem previousItem = ((ClausewitzItem) ((ClausewitzPObject) currentNode).getParent()).getLastChild(currentNode.getName());

                            if (previousItem != null) {
                                currentNode = ((ClausewitzItem) ((ClausewitzPObject) currentNode).getParent()).changeChildToList(previousItem.getOrder(),
                                                                                                                                 currentNode.getName(),
                                                                                                                                 splitSameLine(currentLine));
                            } else {
                                currentNode = ((ClausewitzItem) ((ClausewitzPObject) currentNode).getParent()).addList(currentNode.getName(),
                                                                                                                       splitSameLine(currentLine));
                            }
                        }

                        previousLineType = ClausewitzLineType.LIST;
                    }
                }
            }
        }
    }

    private static String[] splitSameLine(String line) {
        String regex = "\"([^\"]*)\"|(\\S+)";
        List<String> strings = new ArrayList<>();

        Matcher m = Pattern.compile(regex).matcher(line);
        while (m.find()) {
            if (m.group(1) != null) {
                strings.add(ClausewitzUtils.addQuotes(m.group(1)));
            } else {
                strings.add(m.group(2));
            }
        }

        return strings.toArray(new String[0]);
    }
}
