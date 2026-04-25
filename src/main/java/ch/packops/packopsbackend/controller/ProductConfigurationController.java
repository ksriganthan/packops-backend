package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.ProductConfigurationCreateDto;
import ch.packops.packopsbackend.dto.ProductConfigurationDto;
import ch.packops.packopsbackend.dto.ProductConfigurationUpdateDto;
import ch.packops.packopsbackend.security.AuthService;
import ch.packops.packopsbackend.security.AuthorizationService;
import ch.packops.packopsbackend.service.ProductConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Kapischan
 */

//TODO: Für Variante 1 oder 2 entscheiden

@RestController
@RequestMapping("/api/products")
public class ProductConfigurationController {

    private final ProductConfigurationService productConfigurationService;
    private final AuthService authService;
    private final AuthorizationService authorizationService;

    public ProductConfigurationController(
            ProductConfigurationService productConfigurationService,
            AuthService authService,
            AuthorizationService authorizationService) {
        this.productConfigurationService = productConfigurationService;
        this.authService = authService;
        this.authorizationService = authorizationService;
    }

    // TODO: Variante 1 gemäss REST-API-Spezifikation
    // GET /api/products
    @GetMapping
    public ResponseEntity<?> getProductConfigurations(
            @RequestParam(required = false) String category, @RequestParam String token) {
        try {
            authService.authenticate(token); // Alle dürfen lesen
            List<ProductConfigurationDto> productConfigurations = productConfigurationService.getProductConfigurations(category);
            return ResponseEntity.ok(productConfigurations);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    /* TODO: Variante 2 gemäss Controller Layer
    // GET /api/products?category=...
    @GetMapping
    public ResponseEntity<?> getProductConfigurationsByCategory(
            @RequestParam(required = true) String category, @RequestParam String token) {
        try {
            authService.authenticate(token); // Alle dürfen lesen
            List<ProductConfigurationDto> productConfigurations = productConfigurationService.getProductConfigurationsByCategory(category);
            return ResponseEntity.ok(productConfigurations);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    } */

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductConfiguration(@PathVariable Long id, @RequestParam String token) {
        try {
            authService.authenticate(token); // Alle dürfen lesen
            ProductConfigurationDto product = productConfigurationService.getProductConfiguration(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // POST /api/products
    @PostMapping
    public ResponseEntity<?> createProductConfiguration(@RequestBody ProductConfigurationCreateDto dto, @RequestParam String token) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canManageProductConfigurations(user)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            ProductConfigurationDto created = productConfigurationService.createProductConfiguration(dto);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // PUT /api/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProductConfiguration(@PathVariable Long id, @RequestBody ProductConfigurationUpdateDto dto,
                                                        @RequestParam String token) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canManageProductConfigurations(user)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            ProductConfigurationDto updated = productConfigurationService.updateProductConfiguration(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // DELETE /api/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductConfiguration(@PathVariable Long id,  @RequestParam String token) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canManageProductConfigurations(user)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            productConfigurationService.deleteProductConfiguration(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }
}