package ch.packops.packopsbackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ProcessDetailDto {
    private Long id;
    private Long userId;
    private Long productConfigurationId;
    private Long configurationId;
    private String status;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private Integer targetWeight;
    private Integer tolerance;
    private Integer maxIterationsForReject;
    private Integer maxUnits;
    private Integer unitsPacked;
    private List<PackageUnitDto> packages;

    public ProcessDetailDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getMaxIterationsForReject() {
        return maxIterationsForReject;
    }

    public void setMaxIterationsForReject(Integer maxIterationsForReject) {
        this.maxIterationsForReject = maxIterationsForReject;
    }

    public Integer getMaxUnits() {
        return maxUnits;
    }

    public void setMaxUnits(Integer maxUnits) {
        this.maxUnits = maxUnits;
    }

    public Integer getUnitsPacked() {
        return unitsPacked;
    }

    public void setUnitsPacked(Integer unitsPacked) {
        this.unitsPacked = unitsPacked;
    }

    public List<PackageUnitDto> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageUnitDto> packages) {
        this.packages = packages;
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
