package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.PackageUnit;
import ch.packops.packopsbackend.dto.StatisticsDto;
import ch.packops.packopsbackend.repository.PackageRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import org.springframework.stereotype.Service;
import ch.packops.packopsbackend.domain.Process;

import java.util.List;

/**
 * @author Kapischan
 */
@Service
public class StatisticsService {
    private final ProcessRepository processRepository;
    private final PackageRepository packageRepository;

    public StatisticsService(ProcessRepository processRepository,
                             PackageRepository packageRepository) {
        this.processRepository = processRepository;
        this.packageRepository = packageRepository;
    }

    /**
     * Gibt Statistiken eines einzelnen Prozesses zurück.
     * Diese Methode wird aktuell nicht über einen eigenen Endpunkt aufgerufen,
     * da die Prozessdetails gemäss API-Spezifikation bereits über
     * GET /api/process/{id} im ProcessController abgerufen werden.
     * Die Methode bleibt für eine mögliche zukünftige Erweiterung erhalten.
     */
    public StatisticsDto getProcessStatistics(Long processId) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("Process not found with id: " + processId));

        List<PackageUnit> packages = packageRepository.findByProcessId(processId);

        StatisticsDto dto = new StatisticsDto();
        dto.setTotalProcesses(1);
        dto.setTotalPackages(packages.size());
        dto.setDeadlocksDetected(process.getDeadlocksDetected() != null ? process.getDeadlocksDetected() : 0);

        if (!packages.isEmpty()) {
            double avgWeight = packages.stream()
                    .filter(p -> p.getMeasuredWeight() != null)
                    .mapToInt(PackageUnit::getMeasuredWeight)
                    .average()
                    .orElse(0.0);
            dto.setAverageWeight(avgWeight);
        } else {
            dto.setAverageWeight(0.0);
        }
        return dto;
    }

    // Gesamtübersicht aller Prozesse
    public StatisticsDto getOverviewStatistics() {
        List<Process> processes = processRepository.findAll();
        List<PackageUnit> allPackages = packageRepository.findAll();

        StatisticsDto dto = new StatisticsDto();
        dto.setTotalProcesses(processes.size());
        dto.setTotalPackages(allPackages.size());

        int totalDeadlocks = processes.stream()
                .filter(p -> p.getDeadlocksDetected() != null)
                .mapToInt(Process::getDeadlocksDetected)
                .sum();
        dto.setDeadlocksDetected(totalDeadlocks);

        if (!allPackages.isEmpty()) {
            double avgWeight = allPackages.stream()
                    .filter(p -> p.getMeasuredWeight() != null)
                    .mapToInt(PackageUnit::getMeasuredWeight)
                    .average()
                    .orElse(0.0);
            dto.setAverageWeight(avgWeight);
        } else {
            dto.setAverageWeight(0.0);
        }
        return dto;
    }
}