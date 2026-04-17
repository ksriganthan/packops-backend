package ch.packops.packopsbackend.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "process")
public class Process {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private String status;
    private Integer targetWeight;
    private Integer tolerance;
    private Integer maxIterationsForReject;
    private Integer packageUnits;
    private Integer deadlocksDetected;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_configuration_id")
    private ProductConfiguration productConfiguration;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL)
    private List<PackageUnit> packages;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL)
    private List<AuditLog> logs;

    public Long getId() {
        return id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Integer getPackageUnits() {
        return packageUnits;
    }

    public void setPackageUnits(Integer packageUnits) {
        this.packageUnits = packageUnits;
    }

    public Integer getDeadlocksDetected() {
        return deadlocksDetected;
    }

    public void setDeadlocksDetected(Integer deadlocksDetected) {
        this.deadlocksDetected = deadlocksDetected;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProductConfiguration getProductConfiguration() {
        return productConfiguration;
    }

    public void setProductConfiguration(ProductConfiguration productConfiguration) {
        this.productConfiguration = productConfiguration;
    }

    public List<PackageUnit> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageUnit> packages) {
        this.packages = packages;
    }

    public List<AuditLog> getLogs() {
        return logs;
    }

    public void setLogs(List<AuditLog> logs) {
        this.logs = logs;
    }
}
