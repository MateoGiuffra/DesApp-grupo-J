package com.desapp.football_api.controller.web_services;


import com.desapp.football_api.controller.dto.SimpleUserDTO;
import com.desapp.football_api.controller.dto.UserLoginDTO;
import com.desapp.football_api.controller.dto.UserRegisterDTO;
import com.desapp.football_api.model.User;
import com.desapp.football_api.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and authentication")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User registration", description = "Creates a new user and returns its public data. Also " +
            "sets the session JWT cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered",
                    content = @Content(schema = @Schema(implementation = SimpleUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<SimpleUserDTO> register(@RequestBody UserRegisterDTO userRegisterDTO, HttpServletResponse response) {
        User user = UserRegisterDTO.toModel(userRegisterDTO);
        User registeredUser = authService.register(user, response);
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
        User dbUser = authService.login(user, response);
        return ResponseEntity.ok(SimpleUserDTO.fromModel(dbUser));
    }


    @Operation(summary = "Logout", description = "Invalidates the user session (deletes JWT cookie)", security =
    @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().build();
    }

}
