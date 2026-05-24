package ch.packops.packopsbackend.dto;

import ch.packops.packopsbackend.domain.CategoryTranslation;

/**
 * @author Teodor Glisic
 */

import java.util.List;

public class CategoryCreationDto {

    private List<CategoryTranslationDto> translations;

    public List<CategoryTranslationDto> getTranslations() {
        return translations;
    }

    public void setTranslations(List<CategoryTranslationDto> translations) {
        this.translations = translations;
    }
}
