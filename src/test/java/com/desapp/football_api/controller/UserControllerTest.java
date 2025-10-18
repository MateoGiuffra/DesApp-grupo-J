package com.desapp.football_api.controller;

import com.desapp.football_api.controller.dto.UserRegisterDTO;
import com.desapp.football_api.controller.web_services.UserController;
import com.desapp.football_api.model.User;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.service.CookieService;
import com.desapp.football_api.service.UserService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    CookieService cookieService;

    @Test
    void register_createsUserAndSetsCookie() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("john", "pass");
        User saved = new User();
        saved.setId(1L);
        saved.setUsername("john");
        when(userService.register(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john"));

        verify(cookieService).createCookieToResponse(any(), eq("john"));
    }

    @Test
    void login_authenticatesAndSetsCookie() throws Exception {
        User dbUser = new User();
        dbUser.setId(2L);
        dbUser.setUsername("alice");
        when(userService.login("alice")).thenReturn(dbUser);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("alice"));

        verify(cookieService).createCookieToResponse(any(), eq("alice"));
    }

    @Test
    void getAll_returnsListOfUsers() throws Exception {
        User u = new User();
        u.setId(3L);
        u.setUsername("mike");
        when(userService.findAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].username").value("mike"));
    }

    @Test
    void me_validToken_returnsCurrentUser() throws Exception {
        when(jwtUtil.getUsername("token")).thenReturn("bob");
        when(userService.findByUsername("bob")).thenReturn(new User(1L, "bob", "x"));

        mockMvc.perform(get("/api/users/me").cookie(new jakarta.servlet.http.Cookie("jwt", "token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));

        verify(cookieService).validateToken("token");
    }

    @Test
    void getById_validToken_returnsUser() throws Exception {
        when(userService.findById(5L)).thenReturn(new User(5L, "b", "x"));
        when(jwtUtil.getUsername(anyString())).thenReturn("ignored");

        mockMvc.perform(get("/api/users/5").cookie(new jakarta.servlet.http.Cookie("jwt", "tok")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(cookieService).validateToken("tok");
    }

    @Test
    void logout_clearsCookie() throws Exception {
        mockMvc.perform(post("/api/users/logout").cookie(new jakarta.servlet.http.Cookie("jwt", "tok")))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));
        verify(cookieService).validateToken("tok");
        verify(cookieService).clearCookieFromResponse(any());
    }
}
