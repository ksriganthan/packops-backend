package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.domain.Configuration;
import ch.packops.packopsbackend.domain.PackageUnit;
import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.*;
import ch.packops.packopsbackend.repository.ConfigurationRepository;
import ch.packops.packopsbackend.repository.PackageRepository;
import ch.packops.packopsbackend.repository.PortionRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import ch.packops.packopsbackend.repository.UserRepository;
import ch.packops.packopsbackend.simulation.CombinationAlgorithm;
import ch.packops.packopsbackend.simulation.InputSimulator;
import ch.packops.packopsbackend.simulation.RuntimeSnapshot;
import ch.packops.packopsbackend.simulation.SimulationManager;
import ch.packops.packopsbackend.simulation.WeighingCore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ProcessService {

    private final ProcessRepository processRepository;
    private final ProductConfigurationRepository productConfigurationRepository;
    private final ConfigurationRepository configurationRepository;
    private final PackageRepository packageRepository;
    private final PortionRepository portionRepository;
    private final UserRepository userRepository;
    private final LoggingService loggingService;
    private final Map<Long, SimulationManager> runningSimulations = new ConcurrentHashMap<>();

    public ProcessService(
            ProcessRepository processRepository,
            ProductConfigurationRepository productConfigurationRepository,
            ConfigurationRepository configurationRepository,
            PackageRepository packageRepository,
            PortionRepository portionRepository,
            UserRepository userRepository,
            LoggingService loggingService) {
        this.processRepository = processRepository;
        this.productConfigurationRepository = productConfigurationRepository;
        this.configurationRepository = configurationRepository;
        this.packageRepository = packageRepository;
        this.portionRepository = portionRepository;
        this.userRepository = userRepository;
        this.loggingService = loggingService;
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
        if (process.getProductConfiguration() != null) {
            dto.setProductConfigurationId(process.getProductConfiguration().getId());
        }
        if (process.getConfiguration() != null) {
            dto.setConfigurationId(process.getConfiguration().getId());
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
        if (process.getProductConfiguration() != null) {
            dto.setProductConfigurationId(process.getProductConfiguration().getId());
        }
        if (process.getConfiguration() != null) {
            dto.setConfigurationId(process.getConfiguration().getId());
        }
        return dto;
    }

    public List<ProcessDto> getAllProcesses() {
        loggingService.logInfo("Prozesshistorie abgerufen", null);
        return processRepository.findAll()
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProcessDto> getProcessesByUserId(Long userId) {
        loggingService.logInfo("Prozesse abgerufen für User: " + userId, null);
        return processRepository.findByUserId(userId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProcessDetailDto getProcessById(Long id) {
        loggingService.logInfo("Prozessdetails abgerufen für Prozess: " + id, id);
        Process process = processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found with id: " + id));
        return toDetailDto(process);
    }

    public Process getProcessDomainById(Long id) {
        return processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found with id: " + id));
    }

    /**
     * @author David T.
     */
    public ProcessDto startProcessDto(ProcessStartDto dto, Long userId) {
        return toDto(startProcess(dto, userId));
    }

    public Process startProcess(ProcessStartDto dto) {
        return startProcess(dto, null);
    }

    public Process startProcess(ProcessStartDto dto, Long userId) {
        if (dto == null) {
            dto = new ProcessStartDto();
        }
        final ProcessStartDto request = dto;

        boolean alreadyRunning = processRepository.findAll().stream()
                .anyMatch(process -> "RUNNING".equalsIgnoreCase(process.getStatus()));

        if (alreadyRunning) {
            throw new IllegalArgumentException("Es läuft bereits eine Simulation.");
        }

        ProductConfiguration productConfiguration = request.getProductConfigurationId() != null
                ? productConfigurationRepository.findById(request.getProductConfigurationId())
                .orElseThrow(() -> new IllegalArgumentException("ProductConfiguration not found with id: " + request.getProductConfigurationId()))
                : null;

        Configuration configuration = request.getConfigurationId() != null
                ? configurationRepository.findById(request.getConfigurationId())
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found with id: " + request.getConfigurationId()))
                : configurationRepository.findAll().stream().findFirst().orElse(null);

        Process process = new Process();
        process.setProductConfiguration(productConfiguration);
        process.setConfiguration(configuration);
        process.setUser(resolveUser(userId));
        process.setTargetWeight(resolveTargetWeight(request, productConfiguration, configuration));
        process.setTolerance(resolveTolerance(request, productConfiguration, configuration));
        process.setMaxUnits(resolveMaxUnits(request, productConfiguration, configuration));
        process.setMaxIterationsForReject(resolveMaxIterations(request, configuration));
        process.setUnitsPacked(0);
        process.setDeadlocksDetected(0);
        process.setStatus("RUNNING");
        process.setStartTimestamp(LocalDateTime.now());

        Process savedProcess = processRepository.save(process);
        startSimulationForProcess(savedProcess);

        loggingService.logProcessEvent(savedProcess.getId(), "Simulation gestartet");

        return savedProcess;
    }

    public void stopProcess(Long processId) {
        SimulationManager simulationManager = runningSimulations.remove(processId);

        if (simulationManager != null) {
            simulationManager.stopSimulation();
        }

        Process process = getProcessDomainById(processId);

        if ("RUNNING".equalsIgnoreCase(process.getStatus())) {
            process.setStatus("STOPPED");
            process.setEndTimestamp(LocalDateTime.now());
            processRepository.save(process);
        }

        loggingService.logProcessEvent(processId, "Simulation gestoppt");
    }

    public ProcessStatusDto getStatus(Long processId) {
        Process process = getProcessDomainById(processId);

        RuntimeSnapshot snapshot = null;
        SimulationManager simulationManager = runningSimulations.get(processId);

        if (simulationManager != null) {
            snapshot = simulationManager.getRuntimeSnapshot();

            if (!"RUNNING".equalsIgnoreCase(snapshot.getStatus())) {
                runningSimulations.remove(processId);
            }
        }

        ProcessStatusDto dto = new ProcessStatusDto();
        dto.setProcessId(process.getId());
        dto.setStatus(snapshot != null ? snapshot.getStatus() : process.getStatus());
        dto.setRecentPortionWeight(snapshot != null ? snapshot.getRecentPortionWeight() : null);
        dto.setUnitsPacked(snapshot != null ? snapshot.getUnitsPacked() : safeInt(process.getUnitsPacked()));
        dto.setMaxUnits(snapshot != null ? snapshot.getMaxUnits() : safeInt(process.getMaxUnits()));
        dto.setMaxIterationsForReject(snapshot != null ? snapshot.getMaxIterationsForReject() : safeInt(process.getMaxIterationsForReject()));
        dto.setDeadlocksDetected(snapshot != null ? snapshot.getDeadlocksDetected() : safeInt(process.getDeadlocksDetected()));
        dto.setRecentMessage(snapshot != null ? snapshot.getRecentMessage() : "Keine aktive Simulation im Speicher");

        return dto;
    }

    private void startSimulationForProcess(Process process) {
        InputSimulator inputSimulator = new InputSimulator();

        WeighingCore weighingCore = new WeighingCore(
                process,
                inputSimulator,
                new CombinationAlgorithm(),
                packageRepository,
                portionRepository,
                processRepository,
                loggingService
        );

        SimulationManager simulationManager = new SimulationManager(inputSimulator, weighingCore);
        runningSimulations.put(process.getId(), simulationManager);
        simulationManager.startSimulation();
    }

    private int resolveTargetWeight(ProcessStartDto dto, ProductConfiguration product, Configuration configuration) {
        if (dto.getTargetWeight() != null) return dto.getTargetWeight();
        if (product != null && product.getDefaultTargetWeight() != null) return product.getDefaultTargetWeight();
        if (configuration.getTargetWeight() != null) return configuration.getTargetWeight();
        throw new IllegalStateException("No targetWeight configured");
    }

    private int resolveTolerance(ProcessStartDto dto, ProductConfiguration product, Configuration configuration) {
        if (dto.getTolerance() != null) return dto.getTolerance();
        if (product != null && product.getDefaultTolerance() != null) return product.getDefaultTolerance();
        if (configuration.getTolerance() != null) return configuration.getTolerance();
        throw new IllegalStateException("No tolerance configured");
    }

    private int resolveMaxUnits(ProcessStartDto dto, ProductConfiguration product, Configuration configuration) {
        if (dto.getMaxUnits() != null) return dto.getMaxUnits();
        if (product != null && product.getPackageUnits() != null) return product.getPackageUnits();
        if (configuration.getMaxUnits() != null) return configuration.getMaxUnits();
        throw new IllegalStateException("No maxUnits configured");
    }

    private int resolveMaxIterations(ProcessStartDto dto, Configuration configuration) {
        if (dto.getMaxIterationsForReject() != null) return dto.getMaxIterationsForReject();
        if (configuration.getMaxIterations() != null) return configuration.getMaxIterations();
        throw new IllegalStateException("No maxIterations configured");
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private User resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId).orElse(null);
    }

}