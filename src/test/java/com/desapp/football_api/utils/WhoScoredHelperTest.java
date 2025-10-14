package com.desapp.football_api.utils;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.desapp.football_api.utils.WhoScoredHelper.roundToTwoDecimals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class WhoScoredHelperTest {

    @Test
    void parseFixtures_manualPayload_buildsMatchesEvenWithMissingValues() throws Exception {
        String payload = "[[1914258,4,'24-05-26','17:00',55,'Valencia',0,65,'Barcelona',0,'vs',,0,0,,'2025/2026','LaLiga','-1',4,206,10803,24622,'SLL','es','es',0,1,0,'España','España','España',,],[1914241,4,'17-05-26','17:00',65,'Barcelona',0,54,'Real Betis',0,'vs',,0,0,,'2025/2026','LaLiga','-1',4,206,10803,24622,'SLL','es','es',0,1,0,'España','España','España',,]]";
        Team team = new Team(65L, "Barcelona", null, new ArrayList<>(), new ArrayList<>());

        List<Match> matches = WhoScoredHelper.parseFixtures(payload, team);

        assertEquals(2, matches.size());
        Match m0 = matches.get(0);
        assertEquals(1914258L, m0.getId());
        assertEquals("Valencia", m0.getHomeTeamName());
        assertEquals("Barcelona", m0.getAwayTeamName());
        assertEquals(java.time.LocalDate.of(2026, 5, 24), m0.getDate());
        assertEquals("17:00", m0.getTime());
        assertEquals("LaLiga", m0.getCompetition());

        Match m1 = matches.get(1);
        assertEquals(1914241L, m1.getId());
        assertEquals("Barcelona", m1.getHomeTeamName());
        assertEquals("Real Betis", m1.getAwayTeamName());
        assertEquals(java.time.LocalDate.of(2026, 5, 17), m1.getDate());
        assertEquals("17:00", m1.getTime());
        assertEquals("LaLiga", m1.getCompetition());
    }

    @Test
    void parseFixtures_handlesEdgeCasesWithSpacesNewlinesAndMissingValues() throws Exception {
        // language=java
        String payload = """
                    [
                    [1914258, 4, "24-05-26", "18:00", 55, "Valencia", 0, 65, "Barcelona", 0, "vs", 0, 0, "2025/2026", "LaLiga", -1, 4, 206, 10803, 24622, "SLL", "es", "es", 0, 1, 0, "España", "España", "España"],
                
                    [1914241, 4, "17-05-26", "17:00", 65, "Barcelona", 0, 54, "Real Betis", 0, "vs", 0, 0, "2025/2026", "LaLiga", -1, 4, 206, 10803, 24622, "SLL", "es", "es", 0, 1, 0, "España", "España", "España"],
                
                    [1914000, 4, "10-05-26", "21:30", 54, "Real Betis", 0, 70, "Atletico Madrid", 0, "vs", 0, "2025/2026", "LaLiga", -1, 4, 206, 10803, 24622, "SLL", "es", "es", 0, 1, 0, "España", "España", "España"]
                    ]
                """;

        Team team = new Team(65L, "Barcelona", null, new ArrayList<>(), new ArrayList<>());

        List<Match> matches = WhoScoredHelper.parseFixtures(payload, team);

        // 1. Asegura que los tres se parsearon correctamente
        assertEquals(3, matches.size(), "Should parse all 3 matches even with messy formatting");

        // 2. Validar el primero
        Match m0 = matches.get(0);
        assertEquals(1914258L, m0.getId());
        assertEquals("Valencia", m0.getHomeTeamName());
        assertEquals("Barcelona", m0.getAwayTeamName());
        assertEquals(java.time.LocalDate.of(2026, 5, 24), m0.getDate());
        assertEquals("18:00", m0.getTime()); // debe limpiar el espacio
        assertEquals("LaLiga", m0.getCompetition());

        // 3. Validar el segundo (espacios, campos vacíos)
        Match m1 = matches.get(1);
        assertEquals(1914241L, m1.getId());
        assertEquals("Barcelona", m1.getHomeTeamName());
        assertEquals("Real Betis", m1.getAwayTeamName()); // con espacio
        assertEquals(java.time.LocalDate.of(2026, 5, 17), m1.getDate());
        assertEquals("17:00", m1.getTime());
        assertEquals("LaLiga", m1.getCompetition());

        // 4. Validar el tercero (mezcla de comillas simples y dobles)
        Match m2 = matches.get(2);
        assertEquals(1914000L, m2.getId());
        assertEquals("Real Betis", m2.getHomeTeamName());
        assertEquals("Atletico Madrid", m2.getAwayTeamName());
        assertEquals(java.time.LocalDate.of(2026, 5, 10), m2.getDate());
        assertEquals("21:30", m2.getTime().replace(" ", "")); // normalizado
        assertEquals("LaLiga", m2.getCompetition());
    }


    @Test
    void testParsePlayedPositions_GK() {
        assertEquals("Goalkeeper", WhoScoredHelper.parsePlayedPositions("GK"));
    }

    @Test
    void testParsePlayedPositions_DefenderWithSides() {
        assertEquals("Defender (Left)", WhoScoredHelper.parsePlayedPositions("DL"));
        assertEquals("Defender (Right)", WhoScoredHelper.parsePlayedPositions("DR"));
        assertEquals("Defender (Centre)", WhoScoredHelper.parsePlayedPositions("DC"));
    }

    @Test
    void testParsePlayedPositions_AttackingMidfielderWithSides() {
        assertEquals("Attacking Midfielder (Left)", WhoScoredHelper.parsePlayedPositions("AML"));
        assertEquals("Attacking Midfielder (Right)", WhoScoredHelper.parsePlayedPositions("AMR"));
        assertEquals("Attacking Midfielder (Centre)", WhoScoredHelper.parsePlayedPositions("AMC"));
    }

    @Test
    void testParsePlayedPositions_DefensiveMidfielder() {
        assertEquals("Defensive Midfielder (Centre)", WhoScoredHelper.parsePlayedPositions("DMC"));
    }

    @Test
    void testParsePlayedPositions_MidfielderWithMultipleSides() {
        assertEquals("Midfielder (Centre, Left)", WhoScoredHelper.parsePlayedPositions("MCL"));
    }

    @Test
    void testParsePlayedPositions_ForwardAndStriker() {
        assertEquals("Forward", WhoScoredHelper.parsePlayedPositions("FW"));
        assertEquals("Striker (Left)", WhoScoredHelper.parsePlayedPositions("STL"));
        assertEquals("Striker (Centre)", WhoScoredHelper.parsePlayedPositions("STC"));
    }

    @Test
    void testParsePlayedPositions_MultipleTokens() {
        assertEquals(
                "Defender (Left), Midfielder (Centre), Attacking Midfielder (Right), Forward",
                WhoScoredHelper.parsePlayedPositions("DL-MC-AMR-FW")
        );
    }

    @Test
    void testParsePlayedPositions_Unknown() {
        assertEquals("Unknown(XX)", WhoScoredHelper.parsePlayedPositions("XX"));
    }

    @Test
    void testParsePlayedPositions_NullOrEmpty() {
        assertEquals("", WhoScoredHelper.parsePlayedPositions(null));
        assertEquals("", WhoScoredHelper.parsePlayedPositions(""));
        assertEquals("", WhoScoredHelper.parsePlayedPositions("   "));
    }

    @Test
    void testParsePlayedPositions_RemoveTrailingHyphens() {
        assertEquals("Defender (Left), Forward", WhoScoredHelper.parsePlayedPositions("-DL-FW-"));
    }

    @Test
    void testCalculateBirthDateByAge() {
        int age = 25;
        String birthDate = WhoScoredHelper.calculateBirthDateByAge(age);
        assertTrue(birthDate.matches("\\d{1,2}/\\d{1,2}/\\d{4}"), "Birthdate format should be d/m/yyyy");
    }

    @Test
    void testGetCountryNameFromCode_Valid() {
        assertEquals("Argentina", WhoScoredHelper.getCountryNameFromCode("AR"));
        assertEquals("Brazil", WhoScoredHelper.getCountryNameFromCode("BR"));
        assertEquals("Spain", WhoScoredHelper.getCountryNameFromCode("ES"));
    }

    @Test
    void testGetCountryNameFromCode_Invalid() {
        assertEquals("Unknown", WhoScoredHelper.getCountryNameFromCode("XX"));
    }

    @Test
    void testGetCountryNameFromCode_LowercaseInput() {
        assertEquals("Argentina", WhoScoredHelper.getCountryNameFromCode("ar"));
    }

    @Test
    void roundToTwoDecimals_variousValues_roundsCorrectly() {
        assertEquals(1.23, roundToTwoDecimals(1.234));
        assertEquals(1.24, roundToTwoDecimals(1.236));
        assertEquals(0.0, roundToTwoDecimals(0.00001));
    }
}
