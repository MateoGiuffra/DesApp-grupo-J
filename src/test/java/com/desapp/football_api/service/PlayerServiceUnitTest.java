package com.desapp.football_api.service;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.repository.PlayerRepository;
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
    PlayerRepository playerRepository;

    @InjectMocks
    PlayerService playerService;

    @Test
    void getPlayerByName_normalizesName() {
        when(playerRepository.findByFullname("Lionel Messi")).thenReturn(Optional.of(new Player()));
        Player p = playerService.getPlayerByName("lionel mEssi");
        assertNotNull(p);
        verify(playerRepository).findByFullname("Lionel Messi");
    }

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
