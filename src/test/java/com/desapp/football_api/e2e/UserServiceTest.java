package com.desapp.football_api.e2e;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.not_found.UserNotFoundException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userService.deleteAll();
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("plain");
        testUser = userService.register(testUser);
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
                () -> userService.register(duplicate)
        );
    }

    @Test
    void matches_validPassword_returnsTrue() {
        Assertions.assertTrue(userService.matches("plain", testUser.getPassword()));
    }

    @Test
    void matches_invalidPassword_throwsBadRequestException() {
        String passwordHash = testUser.getPassword();
        Assertions.assertThrows(
                BadRequestException.class,
                () -> userService.matches("wrong", passwordHash)
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
        testUser.setPassword("newpass");
        User updated = userService.update(testUser);
        Assertions.assertNotEquals("plain", updated.getPassword());
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
    void testRegister() {
        Assertions.assertNotNull(testUser.getId());
        Assertions.assertNotEquals("plain", testUser.getPassword());
    }

    @Test
    void testMatches() {
        Assertions.assertTrue(userService.matches("plain", testUser.getPassword()));
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
        Assertions.assertThrows(Exception.class, () -> userService.register(user));
    }
}