package com.desapp.football_api.unit;

import com.desapp.football_api.model.table_stats.TablePlayerStats;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TablePlayerStatsTest {

    @Test
    void constructor_validBodyText_parsesListCorrectly() {
        String json = "{\"playerTableStats\":[{\"name\":\"Messi\"}]}";
        TablePlayerStats stats = new TablePlayerStats(json);
        assertNotNull(stats.getTableStats());
        assertEquals(1, stats.getTableStats().size());
        assertEquals("Messi", stats.getTableStats().getFirst().getName());
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