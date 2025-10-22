package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.controller.dto.SimpleUserDTO;
import com.desapp.football_api.controller.dto.UserLoginDTO;
import com.desapp.football_api.controller.dto.UserRegisterDTO;
import com.desapp.football_api.model.User;
import com.desapp.football_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User registration, authentication and query")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "User registration", description = "Creates a new user and returns its public data. Also " +
            "sets the session JWT cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered",
                    content = @Content(schema = @Schema(implementation = SimpleUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content)
    })

    @PostMapping("/register")
    public ResponseEntity<SimpleUserDTO> register(@RequestBody UserRegisterDTO userRegisterDTO,
                                                  HttpServletResponse response) {
        User user = UserRegisterDTO.toModel(userRegisterDTO);
        User registeredUser = userService.register(user, response);
        return ResponseEntity.ok(SimpleUserDTO.fromModel(registeredUser));
    }

    @Operation(summary = "Login", description = "Authenticates the user and sets the session JWT cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = SimpleUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<SimpleUserDTO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response) {
        User user = UserLoginDTO.toModel(userLoginDTO);
        User dbUser = userService.login(user, response);
        return ResponseEntity.ok(SimpleUserDTO.fromModel(dbUser));
    }

    @Operation(summary = "List users")
    @ApiResponse(responseCode = "200", description = "User list", content = @Content(schema = @Schema(implementation
            = SimpleUserDTO.class)))
    @GetMapping
    public ResponseEntity<List<SimpleUserDTO>> getAll() {
        return ResponseEntity.ok(userService.findAll().stream()
                .map(SimpleUserDTO::fromModel)
                .toList());
    }

    @Operation(summary = "Current user", description = "Returns the user associated with the current session (JWT)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema =
            @Schema(implementation = SimpleUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated or invalid token", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<SimpleUserDTO> getCurrentUser(Authentication authentication) {
        User authenticatedUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(SimpleUserDTO.fromModel(authenticatedUser));
    }

    @Operation(summary = "Logout", description = "Invalidates the user session (deletes JWT cookie)", security =
    @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content)
    })

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        userService.logout(response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema =
            @Schema(implementation = SimpleUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<SimpleUserDTO> getById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(SimpleUserDTO.fromModel(user));
    }
}