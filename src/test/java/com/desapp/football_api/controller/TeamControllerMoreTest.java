package com.desapp.football_api.controller;

import com.desapp.football_api.controller.web_services.TeamController;
import com.desapp.football_api.model.comparison.TeamComparison;
import com.desapp.football_api.model.team.AdvancedMetrics;
import com.desapp.football_api.services.impl.TeamServiceImpl;
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
@WebMvcTest(TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class TeamControllerMoreTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TeamServiceImpl teamServiceImpl;

    @Test
    void getAdvancedMetrics_byId_returnsPayload() throws Exception {
        AdvancedMetrics metrics = new AdvancedMetrics(2.1, 10, 1.0, 15, 5, "WWDLW", null, null);
        when(teamServiceImpl.getAdvancedMetricsById(65L)).thenReturn(metrics);

        mockMvc.perform(get("/api/teams/65/advanced-metrics").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalsPerGame").value(2.1))
                .andExpect(jsonPath("$.goalsConceded").value(10))
                .andExpect(jsonPath("$.recentForm").value("WWDLW"));
    }

    @Test
    void getAdvancedMetrics_byName_returnsPayload() throws Exception {
        AdvancedMetrics metrics = new AdvancedMetrics(1.7, 20, 1.3, -5, 3, "LLDWW", null, null);
        when(teamServiceImpl.getAdvancedMetricsByName("River")).thenReturn(metrics);

        mockMvc.perform(get("/api/teams/advanced-metrics").param("name", "River").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalsPerGame").value(1.7))
                .andExpect(jsonPath("$.goalsConceded").value(20))
                .andExpect(jsonPath("$.goalDifference").value(-5));
    }

    @Test
    void comparison_byNames_returnsData() throws Exception {
        TeamComparison comparison = new TeamComparison(1L, "Juventus", 2L, "Barcelona", null);
        when(teamServiceImpl.getComparisonByTeamNames(eq("Juventus"), eq("Barcelona"), eq(com.desapp.football_api.model.player.StatsType.Current)))
                .thenReturn(comparison);

        mockMvc.perform(get("/api/teams/comparison").param("firstName", "Juventus").param("secondName", "Barcelona"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstTeamName").value("Juventus"))
                .andExpect(jsonPath("$.secondTeamName").value("Barcelona"));
    }

    @Test
    void comparison_byIds_returnsData() throws Exception {
        TeamComparison comparison = new TeamComparison(10L, "River", 20L, "Boca", null);
        when(teamServiceImpl.getComparisonByTeamIds(10L, 20L, com.desapp.football_api.model.player.StatsType.Current))
                .thenReturn(comparison);

        mockMvc.perform(get("/api/teams/10/comparison/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstTeamId").value(10))
                .andExpect(jsonPath("$.secondTeamId").value(20));
    }
}
