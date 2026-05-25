package ch.packops.packopsbackend.security;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.domain.UserSession;
import ch.packops.packopsbackend.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * @author Mladen Radovanovic
 */
@Service
public class TokenService {

    private final UserSessionRepository userSessionRepository;
    private final JwtEncoder jwtEncoder;

    @Value("${app.security.jwt.issuer}")
    private String issuer;

    @Value("${app.security.jwt.expiration-minutes}")
    private long expirationMinutes;

    public TokenService(UserSessionRepository userSessionRepository,
                        JwtEncoder jwtEncoder) {
        this.userSessionRepository = userSessionRepository;
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(expirationMinutes));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public UserSession createSession(User user) {
        String token = generateToken(user);

        UserSession session = new UserSession();
        session.setToken(token);
        session.setUser(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
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

            if (!Boolean.TRUE.equals(session.getActive())) {
                return false;
            }

            if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
                return false;
            }

            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}