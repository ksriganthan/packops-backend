package ch.packops.packopsbackend.service.UnitTests;
import ch.packops.packopsbackend.domain.Configuration;
import ch.packops.packopsbackend.dto.ConfigurationDto;
import ch.packops.packopsbackend.repository.ConfigurationRepository;
import ch.packops.packopsbackend.service.ConfigurationService;
import ch.packops.packopsbackend.service.LoggingService;
import ch.packops.packopsbackend.service.ValidationService;
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
 * Phase-1-Referenz: Abschnitt 1.6.6, Seiten 34–39
 *   TC-UC08-08 — DB speichert richtigen Zustand nach updateConfiguration
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

    /**
     * TC-UC08-08: Rückgabe-DTO enthält exakt die Werte aus dem Input-DTO.
     * Prüft das Mapping DTO → Domain → DTO.
     */
    @Test
    void updateConfiguration_savesCorrectValues() {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(300);
        dto.setTolerance(10);
        dto.setMaxUnits(50);
        dto.setMaxIterationsForReject(5);

        when(configurationRepository.findAll()).thenReturn(Collections.emptyList());
        when(configurationRepository.save(any(Configuration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfigurationDto result = configurationService.updateConfiguration(dto);

        assertEquals(300, result.getTargetWeight());
        assertEquals(10, result.getTolerance());
        assertEquals(50, result.getMaxUnits());
        assertEquals(5, result.getMaxIterationsForReject());
    }

    /**
     * TC-UC08-08: Das Configuration-Entity, das an save() übergeben wird,
     * enthält exakt die DTO-Werte (ArgumentCaptor-Prüfung der DB-Entity).
     */
    @Test
    void updateConfiguration_passesCorrectEntityToRepository() {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(250);
        dto.setTolerance(5);
        dto.setMaxUnits(100);
        dto.setMaxIterationsForReject(3);

        when(configurationRepository.findAll()).thenReturn(Collections.emptyList());
        when(configurationRepository.save(any(Configuration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        configurationService.updateConfiguration(dto);

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(configurationRepository).save(captor.capture());

        Configuration saved = captor.getValue();
        assertEquals(250, saved.getTargetWeight());
        assertEquals(5, saved.getTolerance());
        assertEquals(100, saved.getMaxUnits());
        assertEquals(3, saved.getMaxIterations()); // Entity-Feld = maxIterations
    }

    /** TC-UC08-08: Bestehende Config wird aktualisiert (kein neues Objekt). */
    @Test
    void updateConfiguration_updatesExistingConfiguration() {
        Configuration existing = new Configuration();
        existing.setTargetWeight(100);
        existing.setTolerance(2);
        existing.setMaxUnits(10);
        existing.setMaxIterations(1);

        when(configurationRepository.findAll()).thenReturn(List.of(existing));
        when(configurationRepository.save(any(Configuration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(400);
        dto.setTolerance(20);
        dto.setMaxUnits(200);
        dto.setMaxIterationsForReject(10);

        ConfigurationDto result = configurationService.updateConfiguration(dto);

        assertEquals(400, result.getTargetWeight());
        assertEquals(20, result.getTolerance());
    }

    /** ValidationService wird korrekt delegiert. */
    @Test
    void updateConfiguration_callsValidationService() {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(250);
        dto.setTolerance(5);
        dto.setMaxUnits(100);
        dto.setMaxIterationsForReject(3);

        when(configurationRepository.findAll()).thenReturn(Collections.emptyList());
        when(configurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        configurationService.updateConfiguration(dto);

        verify(validationService, times(1)).validateConfiguration(dto);
    }

    /** Nach Update wird AuditLog geschrieben. */
    @Test
    void updateConfiguration_callsLoggingService() {
        ConfigurationDto dto = new ConfigurationDto();
        dto.setTargetWeight(250);
        dto.setTolerance(5);
        dto.setMaxUnits(100);
        dto.setMaxIterationsForReject(3);

        when(configurationRepository.findAll()).thenReturn(Collections.emptyList());
        when(configurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        configurationService.updateConfiguration(dto);

        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    /** Leere DB → RuntimeException bei getConfiguration(). */
    @Test
    void getConfiguration_emptyRepository_throwsException() {
        when(configurationRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> configurationService.getConfiguration());
    }
}
