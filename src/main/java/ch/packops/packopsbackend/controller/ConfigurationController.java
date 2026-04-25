package ch.packops.packopsbackend.controller;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.ConfigurationDto;
import ch.packops.packopsbackend.security.AuthService;
import ch.packops.packopsbackend.security.AuthorizationService;
import ch.packops.packopsbackend.service.ConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kapischan
 */

@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private final AuthService authService;
    private final AuthorizationService authorizationService;

    public ConfigurationController(ConfigurationService configurationService,
                                   AuthService authService,
                                   AuthorizationService authorizationService) {
        this.configurationService = configurationService;
        this.authService = authService;
        this.authorizationService = authorizationService;
    }

    // GET /api/configuration
    @GetMapping
    public ResponseEntity<?> getConfiguration(@RequestParam String token) {
        try {
            authService.authenticate(token); // Alle dürfen lesen
            ConfigurationDto config = configurationService.getConfiguration();
            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // PUT /api/configuration
    @PutMapping
    public ResponseEntity<?> updateConfiguration(@RequestBody ConfigurationDto dto, @RequestParam String token) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canUpdateConfiguration(user)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            ConfigurationDto updated = configurationService.updateConfiguration(dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }
}
