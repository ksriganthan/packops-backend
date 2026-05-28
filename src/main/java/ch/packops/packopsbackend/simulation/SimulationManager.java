package ch.packops.packopsbackend.simulation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author David T.
 */

public class SimulationManager {

    // Zeitabstand zwischen zwei Simulationsticks
    private static final long DEFAULT_TICK_INTERVAL_MS = 500L;

    // Aktiviert eine Konsolenausgabe des aktuellen Simulationszustands pro Tick
    private static final boolean DEBUG_CONSOLE_OUTPUT = true;

    private final InputSimulator inputSimulator;
    private final WeighingCore weighingCore;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;

    public SimulationManager(InputSimulator inputSimulator, WeighingCore weighingCore) {
        this.inputSimulator = inputSimulator;
        this.weighingCore = weighingCore;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public synchronized void startSimulation() {
        if (scheduledTask != null && !scheduledTask.isCancelled() && !scheduledTask.isDone()) {
            return;
        }

        inputSimulator.start();
        scheduledTask = scheduler.scheduleAtFixedRate(
                this::runTick,
                0,
                DEFAULT_TICK_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    private void runTick() {
        weighingCore.processTick();
        RuntimeSnapshot snapshot = weighingCore.getRuntimeSnapshot();

        if (DEBUG_CONSOLE_OUTPUT) {
            printSnapshot(snapshot);
        }

        if (!"RUNNING".equalsIgnoreCase(snapshot.getStatus())) {
            inputSimulator.stop();
            if (scheduledTask != null) {
                scheduledTask.cancel(false);
            }
            scheduler.shutdown();
        }
    }

    public synchronized void stopSimulation() {
        inputSimulator.stop();
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        weighingCore.stopProcess();
        scheduler.shutdownNow();
    }

    public RuntimeSnapshot getRuntimeSnapshot() {
        return weighingCore.getRuntimeSnapshot();
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
}