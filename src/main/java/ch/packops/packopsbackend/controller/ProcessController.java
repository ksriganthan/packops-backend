package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.dto.ProcessDetailDto;
import ch.packops.packopsbackend.dto.ProcessDto;
import ch.packops.packopsbackend.dto.ProcessStartDto;
import ch.packops.packopsbackend.dto.ProcessStatusDto;
import ch.packops.packopsbackend.service.ProcessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private static final String ROLE_ADMIN = "admin";

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    // GET /api/process
    // Admin sieht alle Prozesse, andere User nur eigene Prozesse
    @GetMapping
    public ResponseEntity<List<ProcessDto>> getProcesses(@AuthenticationPrincipal Jwt jwt) {
        if (isAdmin(jwt)) {
            return ResponseEntity.ok(processService.getAllProcesses());
        }

        Long userId = getCurrentUserId(jwt);
        return ResponseEntity.ok(processService.getProcessesByUserId(userId));
    }

    // Jeder authentifizierte User darf den Status sehen (Annahme 1 globale Maschine)
    @GetMapping("/{id}")
    public ResponseEntity<ProcessDetailDto> getProcess(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(processService.getProcessById(id));
    }

    // POST /api/process/start
    // Nur admin/operator gemaess SecurityConfig
    @PostMapping("/start")
    public ResponseEntity<ProcessDto> startProcess(
            @Valid @RequestBody ProcessStartDto dto,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(processService.startProcessDto(dto, getCurrentUserId(jwt)));
    }

    // POST /api/process/{id}/stop
    // Nur admin/operator gemaess SecurityConfig
    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopProcess(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Process domainProcess = processService.getProcessDomainById(id);

        if (!isAdmin(jwt) && !isOwnProcess(jwt, domainProcess)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        processService.stopProcess(id);
        return ResponseEntity.noContent().build();
    }

    // Jeder authentifizierte User darf den Status sehen (Annahme 1 globale Maschine)
    @GetMapping("/{id}/status")
    public ResponseEntity<ProcessStatusDto> getStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(processService.getStatus(id));
    }

    @GetMapping("/active")
    public ResponseEntity<ProcessDto> getActiveProcess() {
        ProcessDto activeProcess = processService.getActiveProcess();
        if (activeProcess != null) {
            return ResponseEntity.ok(activeProcess);
        }
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Jwt jwt) {
        if (jwt == null) {
            return false;
        }
        String role = jwt.getClaimAsString("role");
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    private Long getCurrentUserId(Jwt jwt) {
        return jwt != null ? jwt.getClaim("userId") : null;
    }

    private boolean isOwnProcess(Jwt jwt, Process process) {
        Long currentUserId = getCurrentUserId(jwt);

        return currentUserId != null
                && process != null
                && process.getUser() != null
                && process.getUser().getId() != null
                && process.getUser().getId().equals(currentUserId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}