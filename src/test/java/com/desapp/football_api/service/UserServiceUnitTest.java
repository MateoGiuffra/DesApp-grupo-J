package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.not_found.UserNotFoundException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CookieService cookieService;

    @InjectMocks
    UserService userService;

    User user;
    HttpServletResponse mockResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("plain");
        mockResponse = mock(HttpServletResponse.class);
    }

    @Test
    void register_uniqueUsername_encodesAndSaves() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(cookieService).createCookieToResponse(any(), anyString());

        User saved = userService.register(user, mockResponse);

        assertEquals("hashed", saved.getPassword());
        verify(userRepository).save(any(User.class));
        verify(cookieService).createCookieToResponse(mockResponse, "john");
    }

    @Test
    void register_existingUsername_throwsBadRequest() {
        when(userRepository.existsByUsername("john")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> userService.register(user, mockResponse));
        verify(userRepository, never()).save(any());
        verify(cookieService, never()).createCookieToResponse(any(), any());
    }

    @Test
    void login_validCredentials_returnsUser() {
        User dbUser = new User(1L, "john", "hashed");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(dbUser));
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);
        doNothing().when(cookieService).createCookieToResponse(any(), anyString());

        User result = userService.login(user, mockResponse);

        assertEquals("john", result.getUsername());
        verify(cookieService).createCookieToResponse(mockResponse, "john");
    }

    @Test
    void login_invalidCredentials_throwsBadCredentialsException() {
        User dbUser = new User(1L, "john", "hashed");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(dbUser));
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.login(user, mockResponse));
        verify(cookieService, never()).createCookieToResponse(any(), any());
    }

    @Test
    void logout_clearsCookie() {
        doNothing().when(cookieService).clearCookieFromResponse(any());
        userService.logout(mockResponse);
        verify(cookieService).clearCookieFromResponse(mockResponse);
    }

    @Test
    void findByUsername_found_returnsUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        assertEquals(user, userService.findByUsername("john"));
    }

    @Test
    void findByUsername_missing_throwsNotFound() {
        when(userRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findByUsername("x"));
    }

    @Test
    void existsByUsername_delegatesToRepo() {
        when(userRepository.existsByUsername("john")).thenReturn(true);
        assertTrue(userService.existsByUsername("john"));
    }

    @Test
    void findAll_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        assertEquals(1, userService.findAll().size());
    }

    @Test
    void findById_found_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertEquals(user, userService.findById(1L));
    }

    @Test
    void findById_missing_throwsNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findById(2L));
    }

    @Test
    void update_existing_saves() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);
        assertEquals(user, userService.update(user));
    }

    @Test
    void update_missing_throwsNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> userService.update(user));
    }

    @Test
    void delete_callsRepo() {
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteAll_callsRepo() {
        userService.deleteAll();
        verify(userRepository).deleteAll();
    }
}
