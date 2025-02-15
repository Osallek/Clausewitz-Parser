package fr.osallek.clausewitzparser;

import fr.osallek.clausewitzparser.parser.LuaParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

class LuaParserTest {

    static final Path RESOURCE_FOLDER = Path.of("src", "test", "resources");

    @Test
    void testParseDefines() throws IOException {
        Map<String, Object> map = LuaParser.parse(RESOURCE_FOLDER.resolve("defines.lua").toFile());

        Assertions.assertNotNull(map);
        Assertions.assertEquals(1, map.size());

        Object defines = map.get("NDefines");
        Assertions.assertNotNull(defines);
        Assertions.assertInstanceOf(Map.class, defines);

        Map<String, Object> definesMap = (Map<String, Object>) defines;
        Assertions.assertEquals(15, definesMap.size());

        Object game = definesMap.get("NGame");
        Assertions.assertNotNull(game);
        Assertions.assertInstanceOf(Map.class, game);

        Map<String, Object> gameMap = (Map<String, Object>) game;
        Assertions.assertEquals(16, gameMap.size());

        Object startDate = gameMap.get("START_DATE");
        Assertions.assertNotNull(startDate);
        Assertions.assertInstanceOf(String.class, startDate);
        Assertions.assertEquals("1444.11.11", startDate);

        Object ageUsherInTime = gameMap.get("AGE_USHER_IN_TIME");
        Assertions.assertNotNull(ageUsherInTime);
        Assertions.assertInstanceOf(Integer.class, ageUsherInTime);
        Assertions.assertEquals(120, ageUsherInTime);

        Object diplomacy = definesMap.get("NDiplomacy");
        Assertions.assertNotNull(diplomacy);
        Assertions.assertInstanceOf(Map.class, diplomacy);

        Map<String, Object> diplomacyMap = (Map<String, Object>) diplomacy;
        Assertions.assertEquals(341, diplomacyMap.size());

        Object pressSailorsFraction = diplomacyMap.get("PRESS_SAILORS_FRACTION");
        Assertions.assertNotNull(pressSailorsFraction);
        Assertions.assertInstanceOf(Double.class, pressSailorsFraction);
        Assertions.assertEquals(0.2, pressSailorsFraction);

        Object tradeLeagueBreakOpinion = diplomacyMap.get("TRADE_LEAGUE_BREAK_OPINION");
        Assertions.assertNotNull(tradeLeagueBreakOpinion);
        Assertions.assertInstanceOf(Integer.class, tradeLeagueBreakOpinion);
        Assertions.assertEquals(-50, tradeLeagueBreakOpinion);
    }

    @Test
    void testParseFlat() throws IOException {
        Map<String, Object> map = LuaParser.parse(RESOURCE_FOLDER.resolve("flat.lua").toFile());

        Assertions.assertNotNull(map);
        Assertions.assertEquals(1, map.size());

        Object defines = map.get("NDefines");
        Assertions.assertNotNull(defines);
        Assertions.assertInstanceOf(Map.class, defines);

        Map<String, Object> definesMap = (Map<String, Object>) defines;
        Assertions.assertEquals(5, definesMap.size());

        Object diplomacy = definesMap.get("NDiplomacy");
        Assertions.assertNotNull(diplomacy);
        Assertions.assertInstanceOf(Map.class, diplomacy);

        Map<String, Object> diplomacyMap = (Map<String, Object>) diplomacy;
        Assertions.assertEquals(10, diplomacyMap.size());

        Object changeRivalYears = diplomacyMap.get("CHANGE_RIVAL_YEARS");
        Assertions.assertNotNull(changeRivalYears);
        Assertions.assertInstanceOf(Integer.class, changeRivalYears);
        Assertions.assertEquals(10, changeRivalYears);

        Object celestialEmpireMandatePerStability = diplomacyMap.get("CELESTIAL_EMPIRE_MANDATE_PER_STABILITY");
        Assertions.assertNotNull(celestialEmpireMandatePerStability);
        Assertions.assertInstanceOf(Double.class, celestialEmpireMandatePerStability);
        Assertions.assertEquals(1.2, celestialEmpireMandatePerStability);
    }
}
