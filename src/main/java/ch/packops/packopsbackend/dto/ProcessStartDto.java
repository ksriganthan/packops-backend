package ch.packops.packopsbackend.dto;

public class ProcessStartDto {

    private Long productConfigurationId;
    private Long configurationId;
    private Integer targetWeight;
    private Integer tolerance;
    private Integer maxUnits;
    private Integer maxIterationsForReject;

    public ProcessStartDto() {
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
}