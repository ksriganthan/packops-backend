package ch.packops.packopsbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


/**
 * @author Teodor Glisic
 */

@Entity
public class CategoryTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String languageCode;
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_translation_id")
    @JsonIgnore
    private Category categoryTranslation;

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

    public String getCategoryName() {
        return name;
    }

    public void setCategoryName(String name) {
        this.name = name;
    }

    public Category getCategoryTranslation() {
        return categoryTranslation;
    }

    public void setCategoryTranslation(Category categoryTranslation) {
        this.categoryTranslation = categoryTranslation;
    }
}
