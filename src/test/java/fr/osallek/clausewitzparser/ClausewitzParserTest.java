package fr.osallek.clausewitzparser;

import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.clausewitzparser.model.ClausewitzList;
import fr.osallek.clausewitzparser.model.ClausewitzObject;
import fr.osallek.clausewitzparser.parser.ClausewitzParser;
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
import java.util.Optional;
import java.util.zip.ZipFile;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertEquals(Optional.of("\"d647a13792fa0c47440b944406d7edef\""), root.getVarAsString("checksum"));

        Optional<ClausewitzList> list = root.getList("mod_enabled");
        Assertions.assertTrue(list.isPresent());
        Assertions.assertEquals(3, list.get().size());
        Assertions.assertTrue(list.get().contains("\"mod/anbennar_idea_every_tech.mod\""));
        Assertions.assertFalse(list.get().contains("\"mod/random_mod_name\""));

        Optional<ClausewitzItem> child = root.getChild("teams");
        Assertions.assertTrue(child.isPresent());
        Assertions.assertFalse(child.get().isEmpty());

        Optional<ClausewitzItem> grandChild = child.get().getChild("team");
        Assertions.assertTrue(grandChild.isPresent());
        Assertions.assertFalse(grandChild.get().isEmpty());
        Assertions.assertEquals(Optional.of("\"La thune\""), grandChild.get().getVarAsString("name"));

        child = root.getChild("countries");
        Assertions.assertTrue(child.isPresent());
        Assertions.assertFalse(child.get().isEmpty());

        grandChild = child.get().getChild("A85");
        Assertions.assertTrue(grandChild.isPresent());
        Assertions.assertFalse(grandChild.get().isEmpty());
        Assertions.assertEquals(Optional.of(true), grandChild.get().getVarAsBool("human"));
        Assertions.assertEquals(Optional.of("\"magisterium\""), grandChild.get().getVarAsString("government_name"));
        Assertions.assertEquals(Optional.of(LocalDate.of(1444, 11, 11)), grandChild.get().getVarAsDate("last_focus_move"));
    }

    @Test
    void testParseCompressedSave() throws IOException {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);

        try (ZipFile zipFile = new ZipFile(RESOURCE_FOLDER.resolve("1_30_4_compressed.eu4").toFile())) {
            ClausewitzItem root = ClausewitzParser.parse(zipFile, "gamestate", 1);

            Assertions.assertNotNull(root);

            Assertions.assertEquals(Optional.of("\"7c8a83d3b91c2349764d14fb48cb1e23\""), root.getVarAsString("checksum"));

            Optional<ClausewitzItem> child = root.getChild("teams");
            Assertions.assertTrue(child.isPresent());
            Assertions.assertFalse(child.get().isEmpty());

            Optional<ClausewitzItem> grandChild = child.get().getChild("team");
            Assertions.assertTrue(grandChild.isPresent());
            Assertions.assertFalse(grandChild.get().isEmpty());
            Assertions.assertEquals(Optional.of("\"La thune\""), grandChild.get().getVarAsString("name"));

            child = root.getChild("countries");
            Assertions.assertTrue(child.isPresent());
            Assertions.assertFalse(child.get().isEmpty());

            grandChild = child.get().getChild("A85");
            Assertions.assertTrue(grandChild.isPresent());
            Assertions.assertFalse(grandChild.get().isEmpty());
            Assertions.assertEquals(Optional.of(true), grandChild.get().getVarAsBool("human"));
            Assertions.assertEquals(Optional.of("\"magisterium\""), grandChild.get().getVarAsString("government_name"));
            Assertions.assertEquals(Optional.of(LocalDate.of(1444, 11, 11)), grandChild.get().getVarAsDate("last_focus_move"));
        }
    }

    @Test
    void testReadFlatSingleObject() {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);
        Optional<ClausewitzObject> root = ClausewitzParser.readSingleObject(RESOURCE_FOLDER.resolve("1_30_4_flat.eu4").toFile(), 1, "mod_enabled={");

        Assertions.assertTrue(root.isPresent());
        Assertions.assertEquals(ClausewitzList.class, root.get().getClass());

        ClausewitzList list = (ClausewitzList) root.get();
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

        Optional<ClausewitzList> list = root.getList("western_mediterrenean_area");
        Assertions.assertTrue(list.isPresent());
        Assertions.assertEquals(13, list.get().size());
        Assertions.assertTrue(list.get().contains("1293"));
        Assertions.assertFalse(list.get().contains("1321"));

        Optional<ClausewitzItem> child = root.getChild("luxemburg_liege_area");
        Assertions.assertTrue(child.isPresent());
        Assertions.assertTrue(child.get().isEmpty());

        child = root.getChild("brittany_area");
        Assertions.assertTrue(child.isPresent());
        Assertions.assertFalse(child.get().isEmpty());

        list = child.get().getList("color");
        Assertions.assertTrue(list.isPresent());
        Assertions.assertTrue(list.get().contains("118"));
        Assertions.assertFalse(list.get().contains("1321"));

        list = child.get().getList("");
        Assertions.assertTrue(list.isPresent());
        Assertions.assertTrue(list.get().contains("171"));
        Assertions.assertFalse(list.get().contains("1321"));
    }

    @Test
    void testParseRegionFile() {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);
        ClausewitzItem root = ClausewitzParser.parse(RESOURCE_FOLDER.resolve("region.txt").toFile(), 0);

        Assertions.assertNotNull(root);

        Optional<ClausewitzItem> child = root.getChild("france_region");
        Assertions.assertTrue(child.isPresent());
        Assertions.assertFalse(child.get().isEmpty());

        Optional<ClausewitzList> list = child.get().getList("areas");
        Assertions.assertTrue(list.isPresent());
        Assertions.assertEquals(17, list.get().size());
        Assertions.assertTrue(list.get().contains("savoy_dauphine_area"));
        Assertions.assertFalse(list.get().contains("eastern_norway"));

        child = root.getChild("niger_region");
        Assertions.assertTrue(child.isPresent());
        Assertions.assertFalse(child.get().isEmpty());

        list = child.get().getList("monsoon");
        Assertions.assertTrue(list.isPresent());
        Assertions.assertEquals(2, list.get().size());
        Assertions.assertTrue(list.get().contains("00.06.01"));
        Assertions.assertFalse(list.get().contains("00.00.01"));
    }

    @Test
    void testParseStaticModifiersFile() {
        Configurator.setLevel(ClausewitzParser.class.getCanonicalName(), Level.DEBUG);
        ClausewitzItem root = ClausewitzParser.parse(RESOURCE_FOLDER.resolve("static_modifiers.txt").toFile(), 0);

        Assertions.assertNotNull(root);
        Assertions.assertEquals(257, root.getNbObjects());

        Optional<ClausewitzItem> child = root.getChild("tropical");
        Assertions.assertTrue(child.isPresent());
        Assertions.assertEquals(5, child.get().getNbObjects());
        Assertions.assertTrue(child.get().hasVar("picture"));
        Assertions.assertEquals(Optional.of("\"climate_tropical\""), child.get().getVarAsString("picture"));
        Assertions.assertEquals(Optional.of(0.1), child.get().getVarAsDouble("local_development_cost"));
        Assertions.assertEquals(Optional.of(-0.3), child.get().getVarAsDouble("supply_limit_modifier"));
        Assertions.assertEquals(Optional.of(-10), child.get().getVarAsInt("local_colonial_growth"));

        child = root.getChild("hanafi_scholar_modifier");
        Assertions.assertTrue(child.isPresent());
        Assertions.assertEquals(5, child.get().getNbObjects());
        Assertions.assertEquals(Optional.of("\"RELIGIOUS_SCHOLAR_EXPIRY\""), child.get().getVarAsString("expire_message_type"));
        Assertions.assertEquals(Optional.of(true), child.get().getVarAsBool("religion"));
    }
}
