package ch.packops.packopsbackend.security;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.domain.UserSession;
import ch.packops.packopsbackend.repository.UserSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenService {

    private final UserSessionRepository userSessionRepository;

    public TokenService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public UserSession createSession(User user) {
        String token = generateToken();

        UserSession session = new UserSession();
        session.setToken(token);
        session.setUser(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setActive(true);

        return userSessionRepository.save(session);
    }

    public void invalidateToken(String token) {
        UserSession session = getSessionByToken(token);
        session.setActive(false);
        userSessionRepository.save(session);
    }

    public UserSession getSessionByToken(String token) {
        return userSessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Session not found for token"));
    }

    public boolean isValid(String token) {
        try {
            UserSession session = getSessionByToken(token);
            return session.isActive();
        } catch (RuntimeException e) {
            return false;
        }
    }
}