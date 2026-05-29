package ch.packops.packopsbackend.simulation;

import ch.packops.packopsbackend.domain.PackageUnit;
import ch.packops.packopsbackend.domain.Portion;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.repository.PackageRepository;
import ch.packops.packopsbackend.repository.PortionRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.service.LoggingService;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author David T.
 */


public class WeighingCore {

    // Anzahl paralleler Kanäle der simulierten Wiegeanlage
    public static final int CHANNEL_COUNT = 8;
    // Pro Kanal werden zwei MemoryBuckets verwendet
    public static final int MEMORY_BUCKETS_PER_CHANNEL = 2;

    private final Process process;
    private final InputSimulator inputSimulator;
    private final CombinationAlgorithm combinationAlgorithm;
    private final PackageRepository packageRepository;
    private final PortionRepository portionRepository;
    private final ProcessRepository processRepository;
    private final LoggingService loggingService;

    private final BufferBucket[] bufferBuckets = new BufferBucket[CHANNEL_COUNT];
    private final WeighingBucket[] weighingBuckets = new WeighingBucket[CHANNEL_COUNT];
    private final MemoryBucket[] memoryBuckets = new MemoryBucket[CHANNEL_COUNT * MEMORY_BUCKETS_PER_CHANNEL];

    private int tick;
    private int recentPortionWeight;
    private Map<String, String> recentMessage;

    private List<Integer> recentSelectedBuckets = new ArrayList<>();

    public WeighingCore(Process process,
                        InputSimulator inputSimulator,
                        CombinationAlgorithm combinationAlgorithm,
                        PackageRepository packageRepository,
                        PortionRepository portionRepository,
                        ProcessRepository processRepository,
                        LoggingService loggingService) {
        this.process = Objects.requireNonNull(process, "process must not be null");
        this.inputSimulator = Objects.requireNonNull(inputSimulator, "inputSimulator must not be null");
        this.combinationAlgorithm = Objects.requireNonNull(combinationAlgorithm, "combinationAlgorithm must not be null");
        this.packageRepository = Objects.requireNonNull(packageRepository, "packageRepository must not be null");
        this.portionRepository = Objects.requireNonNull(portionRepository, "portionRepository must not be null");
        this.processRepository = Objects.requireNonNull(processRepository, "processRepository must not be null");
        this.loggingService = loggingService;

        this.recentMessage = msgSimulationStarted();
        initializeBuckets();
    }

