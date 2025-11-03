package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.not_found.UserNotFoundException;
import com.desapp.football_api.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootTest
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private CookieService cookieService;

    private User testUser;
    private HttpServletResponse mockResponse;

    @BeforeEach
    void setUp() {
        userService.deleteAll();
        mockResponse = Mockito.mock(HttpServletResponse.class);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("plain");

        testUser = userService.register(testUser, mockResponse);
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
                () -> userService.register(duplicate, mockResponse)
        );
    }

    @Test
    void login_validCredentials_returnsUser() {
        User loginAttempt = new User();
        loginAttempt.setUsername("testuser");
        loginAttempt.setPassword("plain");

        User loggedInUser = userService.login(loginAttempt, mockResponse);

        Assertions.assertNotNull(loggedInUser);
        Assertions.assertEquals("testuser", loggedInUser.getUsername());
        Mockito.verify(cookieService).createCookieToResponse(mockResponse, "testuser");
    }

    @Test
    void login_invalidPassword_throwsBadCredentialsException() {
        User loginAttempt = new User();
        loginAttempt.setUsername("testuser");
        loginAttempt.setPassword("wrong");

        Assertions.assertThrows(
                BadCredentialsException.class,
                () -> userService.login(loginAttempt, mockResponse)
        );
    }

    @Test
    void findByUsername_existingUser_returnsUser() {
        User result = userService.findByUsername("testuser");
        Assertions.assertNotNull(result);
        Assertions.assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsername_nonExistingUser_throwsUserNotFoundException() {
        Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.findByUsername("nouser")
        );
    }

    @Test
    void existsByUsername_existingUser_returnsTrue() {
        Assertions.assertTrue(userService.existsByUsername("testuser"));
    }

    @Test
    void existsByUsername_nonExistingUser_returnsFalse() {
        Assertions.assertFalse(userService.existsByUsername("nouser"));
    }

    @Test
    void findAll_usersExist_returnsList() {
        List<User> result = userService.findAll();
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void findById_existingUser_returnsUser() {
        User result = userService.findById(testUser.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(testUser.getId(), result.getId());
    }

    @Test
    void findById_nonExistingUser_throwsUserNotFoundException() {

        Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(999L)
        );
    }

    @Test
    void update_changePassword_savesNewPassword() {
        String newPassword = "newpass";
        testUser.setPassword(passwordEncoder.encode(newPassword));
        User updated = userService.update(testUser);
        Assertions.assertTrue(passwordEncoder.matches(newPassword, updated.getPassword()));
    }

    @Test
    void delete_existingUser_removesUser() {
        Long userId = testUser.getId();
        userService.delete(userId);

        Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(userId)
        );
    }

    @Test
    void deleteAll_usersExist_removesAllUsers() {
        userService.deleteAll();
        List<User> result = userService.findAll();
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void update_nonExistingUser_throwsUserNotFoundException() {
        User nonExisting = new User();
        nonExisting.setId(999L);
        nonExisting.setUsername("nouser");
        nonExisting.setPassword("pass");

        Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.update(nonExisting)
        );
    }

    @Test
    void delete_nonExistingUser_doesNotThrow() {
        Assertions.assertDoesNotThrow(() -> userService.delete(999L));
    }

    @Test
    void register_userWithNullPassword_throwsException() {
        User user = new User();
        user.setUsername("nullpass");
        user.setPassword(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(user, mockResponse));
    }
}