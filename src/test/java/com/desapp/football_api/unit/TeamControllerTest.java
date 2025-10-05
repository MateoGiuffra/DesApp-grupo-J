package com.desapp.football_api.unit;

import com.desapp.football_api.controller.web_services.TeamController;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.service.TeamService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@WebMvcTest(TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class TeamControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TeamService teamService;

    @Test
    void getTeamById_withFieldsSquad_returnsOnlySquad() throws Exception {
        Player p = new Player();
        p.setId(1L);
        p.setFullname("P1");
        Team t = new Team(66L, "Team", List.of(p));
        when(teamService.getPlayersByTeamId(eq(66L), eq(StatsType.Current))).thenReturn(t);

        mockMvc.perform(get("/api/teams/66")
                        .param("fields", "squad")
                        .param("type", "Current")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullname").value("P1"));
    }

    @Test
    void getTeamByName_returnsTeamDto() throws Exception {
        Team t = new Team(10L, "River", List.of());
        when(teamService.getPlayersByTeamName(eq("River"), eq(StatsType.Current))).thenReturn(t);

        mockMvc.perform(get("/api/teams/search")
                        .param("name", "River")
                        .param("type", "Current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("River"));
    }
}
