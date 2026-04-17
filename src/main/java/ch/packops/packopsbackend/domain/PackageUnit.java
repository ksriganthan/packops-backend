package ch.packops.packopsbackend.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "package_unit")
public class PackageUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer measuredWeight;
    private Integer deviation;
    private Boolean withinTolerance;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "process_id")
    private Process process;

    @OneToMany(mappedBy = "packageUnit", cascade = CascadeType.ALL)
    private List<Portion> portions;

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

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public List<Portion> getPortions() {
        return portions;
    }

    public void setPortions(List<Portion> portions) {
        this.portions = portions;
    }
}
