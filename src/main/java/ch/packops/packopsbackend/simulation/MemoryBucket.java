package ch.packops.packopsbackend.simulation;

import ch.packops.packopsbackend.domain.Portion;

public class MemoryBucket implements Bucket {

    @Override
    public void fill(Portion portion) {
        // TODO: implement
    }

    @Override
    public Portion releasePortion() {
        // TODO: implement
        return null;
    }

    @Override
    public boolean isEmpty() {
        // TODO: implement
        return true;
    }

    public void incrementIterations() {
        // TODO: implement
    }

    public void resetIterations() {
        // TODO: implement
    }

    public boolean isDeadlocked() {
        // TODO: implement
        return false;
    }
}