package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.ProcessDetailDto;
import ch.packops.packopsbackend.dto.ProcessDto;
import ch.packops.packopsbackend.dto.ProcessStartDto;
import ch.packops.packopsbackend.dto.ProcessStatusDto;
import ch.packops.packopsbackend.security.AuthService;
import ch.packops.packopsbackend.security.AuthorizationService;
import ch.packops.packopsbackend.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private final ProcessService processService;
    private final AuthService authService;
    private final AuthorizationService authorizationService;

    public ProcessController(ProcessService processService,
                             AuthService authService,
                             AuthorizationService authorizationService) {
        this.processService = processService;
        this.authService = authService;
        this.authorizationService = authorizationService;
    }

    /**
     * @author Kapischan
     */

    // GET /api/process
    @GetMapping
    public ResponseEntity<?> getProcesses(@RequestParam String token) {
        try {
            User user = authService.authenticate(token);
            if (authorizationService.canManageUsers(user)) {
                // Admin → alle Prozesse
                List<ProcessDto> processes = processService.getAllProcesses();
                return ResponseEntity.ok(processes);
            } else {
                // Operator → nur eigene Prozesse
                List<ProcessDto> processes = processService.getProcessesByUserId(user.getId());
                return ResponseEntity.ok(processes);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // GET /api/process/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getProcess(
            @PathVariable Long id,
            @RequestParam String token) {
        try {
            User user = authService.authenticate(token);
            ProcessDetailDto process = processService.getProcessById(id);
            Process domainProcess = processService.getProcessDomainById(id);
            if (!authorizationService.canViewProcess(user, domainProcess)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            return ResponseEntity.ok(process);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }


    @PostMapping("/start")
    public ResponseEntity<Process> startProcess(@RequestBody ProcessStartDto dto) {
        // Todo
        return ResponseEntity.ok(processService.startProcess(dto));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopProcess(@PathVariable Long id) {
        // Todo
        processService.stopProcess(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ProcessStatusDto> getStatus(@PathVariable Long id) {
        // Todo
        return ResponseEntity.ok(processService.getStatus(id));
    }

}