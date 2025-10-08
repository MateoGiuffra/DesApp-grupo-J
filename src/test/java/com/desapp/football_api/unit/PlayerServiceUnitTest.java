package com.desapp.football_api.unit;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.repository.PlayerRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.service.PlayerService;
import com.desapp.football_api.service.StatsService;
import com.desapp.football_api.service.WhoScoredService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PlayerServiceUnitTest {

    @Mock
    WhoScoredService whoScoredService;
    @Mock
    PlayerRepository playerRepository;
    @Mock
    StatsService statsService;
    @Mock
    TeamRepository teamRepository;

    @InjectMocks
    PlayerService playerService;

    @Test
    void getPlayerByName_normalizesName() {
        when(playerRepository.findByFullname("Lionel Messi")).thenReturn(Optional.of(new Player()));
        Player p = playerService.getPlayerByName("lionel mEssi");
        assertNotNull(p);
        verify(playerRepository).findByFullname("Lionel Messi");
    }

//    @Test
//    void createPlayerFromJSON_valid_buildsPlayerAndSaves() {
//        String json = "{" +
//                "\"playerTableStats\":[{" +
//                "\"playerId\":10," +
//                "\"name\":\"Leo\"," +
//                "\"playedPositions\":\"DL\"," +
//                "\"age\":30," +
//                "\"regionCode\":\"AR\"," +
//                "\"teamId\":1," +
//                "\"teamName\":\"PSG\"}]}";
//
//        when(teamRepository.findById(1L)).thenReturn(Optional.empty());
//        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
//        Team team = teamRepository.findById(1L).orElse(null);
//        Player player = playerService.createPlayerFromJSON(json, 10L, StatsType.Current, team);
//
//        assertEquals(10L, player.getId());
//        assertEquals("Leo", player.getFullname());
//        assertEquals("Defender (Left)", player.getPositions());
//        assertEquals("Argentina", player.getNationality());
//        assertNotNull(player.getStats());
//        assertNotNull(player.getTeam());
//
//        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
//        verify(playerRepository).save(captor.capture());
//        assertEquals(10L, captor.getValue().getId());
//    }

//    @Test
//    void createPlayerFromJSON_empty_throwsPlayerNotFound() {
//        String json = "{\"playerTableStats\":[]}";
//        assertThrows(PlayerNotFoundException.class, () -> playerService.createPlayerFromJSON(json, 99L, StatsType.Current));
//    }

    @Test
    void getPlayerById_found_returnsPlayer() {
        Player persisted = new Player();
        persisted.setId(5L);
        when(playerRepository.findById(5L)).thenReturn(Optional.of(persisted));

        Player out = playerService.getPlayerById(5L);
        assertNotNull(out);
        assertEquals(5L, out.getId());
    }
}
