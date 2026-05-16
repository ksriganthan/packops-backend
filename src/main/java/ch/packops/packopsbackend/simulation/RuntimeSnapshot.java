package ch.packops.packopsbackend.simulation;

import java.util.ArrayList;
import java.util.List;

public class RuntimeSnapshot {

    private Long processId;
    private String status;
    private int tick;
    private int unitsPacked;
    private int maxUnits;
    private int maxIterationsForReject;
    private int deadlocksDetected;
    private Integer recentPortionWeight;
    private String recentMessage;
    private List<BucketSnapshot> bufferBuckets = new ArrayList<>();
    private List<BucketSnapshot> weighingBuckets = new ArrayList<>();
    private List<BucketSnapshot> memoryBuckets = new ArrayList<>();

    public RuntimeSnapshot() {
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public int getUnitsPacked() {
        return unitsPacked;
    }

    public void setUnitsPacked(int unitsPacked) {
        this.unitsPacked = unitsPacked;
    }

    public int getMaxUnits() {
        return maxUnits;
    }

    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
    }

    public int getMaxIterationsForReject() {
        return maxIterationsForReject;
    }

    public void setMaxIterationsForReject(int maxIterationsForReject) {
        this.maxIterationsForReject = maxIterationsForReject;
    }

    public int getDeadlocksDetected() {
        return deadlocksDetected;
    }

    public void setDeadlocksDetected(int deadlocksDetected) {
        this.deadlocksDetected = deadlocksDetected;
    }

    public Integer getRecentPortionWeight() {
        return recentPortionWeight;
    }

    public void setRecentPortionWeight(Integer recentPortionWeight) {
        this.recentPortionWeight = recentPortionWeight;
    }

    public String getRecentMessage() {
        return recentMessage;
    }

    public void setRecentMessage(String recentMessage) {
        this.recentMessage = recentMessage;
    }

    public List<BucketSnapshot> getBufferBuckets() {
        return bufferBuckets;
    }

    public void setBufferBuckets(List<BucketSnapshot> bufferBuckets) {
        this.bufferBuckets = bufferBuckets;
    }

    public List<BucketSnapshot> getWeighingBuckets() {
        return weighingBuckets;
    }

    public void setWeighingBuckets(List<BucketSnapshot> weighingBuckets) {
        this.weighingBuckets = weighingBuckets;
    }

    public List<BucketSnapshot> getMemoryBuckets() {
        return memoryBuckets;
    }

    public void setMemoryBuckets(List<BucketSnapshot> memoryBuckets) {
        this.memoryBuckets = memoryBuckets;
    }

    public static class BucketSnapshot {
        private int bucketNr;
        private boolean empty;
        private Integer weight;
        private int iterations;

        public BucketSnapshot() {
        }

        public BucketSnapshot(int bucketNr, boolean empty, Integer weight, int iterations) {
            this.bucketNr = bucketNr;
            this.empty = empty;
            this.weight = weight;
            this.iterations = iterations;
        }

        public int getBucketNr() {
            return bucketNr;
        }

        public void setBucketNr(int bucketNr) {
            this.bucketNr = bucketNr;
        }

        public boolean isEmpty() {
            return empty;
        }

        public void setEmpty(boolean empty) {
            this.empty = empty;
        }

        public Integer getWeight() {
            return weight;
        }

        public void setWeight(Integer weight) {
            this.weight = weight;
        }

        public int getIterations() {
            return iterations;
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }
    }
}
