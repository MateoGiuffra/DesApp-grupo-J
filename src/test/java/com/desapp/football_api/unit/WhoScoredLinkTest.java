package com.desapp.football_api.unit;

import com.desapp.football_api.utils.WhoScoredLink;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class WhoScoredLinkTest {

    @Test
    void getHistoricalPlayerLink_containsPlayerIdAndParams() {
        String url = WhoScoredLink.getHistoricalPlayerLink(10L);
        assertTrue(url.startsWith("https://es.whoscored.com/statisticsfeed/1/getplayerstatistics"));
        assertTrue(url.contains("playerId=10"));
        assertTrue(url.contains("isCurrent=false"));
    }

    @Test
    void getCurrentPlayerLink_containsPlayerIdAndIsCurrentTrue() {
        String url = WhoScoredLink.getCurrentPlayerLink(99L);
        assertTrue(url.contains("playerId=99"));
        assertTrue(url.contains("isCurrent=true"));
    }

    @Test
    void getTeamLink_containsTeamIdParam() {
        String url = WhoScoredLink.getTeamLink(55L);
        assertTrue(url.startsWith("https://www.whoscored.com/statisticsfeed/1/getplayerstatistics"));
        assertTrue(url.contains("teamIds=55"));
        assertTrue(url.contains("isCurrent=true"));
    }

    @Test
    void getTeamFixturesLink_buildsCorrectPath() {
        String url = WhoScoredLink.getTeamFixturesLink(777L);
        assertEquals("https://es.whoscored.com/teamsfeed/777/fixtures/?field=all&tournamentId=all&type=fixture", url);
    }
}
