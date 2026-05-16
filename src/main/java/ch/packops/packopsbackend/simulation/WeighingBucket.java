package ch.packops.packopsbackend.simulation;

import ch.packops.packopsbackend.domain.Portion;

import java.time.LocalDateTime;

public class WeighingBucket implements Bucket {

    private final int bucketNr;
    private Portion portion;

    public WeighingBucket(int bucketNr) {
        this.bucketNr = bucketNr;
    }

    @Override
    public void fill(Portion portion) {
        if (portion == null) {
            throw new IllegalArgumentException("portion must not be null");
        }
        if (!isEmpty()) {
            throw new IllegalStateException("WeighingBucket is already filled");
        }
        this.portion = portion;
        measureWeight();
    }

    @Override
    public Portion releasePortion() {
        Portion released = portion;
        portion = null;
        return released;
    }

    @Override
    public boolean isEmpty() {
        return portion == null;
    }

    public void measureWeight() {
        if (portion != null) {
            portion.setBucketNr(bucketNr);
            if (portion.getTimestamp() == null) {
                portion.setTimestamp(LocalDateTime.now());
            }
        }
    }

    public int getBucketNr() {
        return bucketNr;
    }

    public Portion getPortion() {
        return portion;
    }
}