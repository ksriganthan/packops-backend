package ch.packops.packopsbackend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "packages")
public class PackageUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer measuredWeight;

    private Integer deviation;

    private Boolean wasRefeed;

    @ManyToOne
    @JoinColumn(name = "process_id")
    private Process process;

    public PackageUnit() {
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

    public Integer getDeviation() {
        return deviation;
    }

    public void setDeviation(Integer deviation) {
        this.deviation = deviation;
    }

    public Boolean getWasRefeed() {
        return wasRefeed;
    }

    public void setWasRefeed(Boolean wasRefeed) {
        this.wasRefeed = wasRefeed;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }
}