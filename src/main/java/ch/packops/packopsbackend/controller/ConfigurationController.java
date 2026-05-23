package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.ConfigurationDto;
import ch.packops.packopsbackend.service.ConfigurationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kapischan Sriganthan
 */

@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    // GET /api/configuration
    // Alle eingeloggten User dürfen lesen
    @GetMapping
    public ResponseEntity<ConfigurationDto> getConfiguration() {
        // Gibt HTTP 200 OK zurück und packt das ConfigurationDto in den Body der Antwort
        return ResponseEntity.ok(configurationService.getConfiguration());
    }

    // PUT /api/configuration
    // Nur Admin/Operator gemäss SecurityConfig
    @PutMapping
    public ResponseEntity<ConfigurationDto> updateConfiguration(@RequestBody ConfigurationDto dto) {
        return ResponseEntity.ok(configurationService.updateConfiguration(dto));
    }

    // Z.B. wenn die Validierung der DTO-Properties fehlschlägt, könnte eine IllegalArgumentException geworfen werden
    @ExceptionHandler(IllegalArgumentException.class)
    // Falls während der Verarbeitung eine IllegalArgumentException geworfen wird, dann wird diese Methode aufgerufen
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        // beinhaltet die Exception-Message als Body
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // Z.B. wenn ein serverseitiger/unerwarteter Fehler (keine Configuration) auftritt, könnte eine RuntimeException geworfen werden
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}