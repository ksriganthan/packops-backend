package ch.packops.packopsbackend.service;
import ch.packops.packopsbackend.domain.Configuration;
import ch.packops.packopsbackend.dto.ConfigurationDto;
import ch.packops.packopsbackend.repository.ConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Kapischan Sriganthan
 */

/**
 * Unit Tests für ConfigurationService
 */
@ExtendWith(MockitoExtension.class)
public class ConfigurationServiceTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private ConfigurationService configurationService;

    // ── TC-UC08-08: updateConfiguration speichert korrekte Werte ──

    @Test
    void updateConfiguration_savesCorrectValues() {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(300);
        dto.setTolerance(10);
        dto.setMaxUnits(50);
        dto.setMaxIterationsForReject(5);

        // Mock: Existierende Configuration (wird vom refactored Code erwartet)
        Configuration existing = new Configuration();
        existing.setTargetWeight(200);
        existing.setTolerance(5);
        existing.setMaxUnits(100);
        existing.setMaxIterations(3);
        when(configurationRepository.findAll()).thenReturn(List.of(existing));

        when(configurationRepository.save(any(Configuration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfigurationDto result = configurationService.updateConfiguration(dto);

        assertEquals(300, result.getTargetWeight());
        assertEquals(10, result.getTolerance());
        assertEquals(50, result.getMaxUnits());
        assertEquals(5, result.getMaxIterationsForReject());
    }

    @Test
    void updateConfiguration_passesCorrectEntityToRepository() {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(250);
        dto.setTolerance(5);
        dto.setMaxUnits(100);
        dto.setMaxIterationsForReject(3);

        // Mock: Existierende Configuration
        Configuration existing = new Configuration();
        existing.setTargetWeight(200);
        existing.setTolerance(10);
        existing.setMaxUnits(50);
        existing.setMaxIterations(2);
        when(configurationRepository.findAll()).thenReturn(List.of(existing));

        when(configurationRepository.save(any(Configuration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        configurationService.updateConfiguration(dto);

        // Um das Objekt, welches an Respository für die Speicherung übergeben wird, aufzufangen und zu prüfen
        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(configurationRepository).save(captor.capture());

        Configuration saved = captor.getValue();
        assertEquals(250, saved.getTargetWeight());
        assertEquals(5, saved.getTolerance());
        assertEquals(100, saved.getMaxUnits());
        assertEquals(3, saved.getMaxIterations()); // Entity-Feld = maxIterations
    }

    @Test
    void updateConfiguration_updatesExistingConfiguration() {
        Configuration existing = new Configuration();
        existing.setTargetWeight(100);
        existing.setTolerance(2);
        existing.setMaxUnits(10);
        existing.setMaxIterations(1);

        when(configurationRepository.findAll()).thenReturn(List.of(existing));
        when(configurationRepository.save(any(Configuration.class))) // Egal welches Configuration-Object
                .thenAnswer(invocation -> invocation.getArgument(0)); // Erste Argument des save-Aufrufs

        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(400);
        dto.setTolerance(20);
        dto.setMaxUnits(200);
        dto.setMaxIterationsForReject(10);

        ConfigurationDto result = configurationService.updateConfiguration(dto);

        assertEquals(400, result.getTargetWeight());
        assertEquals(20, result.getTolerance());
    }

    @Test
    void updateConfiguration_callsValidationService() {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(250);
        dto.setTolerance(5);
        dto.setMaxUnits(100);
        dto.setMaxIterationsForReject(3);

        // Mock: Existierende Configuration
        Configuration existing = new Configuration();
        existing.setTargetWeight(200);
        when(configurationRepository.findAll()).thenReturn(List.of(existing));
        when(configurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        configurationService.updateConfiguration(dto);

        // Prüfen, ob die validateConfiguration-Methode mit genau diesem DTO genau einmal aufgerufen wurde
        verify(validationService, times(1)).validateConfiguration(dto);
    }

    @Test
    void updateConfiguration_callsLoggingService() {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(250);
        dto.setTolerance(5);
        dto.setMaxUnits(100);
        dto.setMaxIterationsForReject(3);

        // Mock: Existierende Configuration
        Configuration existing = new Configuration();
        existing.setTargetWeight(200);
        when(configurationRepository.findAll()).thenReturn(List.of(existing));
        when(configurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        configurationService.updateConfiguration(dto);

        // ProcessID sollte null sein
        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    @Test
    void getConfiguration_emptyRepository_throwsException() {
        when(configurationRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> configurationService.getConfiguration());
    }
}
