package com.desapp.football_api.impl;

import com.desapp.football_api.exceptions.not_found.UserNotFoundException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.services.impl.CookieServiceImpl;
import com.desapp.football_api.services.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootTest
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceImplTest {
    @Autowired
    private UserServiceImpl userServiceImpl;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private CookieServiceImpl cookieServiceImpl;
    private Pageable pageable;
    private User testUser;

    @Test
    void findByUsername_existingUser_returnsUser() {
        User result = userServiceImpl.findByUsername("testuser");
        Assertions.assertNotNull(result);
        Assertions.assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsername_nonExistingUser_throwsUserNotFoundException() {
        Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userServiceImpl.findByUsername("nouser")
        );
    }

    @Test
    void existsByUsername_existingUser_returnsTrue() {
        Assertions.assertTrue(userServiceImpl.existsByUsername("testuser"));
    }

    @Test
    void existsByUsername_nonExistingUser_returnsFalse() {
        Assertions.assertFalse(userServiceImpl.existsByUsername("nouser"));
    }

    @Test
    void getUsersPage_usersExist_returnsList() {
        List<User> result = userServiceImpl.getUsersPage(pageable).getContent();
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void findById_existingUser_returnsUser() {
        User result = userServiceImpl.findById(testUser.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(testUser.getId(), result.getId());
    }

    @Test
    void findById_nonExistingUser_throwsUserNotFoundException() {

        Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userServiceImpl.findById(999L)
        );
    }

    @Test
    void update_changePassword_savesNewPassword() {
        String newPassword = "newpass";
        testUser.setPassword(passwordEncoder.encode(newPassword));
        User updated = userServiceImpl.update(testUser);
        Assertions.assertTrue(passwordEncoder.matches(newPassword, updated.getPassword()));
    }

    @Test
    void delete_existingUser_removesUser() {
        Long userId = testUser.getId();
        userServiceImpl.delete(userId);

        Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userServiceImpl.findById(userId)
        );
    }

    @Test
    void deleteAll_usersExist_removesAllUsers() {
        userServiceImpl.deleteAll();
        List<User> result = userServiceImpl.getUsersPage(Pageable.unpaged()).getContent();
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
                () -> userServiceImpl.update(nonExisting)
        );
    }

    @Test
    void delete_nonExistingUser_doesNotThrow() {
        Assertions.assertDoesNotThrow(() -> userServiceImpl.delete(999L));
    }


}