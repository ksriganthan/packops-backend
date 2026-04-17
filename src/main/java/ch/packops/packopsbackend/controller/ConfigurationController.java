package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.service.ConfigurationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // TODO: GET /api/configuration
    // TODO: PUT /api/configuration
}
