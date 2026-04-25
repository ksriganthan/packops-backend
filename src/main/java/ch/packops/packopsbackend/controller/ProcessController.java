package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.dto.ProcessDetailDto;
import ch.packops.packopsbackend.dto.ProcessDto;
import ch.packops.packopsbackend.dto.ProcessStartDto;
import ch.packops.packopsbackend.dto.ProcessStatusDto;
import ch.packops.packopsbackend.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    /**
     * @author Kapischan
     */

    // GET /api/process
    @GetMapping
    public ResponseEntity<List<ProcessDto>> getProcesses(
            @RequestParam String token) {
        // TODO: Token-Validierung
        // TODO: Rollenbasierte Filterung
        try {
            List<ProcessDto> processes = processService.getAllProcesses();
            return ResponseEntity.ok(processes);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/process/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProcessDetailDto> getProcess(
            @PathVariable Long id,
            @RequestParam String token) {
        // TODO: Token-Validierung
        try {
            ProcessDetailDto process = processService.getProcessById(id);
            return ResponseEntity.ok(process);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/start")
    public ResponseEntity<Process> startProcess(@RequestBody ProcessStartDto dto) {
        return ResponseEntity.ok(processService.startProcess(dto));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopProcess(@PathVariable Long id) {
        processService.stopProcess(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ProcessStatusDto> getStatus(@PathVariable Long id) {
        return ResponseEntity.ok(processService.getStatus(id));
    }

}