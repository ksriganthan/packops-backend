package ch.packops.packopsbackend.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedPasswordHash) {
        if (rawPassword == null || storedPasswordHash == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, storedPasswordHash);
    }
}