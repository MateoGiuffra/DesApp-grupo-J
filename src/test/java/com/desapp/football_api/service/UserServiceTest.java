package com.desapp.football_api.service;

import com.desapp.football_api.model.User;
import com.desapp.football_api.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {
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
    void testRegister() {
        Assertions.assertNotNull(testUser.getId());
        Assertions.assertNotEquals("plain", testUser.getPassword());
    }

    @Test
    void testMatches() {
        Assertions.assertTrue(userService.matches("plain", testUser.getPassword()));
    }

    @Test
    void testFindByUsername() {
        Optional<User> result = userService.findByUsername("testuser");
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testFindAll() {
        List<User> result = userService.findAll();
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void testFindById() {
        Optional<User> result = userService.findById(testUser.getId());
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(testUser.getId(), result.get().getId());
    }

    @Test
    void testUpdate() {
        testUser.setPassword("newpass");
        User updated = userService.update(testUser);
        Assertions.assertNotEquals("plain", updated.getPassword());
    }

    @Test
    void testDelete() {
        userService.delete(testUser.getId());
        Optional<User> result = userService.findById(testUser.getId());
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void testDeleteAll() {
        userService.deleteAll();
        List<User> result = userService.findAll();
        Assertions.assertTrue(result.isEmpty());
    }
}