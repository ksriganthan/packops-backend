package ch.packops.packopsbackend.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processes")
public class Process {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer targetWeight;

    private Integer tolerance;

    private Integer maxUnits;

    private Integer maxIterationsForReject;

    private Integer unitsPacked;

    private String status;

    private LocalDateTime startTimestamp;

    private LocalDateTime endTimestamp;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Process() {
    }

    public Long getId() {
        return id;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}