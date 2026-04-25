package ch.packops.packopsbackend.security;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.domain.UserSession;
import ch.packops.packopsbackend.dto.AuthResponseDto;
import ch.packops.packopsbackend.dto.LoginRequestDto;
import ch.packops.packopsbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                       PasswordService passwordService,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    public AuthResponseDto login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        boolean passwordMatches = passwordService.matches(password, user.getPasswordHash());
        if (!passwordMatches) {
            throw new RuntimeException("Invalid username or password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserSession session = tokenService.createSession(user);

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
        if (!tokenService.isValid(token)) {
            throw new RuntimeException("Invalid token");
        }

        tokenService.invalidateToken(token);
        // Todo
        return false;
    }

    public User authenticate(String token) {
        if (!tokenService.isValid(token)) {
            throw new RuntimeException("Invalid token");
        }

        return tokenService.getSessionByToken(token).getUser();
    }

    public UserSession login(LoginRequestDto dto) {
        // Todo
        return new UserSession();
    }
}