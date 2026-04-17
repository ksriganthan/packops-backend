package ch.packops.packopsbackend.domain;

import jakarta.persistence.*;

import java.util.List;
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<ProductConfiguration> productConfigurations;

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

    public List<ProductConfiguration> getProductConfigurations() {
        return productConfigurations;
    }
    public void setProductConfigurations(List<ProductConfiguration> productConfigurations) {
        this.productConfigurations = productConfigurations;
    }
}