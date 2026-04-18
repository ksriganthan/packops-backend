package ch.packops.packopsbackend.dto;

public class ProcessStatusDto {

    private Long processId;
    private String status;
    private Integer recentPortionWeight;
    private Integer unitsPacked;
    private Integer maxUnits;
    private Integer maxIterationsForReject;
    private Integer deadlocksDetected;
    private String recentMessage;

    public ProcessStatusDto() {
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

    public String getRecentMessage() {
        return recentMessage;
    }

    public void setRecentMessage(String recentMessage) {
        this.recentMessage = recentMessage;
    }
}