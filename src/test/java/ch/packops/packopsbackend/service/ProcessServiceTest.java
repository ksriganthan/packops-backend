package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.ProcessDto;
import ch.packops.packopsbackend.repository.ConfigurationRepository;
import ch.packops.packopsbackend.repository.PackageRepository;
import ch.packops.packopsbackend.repository.PortionRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import ch.packops.packopsbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Kapischan Sriganthan
 */


/**
 * Unit Tests für ProcessService
 *   TC-UC03-02 — Leere DB → leere Liste
 *   TC-UC03-04 — Prozessdaten korrekt zu DTO gemappt
 */

@ExtendWith(MockitoExtension.class)
public class ProcessServiceTest {

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private ProductConfigurationRepository productConfigurationRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private PortionRepository portionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoggingService loggingService;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private ProcessService processService;

    // ── TC-UC03-02: Leere DB ───────────────────────────────────────

    /** TC-UC03-02: Keine Prozesse in DB → getAllProcesses() gibt leere Liste zurück */
    @Test
    void getAllProcesses_emptyRepository_returnsEmptyList() {
        when(processRepository.findAll()).thenReturn(Collections.emptyList());

        List<ProcessDto> result = processService.getAllProcesses();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── TC-UC03-04: Felder korrekt gemappt ────────────────────────

    /**
     * TC-UC03-04: Alle Felder eines Process-Objekts werden 1:1 in ProcessDto gemappt.
     * Prüft: processId, status, startTimestamp, targetWeight, tolerance,
     *        maxUnits, maxIterationsForReject, unitsPacked, userId
     */
    @Test
    void getAllProcesses_mapsFieldsCorrectly() {
        User user = new User();
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 42L);
        } catch (Exception e) {
            fail("User-ID konnte nicht gesetzt werden: " + e.getMessage());
        }

        Process process = new Process();
        try {
            java.lang.reflect.Field idField = Process.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(process, 1L);
        } catch (Exception e) {
            fail("Process-ID konnte nicht gesetzt werden: " + e.getMessage());
        }

        LocalDateTime start = LocalDateTime.of(2024, 6, 1, 10, 0);
        process.setStatus("RUNNING");
        process.setStartTimestamp(start);
        process.setTargetWeight(250);
        process.setTolerance(5);
        process.setMaxUnits(100);
        process.setMaxIterationsForReject(3);
        process.setUnitsPacked(12);
        process.setUser(user);

        when(processRepository.findAll()).thenReturn(List.of(process));

        List<ProcessDto> result = processService.getAllProcesses();

        assertEquals(1, result.size());
        ProcessDto dto = result.getFirst();
        assertEquals(1L, dto.getProcessId());
        assertEquals("RUNNING", dto.getStatus());
        assertEquals(start, dto.getStartTimestamp());
        assertEquals(250, dto.getTargetWeight());
        assertEquals(5, dto.getTolerance());
        assertEquals(100, dto.getMaxUnits());
        assertEquals(3, dto.getMaxIterationsForReject());
        assertEquals(12, dto.getUnitsPacked());
        assertEquals(42L, dto.getUserId());
    }

    /** TC-UC03-04: Process ohne User → userId im DTO ist null */
    @Test
    void getAllProcesses_processWithoutUser_userIdIsNull() {
        Process process = new Process();
        process.setStatus("STOPPED");
        process.setTargetWeight(100);
        process.setTolerance(2);
        process.setMaxUnits(10);
        process.setMaxIterationsForReject(1);
        process.setUnitsPacked(0);

        when(processRepository.findAll()).thenReturn(List.of(process));

        List<ProcessDto> result = processService.getAllProcesses();

        assertNull(result.getFirst().getUserId());
    }

    /**
     *
     * TC-UC01: Process-Start
     *   - TC-UC01-01: Erfolgreicher Start
     *   - TC-UC01-02: Ungültige Parameter
     *   - TC-UC01-03: Default-Werte aus Configuration
     *
     * TC-UC02: Process-Stopp
     *   - TC-UC02-01: Erfolgreicher Stopp
     *   - TC-UC02-02: Ungültige Process-ID
     *   - TC-UC02-03: Bereits gestoppter Process
     *
     * TC-UC10: Status-Abfrage
     *   - TC-UC10-01: Laufender Process
     *   - TC-UC10-02: Gestoppter Process
     *   - TC-UC10-03: Nicht-existierender Process
     *
     * TC-UC11: Simulation
     *   - TC-UC11-01: Erfolgreiche Initialisierung
     *   - TC-UC11-02: Mehrfach-Start-Prävention
     *   - TC-UC11-03: Portionen-Konfiguration
     */

    // ══════════════════════════════════════════════════════════════════════════════════════
    // TODO: TESTS FÜR DAVID T.'s METHODEN
    // ══════════════════════════════════════════════════════════════════════════════════════

}
