package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.service.ProductConfigurationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // TODO: GET /api/products
    // TODO: GET /api/products/{id}
    // TODO: POST /api/products
    // TODO: PUT /api/products/{id}
    // TODO: DELETE /api/products/{id}
}
