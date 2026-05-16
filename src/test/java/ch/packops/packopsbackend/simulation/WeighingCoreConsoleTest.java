package ch.packops.packopsbackend.simulation;


import ch.packops.packopsbackend.domain.PackageUnit;
import ch.packops.packopsbackend.domain.Portion;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.repository.PackageRepository;
import ch.packops.packopsbackend.repository.PortionRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.service.LoggingService;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeighingCoreConsoleTest {
//HIER KÖNNEN KONFIGURATIONSWERTE GETESTET WERDEN UM IN DER KONSOLE JEDEN CHANNEL UND BUCKET NACHVERFOLGEN ZU KÖNNEN

//    private static final int TARGET_WEIGHT = 250;
//    private static final int TOLERANCE = 5;
//    private static final int MAX_UNITS = 10;
//    private static final int MAX_ITERATIONS = 5;
//    private static final int TICKS_TO_PRINT = 20;
private static final int TARGET_WEIGHT = 999;
    private static final int TOLERANCE = 0;
    private static final int MAX_UNITS = 10;
    private static final int MAX_ITERATIONS = 3;
    private static final int TICKS_TO_PRINT = 8;

    private static final boolean USE_RANDOM_INPUT = false;

    /**
     * @author David T.
     */

    @Test
    void showSimulationTickByTickInConsole() {
        Process process = new Process();
        process.setStatus("RUNNING");
        process.setTargetWeight(TARGET_WEIGHT);
        process.setTolerance(TOLERANCE);
        process.setMaxUnits(MAX_UNITS);
        process.setMaxIterationsForReject(MAX_ITERATIONS);
        process.setUnitsPacked(0);
        process.setDeadlocksDetected(0);

        PackageRepository packageRepository = mock(PackageRepository.class);
        PortionRepository portionRepository = mock(PortionRepository.class);
        ProcessRepository processRepository = mock(ProcessRepository.class);
        LoggingService loggingService = mock(LoggingService.class);

        when(packageRepository.save(any(PackageUnit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(portionRepository.save(any(Portion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(processRepository.save(any(Process.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InputSimulator inputSimulator = USE_RANDOM_INPUT
                ? new InputSimulator()
                : new DebugInputSimulator();

        WeighingCore weighingCore = new WeighingCore(
                process,
                inputSimulator,
                new CombinationAlgorithm(),
                packageRepository,
                portionRepository,
                processRepository,
                loggingService
        );

        for (int i = 0; i < TICKS_TO_PRINT; i++) {
            weighingCore.processTick();
            RuntimeSnapshot snapshot = weighingCore.getRuntimeSnapshot();
            printSnapshot(snapshot);
        }
    }

    private void printSnapshot(RuntimeSnapshot snapshot) {
        System.out.println();
        System.out.println("Tick: " + snapshot.getTick()
                + " | Status: " + snapshot.getStatus()
                + " | Packages: " + snapshot.getUnitsPacked()
                + " | Deadlocks: " + snapshot.getDeadlocksDetected());
        System.out.println("Message: " + snapshot.getRecentMessage());
        System.out.println("---------------------------------------------------------------------");
        System.out.printf("%-8s %-10s %-10s %-12s %-12s%n",
                "Channel", "Buffer", "Weighing", "Memory A", "Memory B");

        for (int channel = 1; channel <= 8; channel++) {
            RuntimeSnapshot.BucketSnapshot buffer =
                    snapshot.getBufferBuckets().get(channel - 1);

            RuntimeSnapshot.BucketSnapshot weighing =
                    snapshot.getWeighingBuckets().get(channel - 1);

            RuntimeSnapshot.BucketSnapshot memoryA =
                    snapshot.getMemoryBuckets().get((channel - 1) * 2);

            RuntimeSnapshot.BucketSnapshot memoryB =
                    snapshot.getMemoryBuckets().get((channel - 1) * 2 + 1);

            System.out.printf("%-8d %-10s %-10s %-12s %-12s%n",
                    channel,
                    formatBucket(buffer),
                    formatBucket(weighing),
                    formatBucket(memoryA),
                    formatBucket(memoryB));
        }
    }

    private String formatBucket(RuntimeSnapshot.BucketSnapshot bucket) {
        if (bucket == null || bucket.isEmpty()) {
            return "-";
        }

        if (bucket.getIterations() > 0) {
            return bucket.getWeight() + "g(" + bucket.getIterations() + ")";
        }

        return bucket.getWeight() + "g";
    }

    static class DebugInputSimulator extends InputSimulator {

        private static final int START_WEIGHT = 40;
        private static final int STEP = 5;
        private static final int MAX_WEIGHT = 100;

        private int currentWeight = START_WEIGHT;

        @Override
        public Portion generatePortion(int targetWeight) {
            Portion portion = new Portion();
            portion.setMeasuredWeight(currentWeight);

            currentWeight += STEP;

            if (currentWeight > MAX_WEIGHT) {
                currentWeight = START_WEIGHT;
            }

            return portion;
        }
    }
}
