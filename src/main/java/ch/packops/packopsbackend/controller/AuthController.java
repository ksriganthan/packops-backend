package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.AuthResponseDto;
import ch.packops.packopsbackend.dto.LoginRequestDto;
import ch.packops.packopsbackend.security.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        try {
            AuthResponseDto response = authService.login(dto.getUsername(), dto.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String token) {
        try {
            authService.logout(token);
            return ResponseEntity.ok().body("Logout successful");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
}