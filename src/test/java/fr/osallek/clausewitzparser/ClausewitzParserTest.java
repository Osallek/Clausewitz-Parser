package fr.osallek.clausewitzparser;

import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.clausewitzparser.model.ClausewitzList;
import fr.osallek.clausewitzparser.model.ClausewitzObject;
import fr.osallek.clausewitzparser.model.ClausewitzVariable;
import fr.osallek.clausewitzparser.parser.ClausewitzParser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.zip.ZipFile;

class ClausewitzParserTest {

    static final Path RESOURCE_FOLDER = Path.of("src/test/resources");

    @Test
    void testBinary() throws IOException, ClassNotFoundException {
        File tokens = RESOURCE_FOLDER.resolve("tokens.txt").toFile();
        File file = RESOURCE_FOLDER.resolve("binary_gamestate").toFile();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1);
             FileInputStream tokensFileStream = new FileInputStream(tokens); ObjectInputStream tokensStream = new ObjectInputStream(tokensFileStream)) {
            ClausewitzParser.convertBinary(reader, StandardCharsets.ISO_8859_1, 6, (Map<Integer, String>) tokensStream.readObject());
        }
    }

    @Test
    void testParseFlatSave() {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);
        ClausewitzItem root = ClausewitzParser.parse(RESOURCE_FOLDER.resolve("1_30_4_flat.eu4").toFile(), 1);

        Assertions.assertNotNull(root);

        Assertions.assertEquals("\"d647a13792fa0c47440b944406d7edef\"", root.getVarAsString("checksum"));

        ClausewitzList list = root.getList("mod_enabled");
        Assertions.assertNotNull(list);
        Assertions.assertEquals(3, list.size());
        Assertions.assertTrue(list.contains("\"mod/anbennar_idea_every_tech.mod\""));
        Assertions.assertFalse(list.contains("\"mod/random_mod_name\""));

        ClausewitzItem child = root.getChild("teams");
        Assertions.assertNotNull(child);
        Assertions.assertFalse(child.isEmpty());

        ClausewitzItem grandChild = child.getChild("team");
        Assertions.assertNotNull(grandChild);
        Assertions.assertFalse(grandChild.isEmpty());
        Assertions.assertEquals("\"La thune\"", grandChild.getVarAsString("name"));

        child = root.getChild("countries");
        Assertions.assertNotNull(child);
        Assertions.assertFalse(child.isEmpty());

        grandChild = child.getChild("A85");
        Assertions.assertNotNull(grandChild);
        Assertions.assertFalse(grandChild.isEmpty());
        Assertions.assertEquals(true, grandChild.getVarAsBool("human"));
        Assertions.assertEquals("\"magisterium\"", grandChild.getVarAsString("government_name"));
        Assertions.assertEquals(LocalDate.of(1444, 11, 11), grandChild.getVarAsDate("last_focus_move"));
    }

    @Test
    void testParseCompressedSave() throws IOException {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);

        try (ZipFile zipFile = new ZipFile(RESOURCE_FOLDER.resolve("1_30_4_compressed.eu4").toFile())) {
            ClausewitzItem root = ClausewitzParser.parse(zipFile, "gamestate", 1);

            Assertions.assertNotNull(root);

            Assertions.assertEquals("\"7c8a83d3b91c2349764d14fb48cb1e23\"", root.getVarAsString("checksum"));

            ClausewitzItem child = root.getChild("teams");
            Assertions.assertNotNull(child);
            Assertions.assertFalse(child.isEmpty());

            ClausewitzItem grandChild = child.getChild("team");
            Assertions.assertNotNull(grandChild);
            Assertions.assertFalse(grandChild.isEmpty());
            Assertions.assertEquals("\"La thune\"", grandChild.getVarAsString("name"));

            child = root.getChild("countries");
            Assertions.assertNotNull(child);
            Assertions.assertFalse(child.isEmpty());

            grandChild = child.getChild("A85");
            Assertions.assertNotNull(grandChild);
            Assertions.assertFalse(grandChild.isEmpty());
            Assertions.assertEquals(true, grandChild.getVarAsBool("human"));
            Assertions.assertEquals("\"magisterium\"", grandChild.getVarAsString("government_name"));
            Assertions.assertEquals(LocalDate.of(1444, 11, 11), grandChild.getVarAsDate("last_focus_move"));
        }
    }

    @Test
    void testReadFlatSingleObject() {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);
        ClausewitzObject root = ClausewitzParser.readSingleObject(RESOURCE_FOLDER.resolve("1_30_4_flat.eu4").toFile(), 1, "mod_enabled={");

        Assertions.assertNotNull(root);
        Assertions.assertEquals(ClausewitzList.class, root.getClass());

        ClausewitzList list = (ClausewitzList) root;
        Assertions.assertNotNull(list);
        Assertions.assertEquals(3, list.size());
        Assertions.assertTrue(list.contains("\"mod/anbennar_idea_every_tech.mod\""));
        Assertions.assertFalse(list.contains("\"mod/random_mod_name\""));
    }

    @Test
    void testReadCompressedSingleObject() throws IOException {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);

        try (ZipFile zipFile = new ZipFile(RESOURCE_FOLDER.resolve("1_30_4_compressed.eu4").toFile())) {
            ClausewitzObject root = ClausewitzParser.readSingleObject(zipFile, "meta", 1, "mod_enabled={");

            Assertions.assertNotNull(root);
            Assertions.assertEquals(ClausewitzList.class, root.getClass());

            ClausewitzList list = (ClausewitzList) root;
            Assertions.assertNotNull(list);
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.contains("\"mod/anbennar_idea_every_tech.mod\""));
            Assertions.assertFalse(list.contains("\"mod/random_mod_name\""));
        }
    }

    @Test
    void testParseAreaFile() {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);
        ClausewitzItem root = ClausewitzParser.parse(RESOURCE_FOLDER.resolve("area.txt").toFile(), 0);

        Assertions.assertNotNull(root);

        ClausewitzList list = root.getList("western_mediterrenean_area");
        Assertions.assertNotNull(list);
        Assertions.assertEquals(13, list.size());
        Assertions.assertTrue(list.contains("1293"));
        Assertions.assertFalse(list.contains("1321"));

        ClausewitzItem child = root.getChild("luxemburg_liege_area");
        Assertions.assertNotNull(child);
        Assertions.assertTrue(child.isEmpty());

        child = root.getChild("brittany_area");
        Assertions.assertNotNull(child);
        Assertions.assertFalse(child.isEmpty());

        list = child.getList("color");
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.contains("118"));
        Assertions.assertFalse(list.contains("1321"));

        list = child.getList("");
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.contains("171"));
        Assertions.assertFalse(list.contains("1321"));
    }

    @Test
    void testParseRegionFile() {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);
        ClausewitzItem root = ClausewitzParser.parse(RESOURCE_FOLDER.resolve("region.txt").toFile(), 0);

        Assertions.assertNotNull(root);

        ClausewitzItem child = root.getChild("france_region");
        Assertions.assertNotNull(child);
        Assertions.assertFalse(child.isEmpty());

        ClausewitzList list = child.getList("areas");
        Assertions.assertNotNull(list);
        Assertions.assertEquals(17, list.size());
        Assertions.assertTrue(list.contains("savoy_dauphine_area"));
        Assertions.assertFalse(list.contains("eastern_norway"));

        child = root.getChild("niger_region");
        Assertions.assertNotNull(child);
        Assertions.assertFalse(child.isEmpty());

        list = child.getList("monsoon");
        Assertions.assertNotNull(list);
        Assertions.assertEquals(2, list.size());
        Assertions.assertTrue(list.contains("00.06.01"));
        Assertions.assertFalse(list.contains("00.00.01"));
    }

    @Test
    void testParseStaticModifiersFile() {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);
        ClausewitzItem root = ClausewitzParser.parse(RESOURCE_FOLDER.resolve("static_modifiers.txt").toFile(), 0);

        Assertions.assertNotNull(root);
        Assertions.assertEquals(257, root.getNbObjects());

        ClausewitzItem child = root.getChild("tropical");
        Assertions.assertNotNull(child);
        Assertions.assertEquals(5, child.getNbObjects());
        Assertions.assertNotNull(child.getVar("picture"));
        Assertions.assertEquals("\"climate_tropical\"", child.getVarAsString("picture"));
        Assertions.assertEquals(0.1, child.getVarAsDouble("local_development_cost"));
        Assertions.assertEquals(-0.3, child.getVarAsDouble("supply_limit_modifier"));
        Assertions.assertEquals(-10, child.getVarAsInt("local_colonial_growth"));

        child = root.getChild("hanafi_scholar_modifier");
        Assertions.assertNotNull(child);
        Assertions.assertEquals(5, child.getNbObjects());
        Assertions.assertEquals("\"RELIGIOUS_SCHOLAR_EXPIRY\"", child.getVarAsString("expire_message_type"));
        Assertions.assertEquals(true, child.getVarAsBool("religion"));
    }

    @Test
    void testParseCountriesFile() {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);
        ClausewitzItem root = ClausewitzParser.parse(RESOURCE_FOLDER.resolve("00_countries.txt").toFile(), 0);

        Assertions.assertNotNull(root);
        Assertions.assertEquals(282, root.getNbVariables());

        ClausewitzVariable variable = root.getVar(279);
        Assertions.assertNotNull(variable);
        Assertions.assertEquals("HAH", variable.getName());
        Assertions.assertEquals("countries/Hashshashin.txt", variable.getValue());
    }
}
