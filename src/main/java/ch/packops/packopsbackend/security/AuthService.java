package ch.packops.packopsbackend.security;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.domain.UserSession;
import ch.packops.packopsbackend.dto.AuthResponseDto;
import ch.packops.packopsbackend.dto.LoginRequestDto;
import ch.packops.packopsbackend.repository.UserRepository;
import ch.packops.packopsbackend.service.LoggingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final LoggingService loggingService;

    public AuthService(UserRepository userRepository,
                       PasswordService passwordService,
                       TokenService tokenService,
                       LoggingService loggingService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.loggingService = loggingService;
    }

    public AuthResponseDto login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            loggingService.logInfo("Login failed: missing credentials", null);
            throw new RuntimeException("Invalid username or password");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    loggingService.logInfo("Login failed for unknown user: " + username, null);
                    return new RuntimeException("Invalid username or password");
                });

        boolean passwordMatches = passwordService.matches(password, user.getPasswordHash());
        if (!passwordMatches) {
            loggingService.logInfo("Login failed for user: " + username, null);
            throw new RuntimeException("Invalid username or password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserSession session = tokenService.createSession(user);

        loggingService.logInfo("Login successful for user: " + username, null);

        AuthResponseDto response = new AuthResponseDto();
        response.setStatus("success");
        response.setUsertoken(session.getToken());
        response.setUserid(user.getId());
        response.setRole(user.getRole());
        response.setEmail(user.getEmail());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    public boolean logout(String token) {
        if (token == null || token.isBlank()) {
            loggingService.logInfo("Logout failed: missing token", null);
            throw new RuntimeException("Invalid token");
        }

        if (!tokenService.isValid(token)) {
            loggingService.logInfo("Logout failed: invalid token", null);
            throw new RuntimeException("Invalid token");
        }

        User user = tokenService.getSessionByToken(token).getUser();
        tokenService.invalidateToken(token);

        if (user != null) {
            loggingService.logInfo("Logout successful for user: " + user.getUsername(), null);
        } else {
            loggingService.logInfo("Logout successful", null);
        }

        return true;
    }

    public User authenticate(String token) {
        if (!tokenService.isValid(token)) {
            loggingService.logInfo("Authentication failed: invalid token", null);
            throw new RuntimeException("Invalid token");
        }

        return tokenService.getSessionByToken(token).getUser();
    }

    public AuthResponseDto login(LoginRequestDto dto) {
        return login(dto.getUsername(), dto.getPassword());
    }
}