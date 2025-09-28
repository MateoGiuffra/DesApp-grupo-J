package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.controller.dto.SimpleUserDTO;
import com.desapp.football_api.controller.dto.UserLoginDTO;
import com.desapp.football_api.controller.dto.UserRegisterDTO;
import com.desapp.football_api.model.User;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.service.CookieService;
import com.desapp.football_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User registration, authentication and query")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CookieService cookieService;

    @Operation(summary = "User registration", description = "Creates a new user and returns its public data. Also sets the session JWT cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered",
                    content = @Content(schema = @Schema(implementation = SimpleUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content)
    })

    @PostMapping("/register")
    public ResponseEntity<SimpleUserDTO> register(@RequestBody UserRegisterDTO userRegisterDTO, HttpServletResponse response, @CookieValue(value = "jwt", required = false) String token) {
        User user = UserRegisterDTO.toModel(userRegisterDTO);
        User registeredUser = userService.register(user);
        cookieService.createCookieToResponse(response, user.getUsername());
        return ResponseEntity.ok(SimpleUserDTO.fromModel(registeredUser));
    }

    @Operation(summary = "Login", description = "Authenticates the user and sets the session JWT cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = SimpleUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<SimpleUserDTO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response, @CookieValue(value = "jwt", required = false) String token) {
        User dbUser = userService.findByUsername(userLoginDTO.username());
        cookieService.createCookieToResponse(response, dbUser.getUsername());
        return ResponseEntity.ok(SimpleUserDTO.fromModel(dbUser));
    }

    @Operation(summary = "List users")
    @ApiResponse(responseCode = "200", description = "User list", content = @Content(schema = @Schema(implementation = SimpleUserDTO.class)))
    @GetMapping
    public ResponseEntity<List<SimpleUserDTO>> getAll() {
        List<SimpleUserDTO> dtos = userService.findAll().stream()
                .map(SimpleUserDTO::fromModel)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Current user", description = "Returns the user associated with the JWT sent in the cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SimpleUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated or invalid token", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<SimpleUserDTO> getCurrentUser(@CookieValue(value = "jwt", required = false) String token) {
        cookieService.validateToken(token);
        String username = jwtUtil.getUsername(token);
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(SimpleUserDTO.fromModel(user));
    }

    @Operation(summary = "Logout", description = "Invalidates the user session (deletes JWT cookie)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response, @CookieValue(value = "jwt", required = false) String token) {
        cookieService.validateToken(token);
        cookieService.clearCookieFromResponse(response);
        return ResponseEntity.ok("Logout successful");
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SimpleUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<SimpleUserDTO> getById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id,
            @CookieValue(value = "jwt", required = false) String token) {
        cookieService.validateToken(token);
        User user = userService.findById(id);
        return ResponseEntity.ok(SimpleUserDTO.fromModel(user));
    }
}