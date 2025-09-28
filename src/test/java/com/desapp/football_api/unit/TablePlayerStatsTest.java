package com.desapp.football_api.unit;

import com.desapp.football_api.model.table_player_stats.TablePlayerStats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TablePlayerStatsTest {

    @Test
    void constructor_validBodyText_parsesListCorrectly() {
        String json = "{\"playerTableStats\":[{\"name\":\"Messi\"}]}";
        TablePlayerStats stats = new TablePlayerStats(json);
        assertNotNull(stats.getPlayerTableStats());
        assertEquals(1, stats.getPlayerTableStats().size());
        assertEquals("Messi", stats.getPlayerTableStats().getFirst().getName());
    }

    @Test
    void constructor_invalidBodyText_throwsRuntimeException() {
        String json = "not a json";
        assertThrows(RuntimeException.class, () -> new TablePlayerStats(json));
    }

    @Test
    void playerExists_emptyList_returnsFalse() {
        TablePlayerStats stats = new TablePlayerStats("{\"playerTableStats\":[]}");
        assertFalse(stats.playerExists());
    }

    @Test
    void playerExists_nonEmptyList_returnsTrue() {
        TablePlayerStats stats = new TablePlayerStats("{\"playerTableStats\":[{\"name\":\"Messi\"}]}");
        assertTrue(stats.playerExists());
    }
}