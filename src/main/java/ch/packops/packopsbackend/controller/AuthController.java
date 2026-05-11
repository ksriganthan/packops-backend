package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.AuthResponseDto;
import ch.packops.packopsbackend.dto.LoginRequestDto;
import ch.packops.packopsbackend.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        AuthResponseDto response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        if ("Invalid username or password".equals(ex.getMessage())
                || "Invalid token".equals(ex.getMessage())
                || "Invalid authorization header".equals(ex.getMessage())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}