package ch.packops.packopsbackend.security;

import ch.packops.packopsbackend.domain.UserSession;
import ch.packops.packopsbackend.repository.UserSessionRepository;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @author Mladen Radovanovic
 */
@Configuration
public class JwtConfig {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(UserSessionRepository userSessionRepository) {
        SecretKey secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        return token -> {
            var jwt = decoder.decode(token);

            UserSession session = userSessionRepository.findByToken(token)
                    .orElseThrow(() -> new BadJwtException("Session not found"));

            if (!Boolean.TRUE.equals(session.getActive())) {
                throw new BadJwtException("Session is inactive");
            }

            if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new BadJwtException("Session is expired");
            }

            return jwt;
        };
    }
}