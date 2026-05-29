package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.*;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.dto.*;
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
    private final PackageRepository packageRepository;
    private final PortionRepository portionRepository;
    private final UserRepository userRepository;
    private final LoggingService loggingService;
    private final ValidationService validationService;
    private final Map<Long, SimulationManager> runningSimulations = new ConcurrentHashMap<>();

    public ProcessService(
            ProcessRepository processRepository,
            ProductConfigurationRepository productConfigurationRepository,
            PackageRepository packageRepository,
            PortionRepository portionRepository,
            UserRepository userRepository,
            LoggingService loggingService,
            ValidationService validationService) {
        this.processRepository = processRepository;
        this.productConfigurationRepository = productConfigurationRepository;
        this.packageRepository = packageRepository;
        this.portionRepository = portionRepository;
        this.userRepository = userRepository;
        this.loggingService = loggingService;
        this.validationService = validationService;
    }

    /**
     * @author Kapischan Sriganthan
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

        List<PackageUnit> packageUnits = packageRepository.findByProcessId(process.getId());

        List<PackageUnitDto> packageDtos = packageUnits.stream()
                .map(pkg -> toPackageDto(pkg, process))
                .collect(Collectors.toList());
        dto.setPackages(packageDtos);

        return dto;
    }

    private PackageUnitDto toPackageDto(PackageUnit pkg, Process process) {
        PackageUnitDto dto = new PackageUnitDto();
        dto.setId(pkg.getId());
        dto.setMeasuredWeight(pkg.getMeasuredWeight());
        dto.setDeviation(pkg.getDeviation());

        // Prüfung, ob es innerhalb der Toleranz liegt (nur wenn beide Werte vorhanden sind)
        if (pkg.getDeviation() != null && process.getTolerance() != null) {
            dto.setWithinTolerance(Math.abs(pkg.getDeviation()) <= process.getTolerance());
        }

        return dto;
    }

    public List<ProcessDto> getAllProcesses() {
        loggingService.logInfo("Prozesshistorie abgerufen", null);
        return processRepository.findAll()
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProcessDto getActiveProcess() {
        return processRepository.findAll().stream()
                .filter(process -> "RUNNING".equalsIgnoreCase(process.getStatus()))
                .findFirst()
                .map(this::toDto)
                .orElse(null);
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

    public Process startProcess(ProcessStartDto dto, Long userId) {
        if (dto == null) {
            dto = new ProcessStartDto();
        }

        validationService.validateProcess(dto);

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


        Process process = new Process();
        process.setProductConfiguration(productConfiguration);
        process.setUser(resolveUser(userId));
        process.setTargetWeight(resolveTargetWeight(request, productConfiguration));
        process.setTolerance(resolveTolerance(request, productConfiguration));
        process.setMaxUnits(resolveMaxUnits(request, productConfiguration));
        process.setMaxIterationsForReject(resolveMaxIterations(request));
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
        dto.setRecentMessage(snapshot != null ? snapshot.getRecentMessage() : Map.of(
                "de", "Keine aktive Simulation im Speicher",
                "fr", "Aucune simulation active en mémoire",
                "en", "No active simulation in memory"
        ));
        // NEU: Snapshot-Daten für die Buckets übergeben
        if (snapshot != null) {
            dto.setBufferBuckets(snapshot.getBufferBuckets());
            dto.setWeighingBuckets(snapshot.getWeighingBuckets());
            dto.setMemoryBuckets(snapshot.getMemoryBuckets());
            dto.setRecentSelectedBuckets(snapshot.getRecentSelectedBuckets());
        }

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

    private int resolveTargetWeight(ProcessStartDto dto, ProductConfiguration product) {
        if (dto.getTargetWeight() != null) return dto.getTargetWeight();
        if (product != null && product.getDefaultTargetWeight() != null) return product.getDefaultTargetWeight();
        throw new IllegalStateException("No targetWeight configured");
    }

    private int resolveTolerance(ProcessStartDto dto, ProductConfiguration product) {
        if (dto.getTolerance() != null) return dto.getTolerance();
        if (product != null && product.getDefaultTolerance() != null) return product.getDefaultTolerance();
        throw new IllegalStateException("No tolerance configured");
    }

    private int resolveMaxUnits(ProcessStartDto dto, ProductConfiguration product) {
        if (dto.getMaxUnits() != null) return dto.getMaxUnits();
        throw new IllegalStateException("No maxUnits configured");
    }

    private int resolveMaxIterations(ProcessStartDto dto) {
        if (dto.getMaxIterationsForReject() != null) return dto.getMaxIterationsForReject();
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