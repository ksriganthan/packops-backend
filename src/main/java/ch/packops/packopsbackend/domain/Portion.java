package ch.packops.packopsbackend.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "portions")
public class Portion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer measuredWeight;

    private Integer bucketNr;

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private PackageUnit packageUnit;

    public Portion() {
    }

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public PackageUnit getPackageUnit() {
        return packageUnit;
    }

    public void setPackageUnit(PackageUnit packageUnit) {
        this.packageUnit = packageUnit;
    }
}