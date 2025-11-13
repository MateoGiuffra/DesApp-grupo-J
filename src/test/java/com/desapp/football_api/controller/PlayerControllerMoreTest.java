package com.desapp.football_api.controller;

import com.desapp.football_api.controller.web_services.PlayerController;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.player_stats.CurrentStats;
import com.desapp.football_api.model.team.Team;
import com.desapp.football_api.services.impl.PlayerServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PlayerController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class PlayerControllerMoreTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PlayerServiceImpl playerServiceImpl;

    @Test
    void getPlayerByName_defaultTypeIsCurrent_whenOmitted() throws Exception {
        Player p = new Player();
        p.setId(9L);
        p.setFullname("Default");
        when(playerServiceImpl.getPlayerByNameAndType(eq("Any"), eq(StatsType.Current))).thenReturn(p);

        mockMvc.perform(get("/api/players").param("name", "Any").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.fullname").value("Default"));
    }

    @Test
    void getById_returnsPlayerDTO_withTeamAndStats() throws Exception {
        Player p = new Player();
        p.setId(77L);
        p.setFullname("Player 77");
        Team team = new Team(123L, "T", null);
        p.setTeam(team);
        CurrentStats stats = new CurrentStats();
        stats.setAssists(4);
        stats.setGoals(12);
        stats.setRating(7.5);
        stats.setGames(30);
        p.setStats(stats);

        when(playerServiceImpl.getPlayerByIdAndType(77L, StatsType.Current)).thenReturn(p);

        mockMvc.perform(get("/api/players/77").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.fullname").value("Player 77"))
                .andExpect(jsonPath("$.teamId").value(123))
                .andExpect(jsonPath("$.assists").value(4))
                .andExpect(jsonPath("$.goals").value(12))
                .andExpect(jsonPath("$.rating").value(7.5))
                .andExpect(jsonPath("$.games").value(30));
    }
}
