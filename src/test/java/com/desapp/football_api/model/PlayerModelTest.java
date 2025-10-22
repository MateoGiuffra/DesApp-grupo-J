package com.desapp.football_api.model;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PlayerModelTest {

    @Test
    void nullSafeConvenienceGetters_andTeamIdMapping() {
        Player p = new Player();
        assertNull(p.getGoals());
        assertNull(p.getAssists());
        assertNull(p.getGames());
        assertNull(p.getRating());
        assertNull(p.getTeamId());

        Team t = new Team(55L, "T", null);
        p.setTeam(t);
        assertEquals(55L, p.getTeamId());

        // When stats are set, getters proxy values
        PlayerStats stats = new PlayerStats() {
            @Override
            public void setExtraStats(java.util.List<com.desapp.football_api.model.table_stats.TableStat> tableStats) {}
            @Override
            public String getPlayerLink(Long playerId) { return null; }
        };
        stats.setGoals(10);
        stats.setGames(20);
        stats.setRating(7.31);
        stats.setAssists(5);
        p.setStats(stats);

        assertEquals(10, p.getGoals());
        assertEquals(20, p.getGames());
        assertEquals(7.31, p.getRating());
        assertEquals(5, p.getAssists());
    }
}
