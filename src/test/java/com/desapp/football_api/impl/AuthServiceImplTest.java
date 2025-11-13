package com.desapp.football_api.impl;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.repository.UserRepository;
import com.desapp.football_api.services.CookieService;
import com.desapp.football_api.services.UserService;
import com.desapp.football_api.services.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    CookieService cookieService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserService userService;
    @Mock
    HttpServletResponse response;

    @InjectMocks
    AuthServiceImpl authServiceImpl;

    @Test
    void register_createsUser_encodesPassword_andSetsCookie() {
        User input = new User();
        input.setUsername("john");
        input.setPassword("pwd");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authServiceImpl.register(input, response);

        assertNotNull(saved);
        assertEquals("ENCODED", saved.getPassword());
        verify(cookieService).createCookieToResponse(response, "john");
    }

    @Test
    void register_whenUsernameExists_throwsBadRequest() {
        User input = new User();
        input.setUsername("john");
        when(userRepository.existsByUsername("john")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> authServiceImpl.register(input, response));
        verify(userRepository, never()).save(any());
        verify(cookieService, never()).createCookieToResponse(any(), any());
    }

    @Test
    void login_ok_setsCookie_andReturnsDbUser() {
        User input = new User();
        input.setUsername("ana");
        input.setPassword("raw");

        User db = new User();
        db.setUsername("ana");
        db.setPassword("hashed");

        when(userService.findByUsername("ana")).thenReturn(db);
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);

        User out = authServiceImpl.login(input, response);
        assertSame(db, out);
        verify(cookieService).createCookieToResponse(response, "ana");
    }

    @Test
    void login_badPassword_throwsBadCredentials() {
        User input = new User();
        input.setUsername("ana");
        input.setPassword("bad");
        User db = new User();
        db.setUsername("ana");
        db.setPassword("hashed");

        when(userService.findByUsername("ana")).thenReturn(db);
        when(passwordEncoder.matches("bad", "hashed")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authServiceImpl.login(input, response));
        verify(cookieService, never()).createCookieToResponse(any(), any());
    }

    @Test
    void logout_callsCookieService() {
        authServiceImpl.logout(response);
        verify(cookieService).clearCookieFromResponse(response);
    }
}
