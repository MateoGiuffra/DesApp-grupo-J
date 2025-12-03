package com.desapp.football_api.controller;

import com.desapp.football_api.config.SecurityConfig;
import com.desapp.football_api.controller.web_services.PredictionController;
import com.desapp.football_api.model.prediction.PredictionResult;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.services.PredictionService;
import com.desapp.football_api.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PredictionController.class)
@Import(SecurityConfig.class)
class PredictionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PredictionService predictionService;

    // Beans requeridos por SecurityConfig
    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    UserServiceImpl userServiceImpl;

    @BeforeEach
    void setup() {
        Mockito.reset(predictionService, jwtUtil, userServiceImpl);
    }

    private PredictionResult sampleResult() {
        PredictionResult r = new PredictionResult();
        r.setHomeTeamId(1L);
        r.setAwayTeamId(2L);
        r.setHomeWinProbability(0.5);
        r.setAwayWinProbability(0.2);
        r.setDrawProbability(0.3);
        return r;
    }

    @Test
    @WithMockUser
    void predict_byIds_returnsResult() throws Exception {
        when(predictionService.prediccionPoisson(anyLong(), anyLong())).thenReturn(sampleResult());

        mockMvc.perform(get("/predictions/1/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeTeamId").value(1))
                .andExpect(jsonPath("$.awayTeamId").value(2))
                .andExpect(jsonPath("$.drawProbability").value(0.3));
    }

    @Test
    @WithMockUser
    void predict_byNames_returnsResult() throws Exception {
        when(predictionService.prediccionPoisson(anyString(), anyString())).thenReturn(sampleResult());

        mockMvc.perform(get("/predictions")
                        .param("localTeamName", "A")
                        .param("visitorTeamName", "B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeTeamId").value(1))
                .andExpect(jsonPath("$.awayTeamId").value(2));
    }
}
