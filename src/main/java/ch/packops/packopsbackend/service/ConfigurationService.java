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
    private final ValidationService validationService;
    private final LoggingService loggingService;

    public ConfigurationService(ConfigurationRepository configurationRepository,
                                ValidationService validationService,
                                LoggingService loggingService) {
        this.configurationRepository = configurationRepository;
        this.validationService = validationService;
        this.loggingService = loggingService;
    }

    // Domain → DTO
    private ConfigurationDto toDto(Configuration config) {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(config.getTargetWeight());
        dto.setTolerance(config.getTolerance());
        dto.setMaxUnits(config.getMaxUnits());
        dto.setMaxIterationsForReject(config.getMaxIterations());
        dto.setLanguage(config.getLanguage());
        return dto;
    }

    // DTO → Domain
    private Configuration toDomain(ConfigurationDto dto) {
        Configuration config = new Configuration();
        config.setTargetWeight(dto.getTargetWeight());
        config.setTolerance(dto.getTolerance());
        config.setMaxUnits(dto.getMaxUnits());
        config.setMaxIterations(dto.getMaxIterationsForReject());
        config.setLanguage(dto.getLanguage());
        return config;
    }

    public ConfigurationDto getConfiguration() {
        Configuration config = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No configuration found"));
        return toDto(config);
    }

    public ConfigurationDto updateConfiguration(ConfigurationDto dto) {
        validationService.validateConfiguration(dto);

        Configuration existing = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No configuration found"));

        // Nur non-null Felder aktualisieren -> Partial Update
        if (dto.getTargetWeight() != null) {
            existing.setTargetWeight(dto.getTargetWeight());
        }
        if (dto.getTolerance() != null) {
            existing.setTolerance(dto.getTolerance());
        }
        if (dto.getMaxUnits() != null) {
            existing.setMaxUnits(dto.getMaxUnits());
        }
        if (dto.getMaxIterationsForReject() != null) {
            existing.setMaxIterations(dto.getMaxIterationsForReject());
        }
        // Todo: Was ist mit Language?
        if (dto.getLanguage() != null) {
            existing.setLanguage(dto.getLanguage());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        loggingService.logInfo("Konfiguration aktualisiert", null);
        return toDto(configurationRepository.save(existing));
    }
}

