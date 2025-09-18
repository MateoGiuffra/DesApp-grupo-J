package model;

import com.desapp.football_api.model.WhoScoredHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhoScoredHelperTest {

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
}
