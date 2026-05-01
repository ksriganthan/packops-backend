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
        return dto;
    }

    // DTO → Domain
    private Configuration toDomain(ConfigurationDto dto) {
        Configuration config = new Configuration();
        config.setTargetWeight(dto.getTargetWeight());
        config.setTolerance(dto.getTolerance());
        config.setMaxUnits(dto.getMaxUnits());
        config.setMaxIterations(dto.getMaxIterationsForReject());
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
        // Validierung über ValidationService
        validationService.validateConfiguration(dto);

        List<Configuration> configs = configurationRepository.findAll();
        Configuration existing = configs.isEmpty() ? new Configuration() : configs.get(0);
        existing.setTargetWeight(dto.getTargetWeight());
        existing.setTolerance(dto.getTolerance());
        existing.setMaxUnits(dto.getMaxUnits());
        existing.setMaxIterations(dto.getMaxIterationsForReject());
        existing.setUpdatedAt(LocalDateTime.now());

        loggingService.logInfo("Konfiguration aktualisiert", null);
        return toDto(configurationRepository.save(existing));
    }
}