    private void initializeBuckets() {
        int maxIterations = safeMaxIterations();
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            int bucketNr = i + 1;
            bufferBuckets[i] = new BufferBucket();
            weighingBuckets[i] = new WeighingBucket(bucketNr);
            memoryBuckets[i * 2] = new MemoryBucket(i * 2 + 1, maxIterations);
            memoryBuckets[i * 2 + 1] = new MemoryBucket(i * 2 + 2, maxIterations);
        }
    }

    /**
     Führt einen einzelnen Simulationstick aus.
     Dabei werden zuerst Portionen weitertransportiert,
     anschliessend wird eine passende Kombination gesucht
     und zuletzt werden mögliche Deadlocks behandelt.
     */
    public synchronized void processTick() {
        if (!"RUNNING".equalsIgnoreCase(process.getStatus())) {
            return;
        }

        shiftPortionsDown();
        createPackage();
        handleDeadlocks();
        tick++;

        if (process.getUnitsPacked() != null
                && process.getMaxUnits() != null
                && process.getMaxUnits() > 0
                && process.getUnitsPacked() >= process.getMaxUnits()) {
            finishProcess();
        }
    }

    /**
     Transportiert Portionen nach dem Pull-Prinzip weiter:
     WeighingBucket -> MemoryBucket,
     BufferBucket -> WeighingBucket,
     InputSimulator -> BufferBucket.
     */
    public void shiftPortionsDown() {
        moveWeighingToMemory();
        moveBufferToWeighing();
        fillEmptyBuffers();
    }

    private void moveWeighingToMemory() {
        for (int channelIndex = 0; channelIndex < CHANNEL_COUNT; channelIndex++) {
            MemoryBucket freeMemoryBucket = findFreeMemoryBucketForChannel(channelIndex);

            if (freeMemoryBucket != null && !weighingBuckets[channelIndex].isEmpty()) {
                Portion portion = weighingBuckets[channelIndex].releasePortion();
                freeMemoryBucket.fill(portion);
                recentMessage = msgPortionMovedToMemory(freeMemoryBucket.getBucketNr());
            }
        }
    }

    private void moveBufferToWeighing() {
        for (int channelIndex = 0; channelIndex < CHANNEL_COUNT; channelIndex++) {
            if (weighingBuckets[channelIndex].isEmpty() && !bufferBuckets[channelIndex].isEmpty()) {
                Portion portion = bufferBuckets[channelIndex].releasePortion();
                weighingBuckets[channelIndex].fill(portion);
                recentPortionWeight = portion.getMeasuredWeight();
                recentMessage = msgWeighingBucketMeasured(weighingBuckets[channelIndex].getBucketNr());
            }
        }
    }

    private void fillEmptyBuffers() {
        for (int channelIndex = 0; channelIndex < CHANNEL_COUNT; channelIndex++) {
            if (bufferBuckets[channelIndex].isEmpty()) {
                Portion generated = inputSimulator.generatePortion(safeTargetWeight());

                if (generated != null) {
                    bufferBuckets[channelIndex].fill(generated);
                    recentPortionWeight = generated.getMeasuredWeight();
                    recentMessage = msgBufferBucketFilled(channelIndex + 1);
                }
            }
        }
    }

    /**
     Sucht aus allen belegten MemoryBuckets eine gültige Gewichtskombination.
     Falls eine Kombination innerhalb der Toleranz gefunden wird,
     wird daraus eine PackageUnit erstellt und persistiert.
     */
    public void createPackage() {
        List<MemoryBucket> filledBuckets = Arrays.stream(memoryBuckets)
                .filter(bucket -> !bucket.isEmpty())
                .toList();

        List<MemoryBucket> selectedBuckets = combinationAlgorithm.findBestBucketCombination(
                filledBuckets,
                safeTargetWeight(),
                safeTolerance()
        );

        if (selectedBuckets.isEmpty()) {
            filledBuckets.forEach(MemoryBucket::incrementIterations);
            recentMessage = msgNoCombinationFound();
            return;
        }

        int totalWeight = selectedBuckets.stream()
                .map(MemoryBucket::getPortion)
                .filter(Objects::nonNull)
                .mapToInt(portion -> portion.getMeasuredWeight() != null ? portion.getMeasuredWeight() : 0)
                .sum();

        PackageUnit packageUnit = new PackageUnit();
        packageUnit.setProcess(process);
        packageUnit.setMeasuredWeight(totalWeight);
        packageUnit.setDeviation(totalWeight - safeTargetWeight());
        packageUnit.setWasRefeed(false);
        PackageUnit savedPackage = packageRepository.save(packageUnit);

        for (MemoryBucket bucket : selectedBuckets) {
            Portion portion = bucket.releasePortion();
            if (portion == null) {
                continue;
            }
            portion.setBucketNr(bucket.getBucketNr());
            portion.setPackageUnit(savedPackage);
            if (portion.getTimestamp() == null) {
                portion.setTimestamp(LocalDateTime.now());
            }
            portionRepository.save(portion);
        }

        int unitsPacked = process.getUnitsPacked() != null ? process.getUnitsPacked() : 0;
        process.setUnitsPacked(unitsPacked + 1);
        processRepository.save(process);

        recentMessage = msgPackageCreated(savedPackage.getId(), totalWeight);
        recentSelectedBuckets = selectedBuckets.stream().map(MemoryBucket::getBucketNr).toList();
    }

    /**
     Behandelt Portionen, die über mehrere Ticks hinweg
     keine passende Kombination bilden konnten.
     Diese Portionen werden zurückgeführt und ohne PackageUnit gespeichert.
     */
    public void handleDeadlocks() {
        for (MemoryBucket bucket : memoryBuckets) {
            if (!bucket.isDeadlocked()) {
                continue;
            }

            Portion rejectedPortion = bucket.releasePortion();
            if (rejectedPortion != null) {
                rejectedPortion.setBucketNr(bucket.getBucketNr());
                rejectedPortion.setPackageUnit(null);
                if (rejectedPortion.getTimestamp() == null) {
                    rejectedPortion.setTimestamp(LocalDateTime.now());
                }
                portionRepository.save(rejectedPortion);
            }

            int deadlocks = process.getDeadlocksDetected() != null ? process.getDeadlocksDetected() : 0;
            process.setDeadlocksDetected(deadlocks + 1);
            processRepository.save(process);

            if (loggingService != null && process.getId() != null) {
                loggingService.logDeadlock(process.getId(), bucket.getBucketNr());
            }
            recentMessage = msgDeadlockResolved(bucket.getBucketNr());
        }
    }

    /**
     Erstellt eine Momentaufnahme der laufenden Simulation.
     Diese Daten werden für Statusabfragen, Frontend-Anzeige
     und Debugging verwendet.
     */
    public synchronized RuntimeSnapshot getRuntimeSnapshot() {
        RuntimeSnapshot snapshot = new RuntimeSnapshot();
        snapshot.setProcessId(process.getId());
        snapshot.setStatus(process.getStatus());
        snapshot.setTick(tick);
        snapshot.setUnitsPacked(process.getUnitsPacked() != null ? process.getUnitsPacked() : 0);
        snapshot.setMaxUnits(process.getMaxUnits() != null ? process.getMaxUnits() : 0);
        snapshot.setMaxIterationsForReject(process.getMaxIterationsForReject() != null ? process.getMaxIterationsForReject() : 0);
        snapshot.setDeadlocksDetected(process.getDeadlocksDetected() != null ? process.getDeadlocksDetected() : 0);
        snapshot.setRecentPortionWeight(recentPortionWeight);
        snapshot.setRecentMessage(recentMessage);
        snapshot.setBufferBuckets(createBufferSnapshots());
        snapshot.setWeighingBuckets(createWeighingSnapshots());
        snapshot.setMemoryBuckets(createMemorySnapshots());
        snapshot.setRecentSelectedBuckets(new ArrayList<>(recentSelectedBuckets));
        return snapshot;
    }

    public synchronized void stopProcess() {
        process.setStatus("STOPPED");
        process.setEndTimestamp(LocalDateTime.now());
        processRepository.save(process);
        recentMessage = msgSimulationStopped();
    }

    private void finishProcess() {
        process.setStatus("FINISHED");
        process.setEndTimestamp(LocalDateTime.now());
        processRepository.save(process);
        recentMessage = msgMaxPackagesReached();
    }

    private MemoryBucket findFreeMemoryBucketForChannel(int channelIndex) {
        MemoryBucket first = memoryBuckets[channelIndex * 2];
        MemoryBucket second = memoryBuckets[channelIndex * 2 + 1];

        if (first.isEmpty()) {
            return first;
        }
        if (second.isEmpty()) {
            return second;
        }
        return null;
    }

    private List<RuntimeSnapshot.BucketSnapshot> createBufferSnapshots() {
        List<RuntimeSnapshot.BucketSnapshot> snapshots = new ArrayList<>();
        for (int i = 0; i < bufferBuckets.length; i++) {
            Portion portion = bufferBuckets[i].getPortion();
            snapshots.add(new RuntimeSnapshot.BucketSnapshot(i + 1, portion == null, portion != null ? portion.getMeasuredWeight() : 0, 0));
        }
        return snapshots;
    }

    private List<RuntimeSnapshot.BucketSnapshot> createWeighingSnapshots() {
        List<RuntimeSnapshot.BucketSnapshot> snapshots = new ArrayList<>();
        for (WeighingBucket bucket : weighingBuckets) {
            Portion portion = bucket.getPortion();
            snapshots.add(new RuntimeSnapshot.BucketSnapshot(bucket.getBucketNr(), portion == null, portion != null ? portion.getMeasuredWeight() : 0, 0));
        }
        return snapshots;
    }

    private List<RuntimeSnapshot.BucketSnapshot> createMemorySnapshots() {
        List<RuntimeSnapshot.BucketSnapshot> snapshots = new ArrayList<>();
        for (MemoryBucket bucket : memoryBuckets) {
            Portion portion = bucket.getPortion();
            snapshots.add(new RuntimeSnapshot.BucketSnapshot(bucket.getBucketNr(), portion == null, portion != null ? portion.getMeasuredWeight() : 0, bucket.getIterations()));
        }
        return snapshots;
    }

    private int safeTargetWeight() {
        if (process.getTargetWeight() == null) {
            throw new IllegalStateException("Process targetWeight is missing");
        }
        return process.getTargetWeight();
    }

    private int safeTolerance() {
        if (process.getTolerance() == null) {
            throw new IllegalStateException("Process tolerance is missing");
        }
        return process.getTolerance();
    }

    private int safeMaxIterations() {
        if (process.getMaxIterationsForReject() == null) {
            throw new IllegalStateException("Process maxIterationsForReject is missing");
        }
        return process.getMaxIterationsForReject();
    }

    // --- MEHRSPRACHIGE NACHRICHTEN ---

    private Map<String, String> msgSimulationStarted() {
        return Map.of(
                "de", "Simulation gestartet",
                "fr", "Simulation démarrée",
                "en", "Simulation started"
        );
    }

    private Map<String, String> msgPortionMovedToMemory(int bucketNr) {
        return Map.of(
                "de", "Portion in MemoryBucket " + bucketNr + " verschoben",
                "fr", "Portion déplacée vers MemoryBucket " + bucketNr,
                "en", "Portion moved to MemoryBucket " + bucketNr
        );
    }

    private Map<String, String> msgWeighingBucketMeasured(int bucketNr) {
        return Map.of(
                "de", "WeighingBucket " + bucketNr + " gemessen",
                "fr", "WeighingBucket " + bucketNr + " mesuré",
                "en", "WeighingBucket " + bucketNr + " measured"
        );
    }

    private Map<String, String> msgBufferBucketFilled(int bucketNr) {
        return Map.of(
                "de", "Neue Portion in BufferBucket " + bucketNr + " eingefüllt",
                "fr", "Nouvelle portion remplie dans BufferBucket " + bucketNr,
                "en", "New portion filled into BufferBucket " + bucketNr
        );
    }

    private Map<String, String> msgNoCombinationFound() {
        return Map.of(
                "de", "Keine passende Kombination gefunden",
                "fr", "Aucune combinaison appropriée trouvée",
                "en", "No suitable combination found"
        );
    }

    private Map<String, String> msgPackageCreated(Long packageId, int weight) {
        return Map.of(
                "de", "Package mit ID " + packageId + " erstellt (" + weight + "g)",
                "fr", "Paquet avec ID " + packageId + " créé (" + weight + "g)",
                "en", "Package with ID " + packageId + " created (" + weight + "g)"
        );
    }

    private Map<String, String> msgDeadlockResolved(int bucketNr) {
        return Map.of(
                "de", "Deadlock in MemoryBucket " + bucketNr + " zurückgeführt",
                "fr", "Blocage dans MemoryBucket " + bucketNr + " résolu",
                "en", "Deadlock in MemoryBucket " + bucketNr + " resolved"
        );
    }

    private Map<String, String> msgSimulationStopped() {
        return Map.of(
                "de", "Simulation gestoppt",
                "fr", "Simulation arrêtée",
                "en", "Simulation stopped"
        );
    }

    private Map<String, String> msgMaxPackagesReached() {
        return Map.of(
                "de", "Maximale Anzahl Packages erreicht",
                "fr", "Nombre maximum de paquets atteint",
                "en", "Maximum number of packages reached"
        );
    }
}