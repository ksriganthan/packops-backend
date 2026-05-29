package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.Category;
import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.domain.ProductConfigurationTranslation;
import ch.packops.packopsbackend.dto.ProductConfigurationCreateDto;
import ch.packops.packopsbackend.dto.ProductConfigurationDto;
import ch.packops.packopsbackend.dto.ProductConfigurationUpdateDto;
import ch.packops.packopsbackend.repository.CategoryRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationTranslationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kapischan Sriganthan
 */
@Service
public class ProductConfigurationService {

    private final ProductConfigurationRepository productConfigurationRepository;
    private final CategoryRepository categoryRepository;
    private final ValidationService validationService;
    private final LoggingService loggingService;
    private final ProductConfigurationTranslationRepository translationRepository;

    public ProductConfigurationService(
            ProductConfigurationRepository productConfigurationRepository,
            CategoryRepository categoryRepository,
            ValidationService validationService,
            LoggingService loggingService,
            ProductConfigurationTranslationRepository translationRepository) {
        this.productConfigurationRepository = productConfigurationRepository;
        this.categoryRepository = categoryRepository;
        this.validationService = validationService;
        this.loggingService = loggingService;
        this.translationRepository = translationRepository;
    }
        // Domain → DTO
    private ProductConfigurationDto toDto(ProductConfiguration product) {
        ProductConfigurationDto dto = new ProductConfigurationDto();
        dto.setId(product.getId());
        dto.setDefaultTargetWeight(product.getDefaultTargetWeight());
        dto.setDefaultTolerance(product.getDefaultTolerance());
        dto.setIcon(product.getIcon());
        dto.setColor(product.getColor());
        dto.setActive(product.getActive());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }

        // Translations setzen
        ArrayList<ProductConfigurationTranslation> translations =
            translationRepository.findAllByProductConfiguration(product);
        dto.setTranslations(translations);

        // name und description aus erster Translation setzen
        if (translations != null && !translations.isEmpty()) {
            ProductConfigurationTranslation firstTranslation = translations.get(0);
            dto.setName(firstTranslation.getName());
            dto.setDescription(firstTranslation.getDescription());
        }

        return dto;
    }

    public List<ProductConfigurationDto> getProductConfigurations() {
        List<ProductConfiguration> products = productConfigurationRepository.findAll();

        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProductConfigurationDto createProductConfiguration(ProductConfigurationCreateDto dto) {
        // Validierung über ValidationService
        validationService.validateProduct(dto);

        ProductConfiguration product = new ProductConfiguration();
        product.setDefaultTargetWeight(dto.getTargetWeight());
        product.setDefaultTolerance(dto.getTolerance());
        product.setIcon(dto.getIcon());
        product.setColor(dto.getColor());
        ProductConfiguration saved = productConfigurationRepository.save(product);

        // Translations speichern
        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            // Falls Translations vorhanden sind, diese verwenden
            for (ProductConfigurationTranslation transDto : dto.getTranslations()) {
                ProductConfigurationTranslation translationEntity = new ProductConfigurationTranslation();
                translationEntity.setProductConfiguration(saved);
                translationEntity.setLanguageCode(transDto.getLanguageCode().toLowerCase());
                translationEntity.setName(transDto.getName());
                translationEntity.setDescription(transDto.getDescription());

                translationRepository.save(translationEntity);
            }
        } else if (dto.getProductName() != null) {
            // Automatisch Translation aus productName/description erstellen
            ProductConfigurationTranslation translation = new ProductConfigurationTranslation();
            translation.setProductConfiguration(saved);
            translation.setLanguageCode("de"); // Default-Sprache
            translation.setName(dto.getProductName());
            translation.setDescription(dto.getDescription());
            translationRepository.save(translation);
        }

        // Category-Handling über Helper-Methode
        handleCategoryAssignment(product, dto.getCategoryId(), dto.getCategoryName());

        loggingService.logInfo("Produkt erstellt: " + dto.getProductName(), null);
        return toDto(saved);
    }

    public ProductConfigurationDto updateProductConfiguration(Long id, ProductConfigurationUpdateDto dto) {
        // Validierung über ValidationService
        validationService.validateProductUpdate(dto);

        ProductConfiguration existing = productConfigurationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));


        if (dto.getTargetWeight() != null) {
            existing.setDefaultTargetWeight(dto.getTargetWeight());
        }
        if (dto.getTolerance() != null) {
            existing.setDefaultTolerance(dto.getTolerance());
        }
        if (dto.getIcon() != null) {
            existing.setIcon(dto.getIcon());
        }
        if (dto.getColor() != null) {
            existing.setColor(dto.getColor());
        }

        // Category-Handling über Helper-Methode (nur wenn CategoryId oder CategoryName angegeben)
        if (dto.getCategoryId() != null || dto.getCategoryName() != null) {
            handleCategoryAssignment(existing, dto.getCategoryId(), dto.getCategoryName());
        }

        ProductConfiguration updatedProduct = productConfigurationRepository.save(existing);

        // 2. Übersetzungen überschreiben
        if (dto.getTranslations() != null) {
            // Bestehende Übersetzungen aus der DB holen
            List<ProductConfigurationTranslation> currentTranslations =
                    translationRepository.findAllByProductConfiguration(updatedProduct);

            // Loop durch die vom Frontend geschickten Daten
            for (ProductConfigurationTranslation transDto : dto.getTranslations()) {
                for (ProductConfigurationTranslation existingTrans : currentTranslations) {
                    // Sicherer Inhaltsvergleich der Sprachcodes via .equals()
                    if (existingTrans.getLanguageCode().equals(transDto.getLanguageCode())) {
                        existingTrans.setName(transDto.getName());
                        existingTrans.setDescription(transDto.getDescription());
                        translationRepository.save(existingTrans);
                        break; // Sprache gefunden und aktualisiert, ab zur nächsten
                    }
                }
            }
        }

        loggingService.logInfo("Produkt aktualisiert: " + id, null);
        return toDto(productConfigurationRepository.save(existing));
    }

    public void activateOrDeactivateProductConfiguration(Long id) {
        ProductConfiguration existing = productConfigurationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        existing.setActive(!existing.getActive());
        productConfigurationRepository.save(existing);
        String text = existing.getActive() ? "aktiviert": "deaktiviert";
        loggingService.logInfo(String.format("Produkt mit ID %s: %s", id, text), null);
    }

    /**
     * Weist einem Produkt eine Category zu
     * - Wenn categoryId angegeben: Suche nach ID
     * - Wenn categoryName angegeben: Case-insensitive Suche nach Name, erstelle neue falls nicht vorhanden
     */
    private void handleCategoryAssignment(ProductConfiguration product, Long categoryId, String categoryName) {
        if (categoryId != null) {
            // Priorität 1: categoryId
            categoryRepository.findById(categoryId)
                    .ifPresent(product::setCategory);
        } else if (categoryName != null) {
        }
    }
}
