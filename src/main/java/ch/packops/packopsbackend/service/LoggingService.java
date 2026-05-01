package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.AuditLog;
import ch.packops.packopsbackend.repository.AuditLogRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import org.springframework.stereotype.Service;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.dto.*;

import java.time.LocalDateTime;

/**
 * @author Kapischan
 */

@Service
public class LoggingService {

    private final AuditLogRepository auditLogRepository;
    private final ProcessRepository processRepository;

    public LoggingService(AuditLogRepository auditLogRepository,
                          ProcessRepository processRepository) {
        this.auditLogRepository = auditLogRepository;
        this.processRepository = processRepository;
    }

    // Normaler Info-Log
    public void logInfo(String message, Long processId) {
        AuditLog log = new AuditLog();
        log.setLevel("INFO");
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        if (processId != null) {
            processRepository.findById(processId)
                    .ifPresent(log::setProcess);
        }
        auditLogRepository.save(log);
    }

    // Prozess-Event loggen
    public void logProcessEvent(Long processId, String message) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("Process not found with id: " + processId));
        AuditLog log = new AuditLog();
        log.setLevel("INFO");
        log.setMessage("Process Event: " + message);
        log.setCreatedAt(LocalDateTime.now());
        log.setProcess(process);
        auditLogRepository.save(log);
    }

    // Deadlock loggen
    public void logDeadlock(Long processId, Integer bucketNr) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("Process not found with id: " + processId));
        AuditLog log = new AuditLog();
        log.setLevel("WARN");
        log.setMessage("Deadlock detected in bucket: " + bucketNr);
        log.setCreatedAt(LocalDateTime.now());
        log.setProcess(process);
        auditLogRepository.save(log);
    }
}