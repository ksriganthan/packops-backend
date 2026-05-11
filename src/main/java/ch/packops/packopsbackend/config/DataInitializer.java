package ch.packops.packopsbackend.config;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.repository.UserRepository;
import ch.packops.packopsbackend.security.PasswordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public DataInitializer(UserRepository userRepository,
                           PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@packops.ch");
        admin.setPasswordHash(passwordService.hash("admin123"));
        admin.setRole("admin");
        admin.setLanguage("de");

        userRepository.save(admin);
    }
}