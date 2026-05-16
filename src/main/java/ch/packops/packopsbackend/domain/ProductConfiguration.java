package ch.packops.packopsbackend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "product_configuration")
public class ProductConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Integer defaultTargetWeight;
    private Integer defaultTolerance;
    private Integer packageUnits;
    private String icon;
    private String color;
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDefaultTargetWeight() {
        return defaultTargetWeight;
    }

    public void setDefaultTargetWeight(Integer defaultTargetWeight) {
        this.defaultTargetWeight = defaultTargetWeight;
    }

    public Integer getDefaultTolerance() {
        return defaultTolerance;
    }

    public void setDefaultTolerance(Integer defaultTolerance) {
        this.defaultTolerance = defaultTolerance;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getPackageUnits() {
        return packageUnits;
    }

    public void setPackageUnits(Integer packageUnits) {
        this.packageUnits = packageUnits;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
