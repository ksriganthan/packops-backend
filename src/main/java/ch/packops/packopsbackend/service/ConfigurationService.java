package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.repository.ConfigurationRepository;
import org.springframework.stereotype.Service;

/**
 * @author Kapischan Sriganthan
 */

@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    // TODO: getConfiguration
    // TODO: updateConfiguration
}

