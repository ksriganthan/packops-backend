package ch.packops.packopsbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


/**
 * @author Teodor Glisic
 */


@Entity
@Table(name = "product_configuration_translation")
public class ProductConfigurationTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_config_id")
    @JsonIgnore
    private ProductConfiguration productConfiguration;
    private String languageCode;

    private String name;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
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

    public ProductConfiguration getProductConfiguration() {
        return productConfiguration;
    }

    public void setProductConfiguration(ProductConfiguration productConfiguration) {
        this.productConfiguration = productConfiguration;
    }
}
