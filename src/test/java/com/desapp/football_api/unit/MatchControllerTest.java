package com.desapp.football_api.unit;

import com.desapp.football_api.controller.web_services.MatchController;
import com.desapp.football_api.model.Match;
import com.desapp.football_api.service.MatchService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
@WebMvcTest(MatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class MatchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MatchService matchService;

    @Test
    void getUpcomingByTeam_returnsMatches() throws Exception {
        Match m = new Match();
        m.setId(1L);
        when(matchService.getUpcomingMatches(eq(10L))).thenReturn(List.of(m));

        mockMvc.perform(get("/api/matches/10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getUpcomingByTeam_emptyList_ok() throws Exception {
        when(matchService.getUpcomingMatches(eq(99L))).thenReturn(List.of());

        mockMvc.perform(get("/api/matches/99"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
