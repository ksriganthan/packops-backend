package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.AuditLog;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.repository.AuditLogRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
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
 */
@ExtendWith(MockitoExtension.class)
public class LoggingServiceTest {

        @Mock
        private AuditLogRepository auditLogRepository;

        @Mock
        private ProcessRepository processRepository;

        @InjectMocks
        private LoggingService loggingService;

        // A10: logInfo()

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

        @Test
        void logDeadlock_processNotFound_throwsException() {
            when(processRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> loggingService.logDeadlock(999L, 1));
            verify(auditLogRepository, never()).save(any());
        }
    }

