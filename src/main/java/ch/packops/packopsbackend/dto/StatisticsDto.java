package ch.packops.packopsbackend.dto;

import java.util.List;
import java.util.ArrayList;

public class StatisticsDto {

    private Integer totalProcesses;
    private Integer totalPackages;
    private Double averageWeight;
    private Integer deadlocksDetected;

    private Integer goodPackages;
    private Double yieldPercent;
    private Integer targetWeight;
    private Double averageGiveaway;
    private Integer packagesPerMinute;
    
    private List<DistributionItemDto> weightDistribution = new ArrayList<>();
    private List<ProcessOverviewDto> availableProcesses = new ArrayList<>();

    private List<ProductOverviewDto> availableProducts = new ArrayList<>();

    public StatisticsDto() {
    }

    public Integer getTotalProcesses() { return totalProcesses; }
    public void setTotalProcesses(Integer totalProcesses) { this.totalProcesses = totalProcesses; }

    public Integer getTotalPackages() { return totalPackages; }
    public void setTotalPackages(Integer totalPackages) { this.totalPackages = totalPackages; }

    public Double getAverageWeight() { return averageWeight; }
    public void setAverageWeight(Double averageWeight) { this.averageWeight = averageWeight; }

    public Integer getDeadlocksDetected() { return deadlocksDetected; }
    public void setDeadlocksDetected(Integer deadlocksDetected) { this.deadlocksDetected = deadlocksDetected; }

    public Integer getGoodPackages() { return goodPackages; }
    public void setGoodPackages(Integer goodPackages) { this.goodPackages = goodPackages; }

    public Double getYieldPercent() { return yieldPercent; }
    public void setYieldPercent(Double yieldPercent) { this.yieldPercent = yieldPercent; }

    public Integer getTargetWeight() { return targetWeight; }
    public void setTargetWeight(Integer targetWeight) { this.targetWeight = targetWeight; }

    public Double getAverageGiveaway() { return averageGiveaway; }
    public void setAverageGiveaway(Double averageGiveaway) { this.averageGiveaway = averageGiveaway; }

    public Integer getPackagesPerMinute() { return packagesPerMinute; }
    public void setPackagesPerMinute(Integer packagesPerMinute) { this.packagesPerMinute = packagesPerMinute; }

    public List<DistributionItemDto> getWeightDistribution() { return weightDistribution; }
    public void setWeightDistribution(List<DistributionItemDto> weightDistribution) { this.weightDistribution = weightDistribution; }

    public List<ProcessOverviewDto> getAvailableProcesses() { return availableProcesses; }
    public void setAvailableProcesses(List<ProcessOverviewDto> availableProcesses) { this.availableProcesses = availableProcesses; }

    public List<ProductOverviewDto> getAvailableProducts() { return availableProducts; }

    public void setAvailableProducts(List<ProductOverviewDto> availableProducts) { this.availableProducts = availableProducts; }
}