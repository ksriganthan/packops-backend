package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.dto.ProcessStartDto;
import ch.packops.packopsbackend.dto.ProcessStatusDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessService {

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