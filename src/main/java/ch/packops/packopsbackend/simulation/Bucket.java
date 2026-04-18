package ch.packops.packopsbackend.simulation;

import ch.packops.packopsbackend.domain.Portion;

public interface Bucket {

    void fill(Portion portion);

    Portion releasePortion();

    boolean isEmpty();
}