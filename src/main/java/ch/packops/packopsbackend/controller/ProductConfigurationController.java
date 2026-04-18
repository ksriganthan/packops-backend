package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.ProductConfigurationDto;
import ch.packops.packopsbackend.service.ProductConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Kapischan
 */


@RestController
@RequestMapping("/api/products")
public class ProductConfigurationController {

    private final ProductConfigurationService productConfigurationService;

    public ProductConfigurationController(ProductConfigurationService productConfigurationService) {
        this.productConfigurationService = productConfigurationService;
    }

    // GET /api/products
    @GetMapping
    public ResponseEntity<List<ProductConfigurationDto>> getProductConfigurations(
            @RequestParam(required = false) String category, @RequestParam String token) {
        try {
            List<ProductConfigurationDto> productConfigurations = productConfigurationService.getProductConfigurations(category);
            return ResponseEntity.ok(productConfigurations);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductConfigurationDto> getProductConfiguration(@PathVariable Long id, @RequestParam String token) {
        try {
            ProductConfigurationDto product = productConfigurationService.getProductConfiguration(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/products
    @PostMapping
    public ResponseEntity<?> createProductConfiguration(@RequestBody ProductConfigurationDto dto, @RequestParam String token) {
        try {
            ProductConfigurationDto created = productConfigurationService.createProductConfiguration(dto);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // PUT /api/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProductConfiguration(@PathVariable Long id, @RequestBody ProductConfigurationDto dto,
                                                        @RequestParam String token) {
        try {
            ProductConfigurationDto updated = productConfigurationService.updateProductConfiguration(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductConfiguration(@PathVariable Long id,  @RequestParam String token) {
        try {
            productConfigurationService.deleteProductConfiguration(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}