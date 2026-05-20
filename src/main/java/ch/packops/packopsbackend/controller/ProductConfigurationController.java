package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.ProductConfigurationCreateDto;
import ch.packops.packopsbackend.dto.ProductConfigurationDto;
import ch.packops.packopsbackend.dto.ProductConfigurationUpdateDto;
import ch.packops.packopsbackend.service.ProductConfigurationService;
import jakarta.validation.Valid;
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

    // GET /api/products?category=...
    // Alle eingeloggten User duerfen lesen
    @GetMapping
    public ResponseEntity<List<ProductConfigurationDto>> getProductConfigurations(
            @RequestParam(required = false) String category) {

        return ResponseEntity.ok(
                productConfigurationService.getProductConfigurations(category)
        );
    }

    // GET /api/products/{id}
    // Alle eingeloggten User duerfen lesen
    @GetMapping("/{id}")
    public ResponseEntity<ProductConfigurationDto> getProductConfiguration(@PathVariable Long id) {
        return ResponseEntity.ok(
                productConfigurationService.getProductConfiguration(id)
        );
    }

    // POST /api/products
    // Nur admin gemaess SecurityConfig
    @PostMapping
    public ResponseEntity<ProductConfigurationDto> createProductConfiguration(
            @Valid @RequestBody ProductConfigurationCreateDto dto) {

        ProductConfigurationDto created =
                productConfigurationService.createProductConfiguration(dto);

        return ResponseEntity.ok(created);
    }

    // PUT /api/products/{id}
    // Nur admin gemaess SecurityConfig
    @PutMapping("/{id}")
    public ResponseEntity<ProductConfigurationDto> updateProductConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody ProductConfigurationUpdateDto dto) {

        ProductConfigurationDto updated =
                productConfigurationService.updateProductConfiguration(id, dto);

        return ResponseEntity.ok(updated);
    }

    // DELETE /api/products/{id}
    // Deaktiviert / Aktiviert die Produkte anstatt sie zu löschen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> activateOrDeactivateProduct(@PathVariable Long id) {
        productConfigurationService.activateOrDeactivateProductConfiguration(id);
        return ResponseEntity.noContent().build();
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