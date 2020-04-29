package com.osallek.clausewitzparser.common;

import com.osallek.clausewitzparser.model.ClausewitzItem;
import com.osallek.clausewitzparser.model.ClausewitzLineType;
import com.osallek.clausewitzparser.model.ClausewitzObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClausewitzUtil {

    private ClausewitzUtil() {
    }

    private static final Logger LOGGER = Logger.getLogger(ClausewitzUtil.class.getName());

    public static ClausewitzItem load(File file, int skip) {
        ClausewitzItem root = null;

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), Charset.forName("windows-1252"))) {
            for (int i = 1; i <= skip; i++) {
                reader.readLine();
            }

            root = new ClausewitzItem();
            readObject(root, null, reader);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred while trying to read file {0}: {1} !", new Object[] {file.getAbsolutePath(), e.getMessage()});
        }

        return root;
    }

    public static ClausewitzItem load(ZipFile zipFile, String entryName, int skip) {
        ClausewitzItem root = null;

        if (zipFile == null) {
            throw new NullPointerException("zipFile null");
        }

        ZipEntry zipEntry = zipFile.getEntry(entryName);

        if (zipEntry == null) {
            LOGGER.log(Level.SEVERE, "Can''t find entry {0} in file {1} !", new Object[] {entryName, zipFile.getName()});
            throw new NullPointerException("No entry");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry), "windows-1252"))) {
            for (int i = 1; i <= skip; i++) {
                reader.readLine();
            }

            root = new ClausewitzItem();
            readObject(root, null, reader);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MessageFormat.format("An error occurred while trying to read entry {0} from file {1}: {2} !", zipEntry
                    .getName(), zipFile.getName(), e.getMessage()), e);
        }

        return root;
    }

    private static void readObject(ClausewitzObject currentNode, ClausewitzLineType previousLineType, BufferedReader reader) throws IOException {
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

            if (Utils.isNotBlank(currentLine) && '#' != currentLine.trim().charAt(0)) { //No blank line or comment line
                String trimmed = currentLine.trim();
                int indexOf;
                if ('{' != trimmed.charAt(trimmed.length() - 1) && trimmed.indexOf('{') >= 0) {
                    //To prevent object written in a single line ie: key={variable=value}
                    indexOf = currentLine.indexOf('{') + 1; //To keep the char at the end of the line
                    currentLine = currentLine.substring(0, indexOf);
                    reader.reset();
                    reader.skip(currentLine.length());
                } else if (!"}".equals(trimmed) && trimmed.indexOf('}') >= 0) {
                    indexOf = currentLine.indexOf('}');
                    if (indexOf == 0) { //Prevent empty line when char it at pos 0
                        indexOf = 1;
                    }

                    currentLine = currentLine.substring(0, indexOf);
                    reader.reset();
                    reader.skip(currentLine.length());
                } else if (trimmed.length() > 1 && '{' == trimmed.charAt(trimmed.length() - 1) &&
                           '{' == trimmed.charAt(trimmed.length() - 2)) {
                    //To prevent line finishing with {{ (no '=' ie: eu4 save map_are_data
                    currentLine = currentLine.substring(0, trimmed.length() - 1);
                    reader.reset();
                    reader.skip(currentLine.length());
                } else if (Utils.hasAtLeast(trimmed, '=', 2)) {
                    indexOf = currentLine.indexOf(' ', currentLine.indexOf('='));
                    currentLine = currentLine.substring(0, indexOf);
                    reader.reset();
                    reader.skip(currentLine.length());
                }

                currentLine = currentLine.trim();

                if ('{' == currentLine.charAt(currentLine.length() - 1)) {
                    //New object
                    ClausewitzItem newChild;

                    if ((indexOf = currentLine.indexOf('=')) >= 0) {
                        newChild = ((ClausewitzItem) currentNode).addChild(currentLine.substring(0, indexOf).trim());
                    } else {
                        newChild = ((ClausewitzItem) currentNode).addChild(currentLine.substring(0,
                                                                                                 currentLine.length() -
                                                                                                 1)
                                                                                      .trim(),
                                                                           false);
                    }

                    previousLineType = ClausewitzLineType.START_OBJECT;
                    readObject(newChild, previousLineType, reader);
                } else if ("}".equals(currentLine)) {
                    //End of object
                    previousLineType = ClausewitzLineType.END_OBJECT;
                    return;
                } else if ((indexOf = currentLine.indexOf('=')) >= 0) {
                    //Variable
                    ((ClausewitzItem) currentNode).addVariable(currentLine.substring(0, indexOf)
                                                                          .trim(), currentLine.substring(indexOf + 1)
                                                                                              .trim());
                } else {
                    //No distinctive sign, value in a list
                    if (!Utils.hasQuotes(currentLine) && currentLine.indexOf(' ') >= 0) {
                        //List on a single line
                        currentNode = ((ClausewitzItem) currentNode.getParent()).addList(currentNode.getName(),
                                                                                         true,
                                                                                         currentLine.split(" "));

                        ((ClausewitzItem) currentNode.getParent()).removeLastChild(currentNode.getName());
                        previousLineType = ClausewitzLineType.LIST_SAME_LINE;
                    } else {
                        //Object list, each line is a value
                        if (ClausewitzLineType.LIST.equals(previousLineType)) {
                            //Appending to an existing list
                            currentNode = ((ClausewitzItem) currentNode.getParent()).addToExistingList(currentNode.getName(), currentLine);
                        } else {
                            //Create a new list in the parent, then delete the current node previously detected and added as an object ie:
                            // key={
                            // value
                            // }
                            currentNode = ((ClausewitzItem) currentNode.getParent()).addList(currentNode.getName(), currentLine);
                            ((ClausewitzItem) currentNode.getParent()).removeLastChild(currentNode.getName());
                        }

                        previousLineType = ClausewitzLineType.LIST;
                    }
                }
            }
        }
    }
}
