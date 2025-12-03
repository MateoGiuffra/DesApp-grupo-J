package com.desapp.football_api.controller;

import com.desapp.football_api.config.SecurityConfig;
import com.desapp.football_api.controller.dto.SimpleUserDTO;
import com.desapp.football_api.controller.dto.UserLoginDTO;
import com.desapp.football_api.controller.dto.UserRegisterDTO;
import com.desapp.football_api.controller.web_services.AuthController;
import com.desapp.football_api.model.User;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.services.AuthService;
import com.desapp.football_api.services.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthService authService;

    // Beans requeridos por SecurityConfig/JwtAuthenticationFilter
    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    UserServiceImpl userServiceImpl;

    @BeforeEach
    void setup() {
        Mockito.reset(authService, jwtUtil, userServiceImpl);
    }

    @Test
    void register_returnsSimpleUserDTO() throws Exception {
        User created = new User(10L, "pepe", "secret");
        when(authService.register(any(User.class), any(HttpServletResponse.class))).thenReturn(created);

        UserRegisterDTO dto = new UserRegisterDTO("pepe", "secret");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("pepe"));
    }

    @Test
    void login_returnsSimpleUserDTO() throws Exception {
        User dbUser = new User(3L, "ana", "pwd");
        when(authService.login(any(User.class), any(HttpServletResponse.class))).thenReturn(dbUser);

        UserLoginDTO dto = new UserLoginDTO("ana", "pwd");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("ana"));
    }

    @Test
    void logout_returnsOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }
}
