package ch.packops.packopsbackend.dto;

import ch.packops.packopsbackend.domain.ProductConfigurationTranslation;

import java.util.ArrayList;
import java.util.List;

public class ProductConfigurationCreateDto {

    private String productName;
    private Long categoryId;
    private String categoryName;
    private Integer targetWeight;
    private Integer tolerance;
    private String description;
    private String icon;
    private String color;
    private ArrayList<ProductConfigurationTranslation> translations;

    public ProductConfigurationCreateDto() {
    }

    public List<ProductConfigurationTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(ArrayList<ProductConfigurationTranslation> translations) {
        this.translations = translations;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}