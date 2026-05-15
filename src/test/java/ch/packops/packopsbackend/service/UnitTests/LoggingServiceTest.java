package ch.packops.packopsbackend.service.UnitTests;

import ch.packops.packopsbackend.domain.AuditLog;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.repository.AuditLogRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.service.LoggingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Kapischan Sriganthan
 */

/**
 * Unit Tests für LoggingService
 * Phase-1-Referenz: Abschnitt 1.6.6, Seiten 34–39
 *   A10 (Basis) — System schreibt serverseitigen Log (Level = INFO)
 *   TC-UC10-04  — Logeintrag bei Rückführvorgang (Level = WARN)
 */
@ExtendWith(MockitoExtension.class)
public class LoggingServiceTest {

        @Mock
        private AuditLogRepository auditLogRepository;

        @Mock
        private ProcessRepository processRepository;

        @InjectMocks
        private LoggingService loggingService;

        // ── A10: logInfo() ─────────────────────────────────────────────

        /**
         * A10 (Basis): logInfo() speichert einen AuditLog mit Level "INFO"
         * und der übergebenen Nachricht.
         */
        @Test
        void logInfo_savesAuditLog() {
            loggingService.logInfo("Konfiguration aktualisiert", null);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository, times(1)).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertEquals("INFO", saved.getLevel());
            assertEquals("Konfiguration aktualisiert", saved.getMessage());
            assertNotNull(saved.getCreatedAt());
            assertNull(saved.getProcess());
        }

        /** A10: logInfo() mit processId — Prozess wird im Log verlinkt. */
        @Test
        void logInfo_withProcessId_linksProcess() {
            Process process = new Process();
            process.setStatus("RUNNING");
            when(processRepository.findById(7L)).thenReturn(Optional.of(process));

            loggingService.logInfo("Prozess läuft", 7L);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(captor.capture());

            assertEquals("INFO", captor.getValue().getLevel());
            assertNotNull(captor.getValue().getProcess());
        }

        // ── TC-UC10-04: logDeadlock() → WARN ──────────────────────────

        /**
         * TC-UC10-04: logDeadlock() erstellt AuditLog mit Level "WARN"
         * bei Deadlock (Portion wird aus Bucket zurückgeführt).
         * Prüft: Level = WARN, Bucket-Nr. in Message, Prozess verlinkt.
         */
        @Test
        void logDeadlock_createsWarnLog() {
            Process process = new Process();
            process.setStatus("RUNNING");
            when(processRepository.findById(5L)).thenReturn(Optional.of(process));

            loggingService.logDeadlock(5L, 3); // Prozess 5, Bucket 3

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository, times(1)).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertEquals("WARN", saved.getLevel());                 // ← MUSS WARN sein
            assertTrue(saved.getMessage().contains("3"),            // Bucket-Nummer in Message
                    "Deadlock-Message soll die Bucket-Nummer enthalten");
            assertNotNull(saved.getProcess());                      // Prozess verlinkt
            assertNotNull(saved.getCreatedAt());
        }

        /**
         * TC-UC10-04: logDeadlock() mit nicht-existierender ProcessId → Exception.
         * Kein AuditLog wird gespeichert.
         */
        @Test
        void logDeadlock_processNotFound_throwsException() {
            when(processRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> loggingService.logDeadlock(999L, 1));
            verify(auditLogRepository, never()).save(any());
        }
    }

