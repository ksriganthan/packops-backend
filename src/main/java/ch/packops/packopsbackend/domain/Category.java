package ch.packops.packopsbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate createdAt =LocalDate.now();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ProductConfiguration> productConfigurations;

    @OneToMany(mappedBy = "categoryTranslation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CategoryTranslation> translations;

    public Long getId() {
        return id;
    }

    public List<ProductConfiguration> getProductConfigurations() {
        return productConfigurations;
    }
    public void setProductConfigurations(List<ProductConfiguration> productConfigurations) {
        this.productConfigurations = productConfigurations;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public List<CategoryTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<CategoryTranslation> translations) {
        this.translations = translations;
    }
}