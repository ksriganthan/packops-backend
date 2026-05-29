package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.ProductConfigurationCreateDto;
import ch.packops.packopsbackend.dto.ProductConfigurationDto;
import ch.packops.packopsbackend.dto.ProductConfigurationUpdateDto;
import ch.packops.packopsbackend.service.ProductConfigurationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Kapischan Sriganthan
 */

@RestController
@RequestMapping("/api/products")
public class ProductConfigurationController {

    private final ProductConfigurationService productConfigurationService;

    public ProductConfigurationController(ProductConfigurationService productConfigurationService) {
        this.productConfigurationService = productConfigurationService;
    }

    // GET /api/products
    // Alle eingeloggten User dürfen lesen
    @GetMapping
    public ResponseEntity<List<ProductConfigurationDto>> getProductConfigurations() {

        return ResponseEntity.ok(
                productConfigurationService.getProductConfigurations()
        );
    }

    // POST /api/products
    // Nur Admin gemäss SecurityConfig
    @PostMapping
    public ResponseEntity<ProductConfigurationDto> createProductConfiguration(
            @RequestBody ProductConfigurationCreateDto dto) {

        ProductConfigurationDto created =
                productConfigurationService.createProductConfiguration(dto);

        return ResponseEntity.ok(created);
    }

    // PUT /api/products/{id}
    // Nur Admin gemäss SecurityConfig
    @PutMapping("/{id}")
    public ResponseEntity<ProductConfigurationDto> updateProductConfiguration(
            @PathVariable Long id,
            @RequestBody ProductConfigurationUpdateDto dto) {

        ProductConfigurationDto updated =
                productConfigurationService.updateProductConfiguration(id, dto);

        return ResponseEntity.ok(updated);
    }

    // DELETE /api/products/{id}
    // Deaktiviert / Aktiviert die Produkte anstatt sie zu löschen
    // Nur Admin gemäss SecurityConfig
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> activateOrDeactivateProduct(@PathVariable Long id) {
        productConfigurationService.activateOrDeactivateProductConfiguration(id);
        return ResponseEntity.noContent().build(); // Body ist leer + 204 no content
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