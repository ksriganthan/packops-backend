package ch.packops.packopsbackend.dto;

import ch.packops.packopsbackend.domain.ProductConfigurationTranslation;

import java.util.ArrayList;

public class ProductConfigurationDto {
    private Long id;
    private String name;
    private String description;
    private Integer defaultTargetWeight;
    private Integer defaultTolerance;
    private Integer packageUnits;
    private String icon;
    private String color;
    private Boolean active;
    private Long categoryId;
    private ArrayList<ProductConfigurationTranslation> translations;

    public ArrayList<ProductConfigurationTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(ArrayList<ProductConfigurationTranslation> translations) {
        this.translations = translations;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getPackageUnits() {
        return packageUnits;
    }

    public void setPackageUnits(Integer packageUnits) {
        this.packageUnits = packageUnits;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
