package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.dto.*;
import ch.packops.packopsbackend.repository.ProcessRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessService {

    private final ProcessRepository processRepository;

    public ProcessService(ProcessRepository processRepository) {
        this.processRepository = processRepository;
    }

    /**
     * @author Kapischan
     */

    // Domain → ProcessDto
    private ProcessDto toDto(Process process) {
        ProcessDto dto = new ProcessDto();
        dto.setProcessId(process.getId());
        dto.setStatus(process.getStatus());
        dto.setStartTimestamp(process.getStartTimestamp());
        dto.setEndTimestamp(process.getEndTimestamp());
        dto.setTargetWeight(process.getTargetWeight());
        dto.setTolerance(process.getTolerance());
        dto.setMaxUnits(process.getMaxUnits());
        dto.setMaxIterationsForReject(process.getMaxIterationsForReject());
        dto.setUnitsPacked(process.getUnitsPacked());
        if (process.getUser() != null) {
            dto.setUserId(process.getUser().getId());
        }
        return dto;
    }

    // Domain → ProcessDetailDto
    private ProcessDetailDto toDetailDto(Process process) {
        ProcessDetailDto dto = new ProcessDetailDto();
        dto.setId(process.getId());
        dto.setStatus(process.getStatus());
        dto.setStartTimestamp(process.getStartTimestamp());
        dto.setEndTimestamp(process.getEndTimestamp());
        dto.setTargetWeight(process.getTargetWeight());
        dto.setTolerance(process.getTolerance());
        dto.setMaxUnits(process.getMaxUnits());
        dto.setMaxIterationsForReject(process.getMaxIterationsForReject());
        dto.setUnitsPacked(process.getUnitsPacked());
        if (process.getUser() != null) {
            dto.setUserId(process.getUser().getId());
        }
        return dto;
    }

    public List<ProcessDto> getAllProcesses() {
        return processRepository.findAll()
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProcessDto> getProcessesByUserId(Long userId) {
        return processRepository.findByUserId(userId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProcessDetailDto getProcessById(Long id) {
        Process process = processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found with id: " + id));
        return toDetailDto(process);
    }

    public Process getProcessDomainById(Long id) {
        return processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found with id: " + id));
    }

    public Process startProcess(ProcessStartDto dto) {
        // TODO: implement
        return null;
    }

    public void stopProcess(Long processId) {
        // TODO: implement
    }

    public ProcessStatusDto getStatus(Long processId) {
        // TODO: implement
        return null;
    }

    public List<Process> getProcesses() {
        // TODO: implement
        return null;
    }

    public Process getProcess(Long processId) {
        // TODO: implement
        return null;
    }
}