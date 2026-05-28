package ch.packops.packopsbackend.simulation;

import ch.packops.packopsbackend.domain.Portion;

/**
 * @author David T.
 */

public class MemoryBucket implements Bucket {

    private final int bucketNr;
    private Portion portion;
    // Zählt, wie viele Ticks eine Portion bereits im MemoryBucket liegt
    private int iterations;
    private int maxIterationsForReject;

    public MemoryBucket(int bucketNr, int maxIterationsForReject) {
        this.bucketNr = bucketNr;
        this.maxIterationsForReject = maxIterationsForReject;
    }

    @Override
    public void fill(Portion portion) {
        if (portion == null) {
            throw new IllegalArgumentException("portion must not be null");
        }
        if (!isEmpty()) {
            throw new IllegalStateException("MemoryBucket is already filled");
        }
        this.portion = portion;
        this.portion.setBucketNr(bucketNr);
        this.iterations = 0;
    }

    @Override
    public Portion releasePortion() {
        Portion released = portion;
        portion = null;
        iterations = 0;
        return released;
    }

    @Override
    public boolean isEmpty() {
        return portion == null;
    }

    public void incrementIterations() {
        if (!isEmpty()) {
            iterations++;
        }
    }

    public void resetIterations() {
        iterations = 0;
    }

    /**
     Eine Portion gilt als blockiert, wenn sie über die maximale Anzahl
     erlaubter Iterationen hinweg nicht verwendet werden konnte.
     */
    public boolean isDeadlocked() {
        return !isEmpty() && iterations >= maxIterationsForReject;
    }

    public int getBucketNr() {
        return bucketNr;
    }

    public Portion getPortion() {
        return portion;
    }

    public int getIterations() {
        return iterations;
    }

    public void setMaxIterationsForReject(int maxIterationsForReject) {
        this.maxIterationsForReject = maxIterationsForReject;
    }
}