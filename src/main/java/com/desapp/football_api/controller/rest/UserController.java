package com.desapp.football_api.controller.rest;

import com.desapp.football_api.controller.dto.SimpleUserDTO;
import com.desapp.football_api.controller.dto.UserLoginDTO;
import com.desapp.football_api.controller.dto.UserRegisterDTO;
import com.desapp.football_api.model.User;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.service.CookieService;
import com.desapp.football_api.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<SimpleUserDTO> register(@RequestBody UserRegisterDTO userRegisterDTO, HttpServletResponse response, @CookieValue(value = "jwt", required = false) String token) {
        User user = new User(userRegisterDTO.username(), userRegisterDTO.password());
        User registeredUser = userService.register(user);
        cookieService.createCookieToResponse(response, user.getUsername());
        return ResponseEntity.ok(SimpleUserDTO.fromModel(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<SimpleUserDTO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response, @CookieValue(value = "jwt", required = false) String token) {
        User dbUser = userService.findByUsername(userLoginDTO.username());
        cookieService.createCookieToResponse(response, dbUser.getUsername());
        return ResponseEntity.ok(SimpleUserDTO.fromModel(dbUser));
    }

    @GetMapping
    public ResponseEntity<List<SimpleUserDTO>> getAll() {
        List<SimpleUserDTO> dtos = userService.findAll().stream()
                .map(SimpleUserDTO::fromModel)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/me")
    public ResponseEntity<SimpleUserDTO> getCurrentUser(@CookieValue(value = "jwt", required = false) String token) {
        cookieService.validateToken(token);
        String username = jwtUtil.getUsername(token);
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(SimpleUserDTO.fromModel(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response, @CookieValue(value = "jwt", required = false) String token) {
        cookieService.validateToken(token);
        cookieService.clearCookieFromResponse(response);
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleUserDTO> getById(@PathVariable Long id, @CookieValue(value = "jwt", required = false) String token) {
        cookieService.validateToken(token);
        User user = userService.findById(id);
        return ResponseEntity.ok(SimpleUserDTO.fromModel(user));
    }

}