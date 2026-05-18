package ch.packops.packopsbackend.simulation;

import ch.packops.packopsbackend.domain.PackageUnit;
import ch.packops.packopsbackend.domain.Portion;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.repository.PackageRepository;
import ch.packops.packopsbackend.repository.PortionRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.service.LoggingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WeighingCore {

    public static final int CHANNEL_COUNT = 8;
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
    private String recentMessage = "Simulation gestartet";

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

    public void shiftPortionsDown() {
        //Zuerst wird aus dem Weighing die Ladung in die MemoryBucket übergeben
        moveWeighingToMemory();
        //Dann wird aus dem Buffer das WeighingBucket befüllt
        moveBufferToWeighing();
        //Die nun leeren Buffer werden gefüllt mit neuem Input
        fillEmptyBuffers();
    }

    private void moveWeighingToMemory() {
        for (int channelIndex = 0; channelIndex < CHANNEL_COUNT; channelIndex++) {
            MemoryBucket freeMemoryBucket = findFreeMemoryBucketForChannel(channelIndex);

            if (freeMemoryBucket != null && !weighingBuckets[channelIndex].isEmpty()) {
                Portion portion = weighingBuckets[channelIndex].releasePortion();
                freeMemoryBucket.fill(portion);

                recentMessage = "Portion in MemoryBucket "
                        + freeMemoryBucket.getBucketNr()
                        + " verschoben";
            }
        }
    }

    private void moveBufferToWeighing() {
        for (int channelIndex = 0; channelIndex < CHANNEL_COUNT; channelIndex++) {
            if (weighingBuckets[channelIndex].isEmpty() && !bufferBuckets[channelIndex].isEmpty()) {
                Portion portion = bufferBuckets[channelIndex].releasePortion();
                weighingBuckets[channelIndex].fill(portion);

                recentPortionWeight = portion.getMeasuredWeight();
                recentMessage = "WeighingBucket "
                        + weighingBuckets[channelIndex].getBucketNr()
                        + " gemessen";
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
                    recentMessage = "Neue Portion in BufferBucket "
                            + (channelIndex + 1)
                            + " eingefüllt";
                }
            }
        }
    }

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
            recentMessage = "Keine passende Kombination gefunden";
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
        recentMessage = "Package " + savedPackage.getId() + " erstellt (" + totalWeight + "g)";

        recentSelectedBuckets = selectedBuckets.stream().map(MemoryBucket::getBucketNr).toList();
    }

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
            recentMessage = "Deadlock in MemoryBucket " + bucket.getBucketNr() + " zurückgeführt";
        }
    }

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
        recentMessage = "Simulation gestoppt";
    }

    private void finishProcess() {
        process.setStatus("FINISHED");
        process.setEndTimestamp(LocalDateTime.now());
        processRepository.save(process);
        recentMessage = "Maximale Anzahl Packages erreicht";
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
}