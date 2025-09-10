package com.desapp.football_api.controller.REST;

import com.desapp.football_api.controller.DTO.SimpleUserDTO;
import com.desapp.football_api.controller.DTO.UserLoginDTO;
import com.desapp.football_api.controller.DTO.UserRegisterDTO;
import com.desapp.football_api.exceptions.bad_request.UserAlreadyExistsException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<SimpleUserDTO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        try {
            User user = new User(userRegisterDTO.username(), userRegisterDTO.password());
            User registeredUser = userService.register(user);
            SimpleUserDTO dto = SimpleUserDTO.fromModel(registeredUser);
            return ResponseEntity.ok(dto);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response) {
        Optional<User> dbUser = userService.findByUsername(userLoginDTO.username());
        if (dbUser.isPresent() && userService.matches(userLoginDTO.password(), dbUser.get().getPassword())) {
            String token = jwtUtil.generateToken(userLoginDTO.username());
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResponseEntity.ok(SimpleUserDTO.fromModel(dbUser.get()));
        }
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @GetMapping("/me")
    public ResponseEntity<SimpleUserDTO> getCurrentUser(@CookieValue(value = "jwt", required = false) String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(null);
        }
        String username = jwtUtil.getUsername(token);
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(SimpleUserDTO.fromModel(user.get()));
        }
        return ResponseEntity.status(401).body(null);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, @CookieValue(value = "jwt", required = false) String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("No estás logueado");
        }
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok("Logout exitoso");
    }


    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return ResponseEntity.ok(userService.update(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}