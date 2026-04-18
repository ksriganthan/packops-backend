package ch.packops.packopsbackend.security;

import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    public boolean matches(String rawPassword, String storedPasswordHash) {
        if (rawPassword == null || storedPasswordHash == null) {
            return false;
        }
        return rawPassword.equals(storedPasswordHash);
    }
}
