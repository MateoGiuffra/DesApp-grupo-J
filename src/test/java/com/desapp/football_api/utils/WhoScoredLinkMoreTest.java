package com.desapp.football_api.utils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class WhoScoredLinkMoreTest {

    @Test
    void getTeamStatsLink_buildsCorrectUrl() {
        String url = WhoScoredLink.getTeamStatsLink(321L);
        assertEquals("https://es.whoscored.com/statisticsfeed/1/getteamstatistics?category=summaryteam&subcategory=all&statsAccumulationType=0&field=Overall&tournamentOptions=&timeOfTheGameStart=&timeOfTheGameEnd=&teamIds=321&stageId=&sortBy=Rating&sortAscending=&page=1&numberOfTeamsToPick=&isCurrent=true&formation=&incPens=&against=", url);
    }
}
