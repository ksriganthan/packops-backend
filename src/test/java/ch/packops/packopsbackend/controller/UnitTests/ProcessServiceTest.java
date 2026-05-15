package ch.packops.packopsbackend.controller.UnitTests;

import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.ProcessDto;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.service.LoggingService;
import ch.packops.packopsbackend.service.ProcessService;
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
 * Phase-1-Referenz: Abschnitt 1.6.6, Seiten 34–39
 *   TC-UC03-02 — Leere DB → leere Liste
 *   TC-UC03-04 — Prozessdaten korrekt zu DTO gemappt
 */
@ExtendWith(MockitoExtension.class)
public class ProcessServiceTest {

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private LoggingService loggingService;

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
        ProcessDto dto = result.get(0);
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

    /** TC-UC03-04: Process ohne User → userId im DTO ist null (kein NPE) */
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

        assertNull(result.get(0).getUserId());
    }
}
