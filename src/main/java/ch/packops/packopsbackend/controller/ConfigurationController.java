package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.ConfigurationDto;
import ch.packops.packopsbackend.service.ConfigurationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    // GET /api/configuration
    // Alle eingeloggten User duerfen lesen
    @GetMapping
    public ResponseEntity<ConfigurationDto> getConfiguration() {
        return ResponseEntity.ok(configurationService.getConfiguration());
    }

    // PUT /api/configuration
    // Nur admin/operator gemaess SecurityConfig
    @PutMapping
    public ResponseEntity<ConfigurationDto> updateConfiguration(
            @Valid @RequestBody ConfigurationDto dto) {
        return ResponseEntity.ok(configurationService.updateConfiguration(dto));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}