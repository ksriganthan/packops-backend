package ch.packops.packopsbackend.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "portion")
public class Portion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer measuredWeight;
    private Integer bucketNr;
    private String bucketType;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "package_unit_id")
    private PackageUnit packageUnit;

    public Long getId() {
        return id;
    }

    public Integer getMeasuredWeight() {
        return measuredWeight;
    }

    public void setMeasuredWeight(Integer measuredWeight) {
        this.measuredWeight = measuredWeight;
    }

    public Integer getBucketNr() {
        return bucketNr;
    }

    public void setBucketNr(Integer bucketNr) {
        this.bucketNr = bucketNr;
    }

    public String getBucketType() {
        return bucketType;
    }

    public void setBucketType(String bucketType) {
        this.bucketType = bucketType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PackageUnit getPackageUnit() {
        return packageUnit;
    }

    public void setPackageUnit(PackageUnit packageUnit) {
        this.packageUnit = packageUnit;
    }
}
