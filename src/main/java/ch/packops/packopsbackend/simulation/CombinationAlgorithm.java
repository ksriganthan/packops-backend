package ch.packops.packopsbackend.simulation;

import ch.packops.packopsbackend.domain.Portion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CombinationAlgorithm {


        public List<MemoryBucket> findBestBucketCombination(List<MemoryBucket> buckets, int targetWeight, int tolerance) {
        if (buckets == null || buckets.isEmpty()) {
            return Collections.emptyList();
        }

        List<MemoryBucket> occupied = buckets.stream()
                .filter(bucket -> !bucket.isEmpty())
                .sorted(Comparator.comparingInt(MemoryBucket::getBucketNr))
                .toList();

        if (occupied.isEmpty()) {
            return Collections.emptyList();
        }

        List<MemoryBucket> best = Collections.emptyList();
        int bestDeviation = Integer.MAX_VALUE;
        int bestCount = Integer.MAX_VALUE;
        int numberOfCombinations = 1 << occupied.size();

        for (int mask = 1; mask < numberOfCombinations; mask++) {
            List<MemoryBucket> candidate = new ArrayList<>();
            int sum = 0;

            for (int i = 0; i < occupied.size(); i++) {
                if ((mask & (1 << i)) != 0) {
                    MemoryBucket bucket = occupied.get(i);
                    candidate.add(bucket);
                    sum += safeWeight(bucket.getPortion());
                }
            }

            int deviation = Math.abs(targetWeight - sum);
            if (deviation <= tolerance
                    && (deviation < bestDeviation || (deviation == bestDeviation && candidate.size() < bestCount))) {
                best = candidate;
                bestDeviation = deviation;
                bestCount = candidate.size();
            }
        }

        return best;
    }

    private int safeWeight(Portion portion) {
        return portion != null && portion.getMeasuredWeight() != null ? portion.getMeasuredWeight() : 0;
    }
}