package ch.packops.packopsbackend.dto;

import java.time.LocalDateTime;

public class PackageUnitDto {
    private Long id;
    private Integer measuredWeight;
    private Integer deviation;
    private Boolean withinTolerance;
    private LocalDateTime createdAt;

    public PackageUnitDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMeasuredWeight() {
        return measuredWeight;
    }

    public void setMeasuredWeight(Integer measuredWeight) {
        this.measuredWeight = measuredWeight;
    }

    public Integer getDeviation() {
        return deviation;
    }

    public void setDeviation(Integer deviation) {
        this.deviation = deviation;
    }

    public Boolean getWithinTolerance() {
        return withinTolerance;
    }

    public void setWithinTolerance(Boolean withinTolerance) {
        this.withinTolerance = withinTolerance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}