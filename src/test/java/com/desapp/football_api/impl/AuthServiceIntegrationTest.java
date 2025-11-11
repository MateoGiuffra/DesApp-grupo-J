package com.desapp.football_api.impl;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.services.UserService;
import com.desapp.football_api.services.AuthService;
import com.desapp.football_api.services.impl.CookieServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthServiceIntegrationTest {
    @Autowired
    private UserService userServiceImpl;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private CookieServiceImpl cookieServiceImpl;
    private Pageable pageable;
    private User testUser;
    private HttpServletResponse mockResponse;


    @BeforeEach
    void setUp() {
        userServiceImpl.deleteAll();
        mockResponse = Mockito.mock(HttpServletResponse.class);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("plain");

        testUser = authService.register(testUser, mockResponse);
    }

    @Test
    void register_userWithUniqueUsername_savesUserWithEncodedPassword() {
        String password = testUser.getPassword();
        Assertions.assertNotNull(testUser.getId());
        Assertions.assertNotEquals("plain", password);
    }

    @Test
    void register_userWithExistingUsername_throwsBadRequestException() {
        User duplicate = new User();
        duplicate.setUsername("testuser");
        duplicate.setPassword("another");

        Assertions.assertThrows(
                BadRequestException.class,
                () -> authService.register(duplicate, mockResponse)
        );
    }

    @Test
    void login_validCredentials_returnsUser() {
        User loginAttempt = new User();
        loginAttempt.setUsername("testuser");
        loginAttempt.setPassword("plain");

        User loggedInUser = authService.login(loginAttempt, mockResponse);

        Assertions.assertNotNull(loggedInUser);
        Assertions.assertEquals("testuser", loggedInUser.getUsername());
        Mockito.verify(cookieServiceImpl).createCookieToResponse(mockResponse, "testuser");
    }

    @Test
    void login_invalidPassword_throwsBadCredentialsException() {
        User loginAttempt = new User();
        loginAttempt.setUsername("testuser");
        loginAttempt.setPassword("wrong");

        Assertions.assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginAttempt, mockResponse)
        );
    }

    @Test
    void register_userWithNullPassword_throwsException() {
        User user = new User();
        user.setUsername("nullpass");
        user.setPassword(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> authService.register(user, mockResponse));
    }
}
