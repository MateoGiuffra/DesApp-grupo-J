package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.controller.dto.SimpleUserDTO;
import com.desapp.football_api.model.User;
import com.desapp.football_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User query")
@AllArgsConstructor
public class UserController {

    private final UserService userServiceImpl;

    @Operation(summary = "List users")
    @ApiResponse(responseCode = "200", description = "User list", content = @Content(schema = @Schema(implementation
            = SimpleUserDTO.class)))
    @GetMapping
    public ResponseEntity<Page<SimpleUserDTO>> getUsersPage(@ParameterObject Pageable pageable) {
        Page<User> usersPage = userServiceImpl.getUsersPage(pageable);
        List<User> users = usersPage.getContent();
        List<SimpleUserDTO> userDTOS = users.stream()
                .map(SimpleUserDTO::fromModel)
                .toList();
        return ResponseEntity.ok(new PageImpl<>(userDTOS, pageable, usersPage.getTotalElements()));
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
            @PathVariable Long id
    ) {
        User user = userServiceImpl.findById(id);
        return ResponseEntity.ok(SimpleUserDTO.fromModel(user));
    }
}