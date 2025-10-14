package com.desapp.football_api.model;

import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.table_stats.TableStat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TeamStatsModelTest {

    @Test
    void aggregates_areAveragedAndScaledAndRounded() {
        TableStat a = new TableStat();
        a.setPossession(0.501); // 50.1%
        a.setPassSuccess(0.8123); // 81.23%
        a.setShotsPerGame(12.3456);

        TableStat b = new TableStat();
        b.setPossession(0.499); // 49.9%
        b.setPassSuccess(0.7877); // 78.77%
        b.setShotsPerGame(7.6543);

        TeamStats ts = new TeamStats(List.of(a, b));

        // possession = avg(50.1, 49.9) = 50.0 -> rounded 50.0
        assertEquals(50.0, ts.getPossession());
        // passSuccess = avg(81.23, 78.77) = 80.0 -> rounded 80.0
        assertEquals(80.0, ts.getPassSuccess());
        // shots = avg(12.3456, 7.6543) = 9.99995 -> ~10.0 rounded to two decimals
        assertEquals(10.0, ts.getShotsPerGame());
    }
}
