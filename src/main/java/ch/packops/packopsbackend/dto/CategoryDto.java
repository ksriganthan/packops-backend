package ch.packops.packopsbackend.dto;

import ch.packops.packopsbackend.domain.CategoryTranslation;

/**
 * @author Teodor Glisic
 */

import java.time.LocalDate;
import java.util.List;

public class CategoryDto {

    private Long id;
    private LocalDate createdAt;
    private List<CategoryTranslationDto> translations;

    public Long getId() {
        return id;
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

    public List<CategoryTranslationDto> getTranslations() {
        return translations;
    }

    public void setTranslations(List<CategoryTranslationDto> translations) {
        this.translations = translations;
    }
}
