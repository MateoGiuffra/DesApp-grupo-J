package com.desapp.football_api.controller;

import com.desapp.football_api.controller.web_services.PlayerController;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PlayerController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class PlayerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PlayerServiceImpl playerServiceImpl;

    @Test
    void getPlayerByName_returnsPlayer() throws Exception {
        Player p = new Player();
        p.setId(10L);
        p.setFullname("Leo");
        when(playerServiceImpl.getPlayerByNameAndType("Messi", StatsType.Current)).thenReturn(p);

        mockMvc.perform(get("/api/players")
                        .param("name", "Messi")
                        .param("type", "Current")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.fullname").value("Leo"));
    }

    @Test
    void getById_returnsPlayer() throws Exception {
        Player p = new Player();
        p.setId(7L);
        p.setFullname("CR7");
        when(playerServiceImpl.getPlayerByIdAndType(7L, StatsType.Historical)).thenReturn(p);

        mockMvc.perform(get("/api/players/7")
                        .param("type", "Historical")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.fullname").value("CR7"));
    }
}
