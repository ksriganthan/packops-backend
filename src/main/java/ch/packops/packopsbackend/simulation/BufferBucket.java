package ch.packops.packopsbackend.simulation;

import ch.packops.packopsbackend.domain.Portion;

public class BufferBucket implements Bucket {

    private Portion portion;

    @Override
    public void fill(Portion portion) {
        if (portion == null) {
            throw new IllegalArgumentException("portion must not be null");
        }
        if (!isEmpty()) {
            throw new IllegalStateException("BufferBucket is already filled");
        }
        this.portion = portion;
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

    public Portion getPortion() {
        return portion;
    }
}