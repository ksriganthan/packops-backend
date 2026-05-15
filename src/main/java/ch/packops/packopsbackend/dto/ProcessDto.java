package ch.packops.packopsbackend.dto;

import java.time.LocalDateTime;

public class ProcessDto {

    private Long processId;
    private Long userId;
    private Long productConfigurationId;
    private Long configurationId;
    private String status;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private Integer targetWeight;
    private Integer tolerance;
    private Integer maxUnits;
    private Integer maxIterationsForReject;
    private Integer unitsPacked;

    public ProcessDto() {
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(LocalDateTime startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public LocalDateTime getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(LocalDateTime endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public Integer getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(Integer targetWeight) {
        this.targetWeight = targetWeight;
    }

    public Integer getTolerance() {
        return tolerance;
    }

    public void setTolerance(Integer tolerance) {
        this.tolerance = tolerance;
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

    public Integer getUnitsPacked() {
        return unitsPacked;
    }

    public void setUnitsPacked(Integer unitsPacked) {
        this.unitsPacked = unitsPacked;
    }

    public Long getProductConfigurationId() {
        return productConfigurationId;
    }

    public void setProductConfigurationId(Long productConfigurationId) {
        this.productConfigurationId = productConfigurationId;
    }

    public Long getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Long configurationId) {
        this.configurationId = configurationId;
    }
}