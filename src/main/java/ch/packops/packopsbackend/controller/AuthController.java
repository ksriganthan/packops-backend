package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.UserSession;
import ch.packops.packopsbackend.dto.LoginRequestDto;
import ch.packops.packopsbackend.security.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public UserSession login(@RequestBody LoginRequestDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/logout")
    public boolean logout(@RequestParam String token) {
        return authService.logout(token);
    }
}