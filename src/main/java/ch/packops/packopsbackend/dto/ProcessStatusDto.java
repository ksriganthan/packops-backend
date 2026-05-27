package ch.packops.packopsbackend.dto;

import ch.packops.packopsbackend.simulation.RuntimeSnapshot;

import java.util.List;
import java.util.Map;

public class ProcessStatusDto {

    private Long processId;
    private String status;
    private Integer recentPortionWeight;
    private Integer unitsPacked;
    private Integer maxUnits;
    private Integer maxIterationsForReject;
    private Integer deadlocksDetected;
    private Map<String, String > recentMessage;

    private List<RuntimeSnapshot.BucketSnapshot> bufferBuckets;
    private List<RuntimeSnapshot.BucketSnapshot> weighingBuckets;
    private List<RuntimeSnapshot.BucketSnapshot> memoryBuckets;

    private List<Integer> recentSelectedBuckets;

    public ProcessStatusDto() {
    }

    public List<Integer> getRecentSelectedBuckets() {
        return recentSelectedBuckets;
    }

    public void setRecentSelectedBuckets(List<Integer> recentSelectedBuckets) {
        this.recentSelectedBuckets = recentSelectedBuckets;
    }

    public List<RuntimeSnapshot.BucketSnapshot> getBufferBuckets() {
        return bufferBuckets;
    }

    public void setBufferBuckets(List<RuntimeSnapshot.BucketSnapshot> bufferBuckets) {
        this.bufferBuckets = bufferBuckets;
    }

    public List<RuntimeSnapshot.BucketSnapshot> getWeighingBuckets() {
        return weighingBuckets;
    }

    public void setWeighingBuckets(List<RuntimeSnapshot.BucketSnapshot> weighingBuckets) {
        this.weighingBuckets = weighingBuckets;
    }

    public List<RuntimeSnapshot.BucketSnapshot> getMemoryBuckets() {
        return memoryBuckets;
    }

    public void setMemoryBuckets(List<RuntimeSnapshot.BucketSnapshot> memoryBuckets) {
        this.memoryBuckets = memoryBuckets;
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

    public Integer getRecentPortionWeight() {
        return recentPortionWeight;
    }

    public void setRecentPortionWeight(Integer recentPortionWeight) {
        this.recentPortionWeight = recentPortionWeight;
    }

    public Integer getUnitsPacked() {
        return unitsPacked;
    }

    public void setUnitsPacked(Integer unitsPacked) {
        this.unitsPacked = unitsPacked;
    }

    public Integer getMaxUnits() {
        return maxUnits;
    }

    public void setMaxUnits(Integer maxUnits) {
        this.maxUnits = maxUnits;
    }

    public Integer getMaxIterationsForReject() {
        return maxIterationsForReject;
    }

    public void setMaxIterationsForReject(Integer maxIterationsForReject) {
        this.maxIterationsForReject = maxIterationsForReject;
    }

    public Integer getDeadlocksDetected() {
        return deadlocksDetected;
    }

    public void setDeadlocksDetected(Integer deadlocksDetected) {
        this.deadlocksDetected = deadlocksDetected;
    }

    public Map<String, String> getRecentMessage() {
        return recentMessage;
    }

    public void setRecentMessage(Map<String, String> recentMessage) {
        this.recentMessage = recentMessage;
    }
}