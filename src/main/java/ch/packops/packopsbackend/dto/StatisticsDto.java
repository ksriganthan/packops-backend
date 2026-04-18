package ch.packops.packopsbackend.dto;

public class StatisticsDto {

    private Integer totalProcesses;
    private Integer totalPackages;
    private Double averageWeight;
    private Integer deadlocksDetected;

    public StatisticsDto() {
    }

    public Integer getTotalProcesses() {
        return totalProcesses;
    }

    public void setTotalProcesses(Integer totalProcesses) {
        this.totalProcesses = totalProcesses;
    }

    public Integer getTotalPackages() {
        return totalPackages;
    }

    public void setTotalPackages(Integer totalPackages) {
        this.totalPackages = totalPackages;
    }

    public Double getAverageWeight() {
        return averageWeight;
    }

    public void setAverageWeight(Double averageWeight) {
        this.averageWeight = averageWeight;
    }

    public Integer getDeadlocksDetected() {
        return deadlocksDetected;
    }

    public void setDeadlocksDetected(Integer deadlocksDetected) {
        this.deadlocksDetected = deadlocksDetected;
    }
}