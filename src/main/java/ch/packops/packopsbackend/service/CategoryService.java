package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.Category;
import ch.packops.packopsbackend.domain.CategoryTranslation;
import ch.packops.packopsbackend.dto.CategoryCreationDto;
import ch.packops.packopsbackend.dto.CategoryDto;
import ch.packops.packopsbackend.dto.CategoryTranslationDto;
import ch.packops.packopsbackend.repository.CategoryRepository;
import ch.packops.packopsbackend.repository.CategoryTranslationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Teodor Glisic
 */

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryTranslationRepository categoryTranslationRepository;

    public CategoryService(CategoryRepository categoryRepository, CategoryTranslationRepository categoryTranslationRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryTranslationRepository = categoryTranslationRepository;
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setCreatedAt(category.getCreatedAt());

        System.out.println("Category Translations: " + category.getTranslations());

        if (category.getTranslations() != null) {
            List<CategoryTranslationDto> translationDtos = category.getTranslations().stream()
                    .map(t -> {
                        CategoryTranslationDto tDto = new CategoryTranslationDto();
                        tDto.setLanguageCode(t.getLanguageCode());
                        tDto.setName(t.getCategoryName()); // Nutzt dein getCategoryName() aus Entity
                        return tDto;
                    })
                    .collect(Collectors.toList());
            dto.setTranslations(translationDtos);
        }

        return dto;
    }

    public List<CategoryDto> getCategoryNamesByLanguage(String language) {
        String langCode = language.toLowerCase();

        return categoryRepository.findAll().stream()
                .map(category -> {
                    CategoryDto dto = new CategoryDto();
                    dto.setId(category.getId());

                    // Suche die Übersetzung für die gewünschte Sprache
                    List<CategoryTranslationDto> filteredTranslations = category.getTranslations().stream()
                            .filter(t -> t.getLanguageCode().equals(langCode))
                            .map(t -> {
                                CategoryTranslationDto tDto = new CategoryTranslationDto();
                                tDto.setLanguageCode(t.getLanguageCode());
                                tDto.setName(t.getCategoryName());
                                return tDto;
                            })
                            .collect(Collectors.toList());

                    // Falls für die Sprache keine Übersetzung existiert, als Fallback Deutsch nehmen
                    if (filteredTranslations.isEmpty() && !langCode.equals("de")) {
                        filteredTranslations = category.getTranslations().stream()
                                .filter(t -> t.getLanguageCode().equals("de"))
                                .map(t -> {
                                    CategoryTranslationDto tDto = new CategoryTranslationDto();
                                    tDto.setLanguageCode(t.getLanguageCode());
                                    tDto.setName(t.getCategoryName());
                                    return tDto;
                                })
                                .collect(Collectors.toList());
                    }

                    dto.setTranslations(filteredTranslations);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public CategoryDto createNewCategory(CategoryCreationDto categoryCreationDto) {
        Category newCategory = new Category();
        newCategory.setTranslations(new ArrayList<>()); // Liste initialisieren
        Category savedCategory = categoryRepository.save(newCategory);

        List<CategoryTranslation> savedTranslations = new ArrayList<>();

        categoryCreationDto.getTranslations().forEach(categoryTranslationDto -> {
            CategoryTranslation newTranslation = new CategoryTranslation();
            newTranslation.setCategoryTranslation(savedCategory);
            newTranslation.setCategoryName(categoryTranslationDto.getName());
            newTranslation.setLanguageCode(categoryTranslationDto.getLanguageCode());

            CategoryTranslation savedTranslation = categoryTranslationRepository.save(newTranslation);
            savedTranslations.add(savedTranslation);
        });


        savedCategory.setTranslations(savedTranslations);

        return toDto(savedCategory);
    }

}
