package com.desapp.football_api.unit;

import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerTest {

    @Test
    void constructor_withValidPlayerTableStats_setsFieldsCorrectly() {
        PlayerTableStat stat = Mockito.mock(PlayerTableStat.class);
        Mockito.when(stat.getApps()).thenReturn(10);
        Mockito.when(stat.getMinsPlayed()).thenReturn(900);
        Mockito.when(stat.getGoal()).thenReturn(5);
        Mockito.when(stat.getAssistTotal()).thenReturn(3);
        Mockito.when(stat.getYellowCard()).thenReturn(2.0);
        Mockito.when(stat.getRedCard()).thenReturn(1.0);
        Mockito.when(stat.getShotsPerGame()).thenReturn(4.5);
        Mockito.when(stat.getPassSuccess()).thenReturn(80.0);
        Mockito.when(stat.getAerialWonPerGame()).thenReturn(1.5);
        Mockito.when(stat.getRating()).thenReturn(7.2);

        Stats player = new Stats(1L, "Leo Messi", "FW", "1987-06-24", "Argentina", "PSG", List.of(stat));

        assertEquals("Leo Messi", player.getFullname());
        assertEquals("FW", player.getPositions());
        assertEquals("1987-06-24", player.getDateOfBirth());
        assertEquals("Argentina", player.getNationality());
        assertEquals("PSG", player.getTeam());
        assertEquals(10, player.getGames());
        assertEquals(900, player.getMins());
        assertEquals(5, player.getGoals());
        assertEquals(3, player.getAssists());
        assertEquals(2, player.getYellowCards());
        assertEquals(1, player.getRedCards());
        assertEquals(4.5, player.getShotsPerGame());
        assertEquals(80.0, player.getPassSuccess());
        assertEquals(1.5, player.getAerialsWonPerGame());
        assertEquals(7.2, player.getRating());
    }

    @Test
    void setPlayerResume_emptyList_setsAllStatsToZero() {
        Stats player = new Stats();
        player.setPlayerResume(Collections.emptyList());

        assertEquals(0, player.getGames());
        assertEquals(0, player.getMins());
        assertEquals(0, player.getGoals());
        assertEquals(0, player.getAssists());
        assertEquals(0, player.getYellowCards());
        assertEquals(0, player.getRedCards());
        assertEquals(0.0, player.getShotsPerGame());
        assertEquals(0.0, player.getPassSuccess());
        assertEquals(0.0, player.getAerialsWonPerGame());
        assertEquals(0.0, player.getRating());
    }

    @Test
    void setPlayerResume_multipleStats_aggregatesAndAveragesCorrectly() {
        PlayerTableStat stat1 = Mockito.mock(PlayerTableStat.class);
        Mockito.when(stat1.getApps()).thenReturn(5);
        Mockito.when(stat1.getMinsPlayed()).thenReturn(450);
        Mockito.when(stat1.getGoal()).thenReturn(2);
        Mockito.when(stat1.getAssistTotal()).thenReturn(1);
        Mockito.when(stat1.getYellowCard()).thenReturn(1.0);
        Mockito.when(stat1.getRedCard()).thenReturn(0.0);
        Mockito.when(stat1.getShotsPerGame()).thenReturn(3.0);
        Mockito.when(stat1.getPassSuccess()).thenReturn(75.0);
        Mockito.when(stat1.getAerialWonPerGame()).thenReturn(1.0);
        Mockito.when(stat1.getRating()).thenReturn(6.8);

        PlayerTableStat stat2 = Mockito.mock(PlayerTableStat.class);
        Mockito.when(stat2.getApps()).thenReturn(7);
        Mockito.when(stat2.getMinsPlayed()).thenReturn(630);
        Mockito.when(stat2.getGoal()).thenReturn(4);
        Mockito.when(stat2.getAssistTotal()).thenReturn(2);
        Mockito.when(stat2.getYellowCard()).thenReturn(2.0);
        Mockito.when(stat2.getRedCard()).thenReturn(1.0);
        Mockito.when(stat2.getShotsPerGame()).thenReturn(5.0);
        Mockito.when(stat2.getPassSuccess()).thenReturn(85.0);
        Mockito.when(stat2.getAerialWonPerGame()).thenReturn(2.0);
        Mockito.when(stat2.getRating()).thenReturn(7.6);

        Stats player = new Stats();
        player.setPlayerResume(Arrays.asList(stat1, stat2));

        assertEquals(12, player.getGames());
        assertEquals(1080, player.getMins());
        assertEquals(6, player.getGoals());
        assertEquals(3, player.getAssists());
        assertEquals(3, player.getYellowCards());
        assertEquals(1, player.getRedCards());
        assertEquals(4.0, player.getShotsPerGame());
        assertEquals(80.0, player.getPassSuccess());
        assertEquals(1.5, player.getAerialsWonPerGame());
        assertEquals(7.2, player.getRating());
    }

    @Test
    void setPlayerResume_statsWithZeroMinutes_shotsAndPassSuccessAreZero() {
        PlayerTableStat stat = Mockito.mock(PlayerTableStat.class);
        Mockito.when(stat.getMinsPlayed()).thenReturn(0);
        Mockito.when(stat.getShotsPerGame()).thenReturn(3.0);
        Mockito.when(stat.getPassSuccess()).thenReturn(70.0);

        Stats player = new Stats();
        player.setPlayerResume(List.of(stat));

        assertEquals(0.0, player.getShotsPerGame());
        assertEquals(0.0, player.getPassSuccess());
    }


}
