package ch.packops.packopsbackend.controller;

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

    @PostMapping("/start")
    public ResponseEntity<ProcessDto> startProcess(@RequestBody ProcessStartDto dto) {
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

    @GetMapping
    public ResponseEntity<List<ProcessDto>> getProcesses() {
        return ResponseEntity.ok(processService.getProcesses());
    }

    @GetMapping("/{processId}")
    public ResponseEntity<ProcessDto> getProcess(@PathVariable Long processId) {
        return ResponseEntity.ok(processService.getProcess(processId));
    }
}