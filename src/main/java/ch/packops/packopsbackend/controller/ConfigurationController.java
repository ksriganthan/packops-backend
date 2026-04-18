package ch.packops.packopsbackend.controller;
import ch.packops.packopsbackend.dto.ConfigurationDto;
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

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
    // GET /api/configuration
    @GetMapping
    public ResponseEntity<ConfigurationDto> getConfiguration(@RequestParam String token) {
        try {
            ConfigurationDto config = configurationService.getConfiguration();
            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/configuration
    @PutMapping
    public ResponseEntity<?> updateConfiguration(@RequestBody ConfigurationDto dto, @RequestParam String token) {
        try {
            ConfigurationDto updated = configurationService.updateConfiguration(dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
