package ch.packops.packopsbackend.simulation;

import ch.packops.packopsbackend.domain.Portion;

import java.time.LocalDateTime;
import java.util.Random;

public class InputSimulator {

    private final Random random = new Random();
    private boolean running;
    private static final int MIN_EXPECTED_PORTIONS_PER_PACKAGE = 5;
    private static final int MAX_SINGLE_PORTION_FACTOR = 2;

    public Portion generatePortion(int targetWeight) {

        int minWeight = Math.max(1,
                targetWeight / MIN_EXPECTED_PORTIONS_PER_PACKAGE);

        int maxWeight = Math.max(minWeight + 1,
                targetWeight / MAX_SINGLE_PORTION_FACTOR);

        int weight = minWeight + random.nextInt(maxWeight - minWeight + 1);

        Portion portion = new Portion();
        portion.setMeasuredWeight(weight);

        return portion;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}