package com.desapp.football_api.unit;

import com.desapp.football_api.model.stats.player_stats.CurrentStats;
import com.desapp.football_api.model.table_stats.TableStat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class StatsAggregationTest {

    private TableStat stat(int apps, int mins, int goals, int assists,
                           double yellow, double red, double shotsPerGame,
                           double passSuccess, double aerialWon, double rating) {
        TableStat s = new TableStat();
        s.setApps(apps);
        s.setMinsPlayed(mins);
        s.setGoal(goals);
        s.setAssistTotal(assists);
        s.setYellowCard(yellow);
        s.setRedCard(red);
        s.setShotsPerGame(shotsPerGame);
        s.setPassSuccess(passSuccess);
        s.setAerialWonPerGame(aerialWon);
        s.setRating(rating);
        return s;
    }

    @Test
    void setPlayerResume_aggregatesAndRoundsCorrectly() {
        TableStat s1 = stat(10, 900, 5, 3, 2, 0, 3.2, 85.4, 1.1, 7.45);
        TableStat s2 = stat(5, 450, 2, 1, 1, 1, 2.8, 79.6, 0.9, 6.80);
        TableStat s3NoMins = stat(1, 0, 0, 0, 0, 0, 9.9, 99.9, 0.0, 0.0); // ignored for some avgs

        CurrentStats stats = new CurrentStats(List.of(s1, s2, s3NoMins));

        assertEquals(16, stats.getGames()); // 10 + 5 + 1
        assertEquals(1350, stats.getMins()); // 900 + 450 + 0
        assertEquals(7, stats.getGoals()); // 5 + 2
        assertEquals(4, stats.getAssists()); // 3 + 1
        assertEquals(3, stats.getYellowCards()); // 2 + 1 (cast)
        assertEquals(1, stats.getRedCards()); // 0 + 1 (cast)

        // shotsPerGame: average across entries with mins>0: (3.2 + 2.8)/2 = 3.0 -> 3.0
        assertEquals(3.0, stats.getShotsPerGame());
        // passSuccess: average of passSuccess across entries with mins>0 rounded to 2 decimals: (85.4 + 79.6)/2 = 82.5
        assertEquals(82.5, stats.getPassSuccess());
        // aerialsWon: average across all entries rounded: (1.1 + 0.9 + 0.0)/3 = 0.666.. -> 0.67
        assertEquals(0.67, stats.getAerialsWonPerGame());
        // rating: average of rating >0 values rounded: (7.45 + 6.80)/2 = 7.125 -> 7.13
        assertEquals(7.13, stats.getRating());
    }
}
