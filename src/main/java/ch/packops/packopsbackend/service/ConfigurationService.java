package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.Configuration;
import ch.packops.packopsbackend.dto.ConfigurationDto;
import ch.packops.packopsbackend.repository.ConfigurationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Kapischan Sriganthan
 */

@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    // Domain → DTO
    private ConfigurationDto toDto(Configuration config) {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(config.getTargetWeight());
        dto.setTolerance(config.getTolerance());
        dto.setMaxUnits(config.getMaxUnits());
        dto.setMaxIterations(config.getMaxIterations());
        return dto;
    }

    // DTO → Domain
    private Configuration toDomain(ConfigurationDto dto) {
        Configuration config = new Configuration();
        config.setTargetWeight(dto.getTargetWeight());
        config.setTolerance(dto.getTolerance());
        config.setMaxUnits(dto.getMaxUnits());
        config.setMaxIterations(dto.getMaxIterations());
        return config;
    }

    public ConfigurationDto getConfiguration() {
        List<Configuration> configs = configurationRepository.findAll();
        if (configs.isEmpty()) {
            throw new RuntimeException("No configuration found");
        }
        return toDto(configs.get(0)); // Es gibt nur eine globale Konfiguration
    }

    public ConfigurationDto updateConfiguration(ConfigurationDto dto) {
        // Validierung gemäss Spezifikation
        if (dto.getTargetWeight() < 50 || dto.getTargetWeight() > 500) {
            throw new IllegalArgumentException("TargetWeight must be between 50 and 500");
        }
        if (dto.getTolerance() < 0) {
            throw new IllegalArgumentException("Tolerance must be positive");
        }

        List<Configuration> configs = configurationRepository.findAll();
        Configuration existing = configs.isEmpty() ? new Configuration() : configs.get(0);
        existing.setTargetWeight(dto.getTargetWeight());
        existing.setTolerance(dto.getTolerance());
        existing.setMaxUnits(dto.getMaxUnits());
        existing.setMaxIterations(dto.getMaxIterations());
        existing.setUpdatedAt(LocalDateTime.now());

        return toDto(configurationRepository.save(existing));
    }
}

