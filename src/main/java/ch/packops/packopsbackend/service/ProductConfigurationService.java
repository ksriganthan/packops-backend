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
        dto.setPackageUnits(product.getPackageUnits());
        dto.setIcon(product.getIcon());
        dto.setColor(product.getColor());
        dto.setActive(product.getActive());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }

        dto.setTranslations(translationRepository.findAllByProductConfiguration(product));

        return dto;
    }

    public List<ProductConfigurationDto> getProductConfigurations(String categoryName) {
        return productConfigurationRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProductConfigurationDto getProductConfiguration(Long id) {
        ProductConfiguration product = productConfigurationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return toDto(product);
    }

    public ProductConfigurationDto createProductConfiguration(ProductConfigurationCreateDto dto) {
        // Validierung über ValidationService
        validationService.validateProduct(dto);

        ProductConfiguration product = new ProductConfiguration();
        product.setDefaultTargetWeight(dto.getTargetWeight());
        product.setDefaultTolerance(dto.getTolerance());
        product.setIcon(dto.getIcon());
        product.setColor(dto.getColor());
        product.setPackageUnits(0);
        ProductConfiguration saved = productConfigurationRepository.save(product);
        if (dto.getTranslations() != null) {
            for (ProductConfigurationTranslation transDto : dto.getTranslations()) {
                ProductConfigurationTranslation translationEntity = new ProductConfigurationTranslation();
                translationEntity.setProductConfiguration(saved);
                translationEntity.setLanguageCode(transDto.getLanguageCode().toLowerCase());
                translationEntity.setName(transDto.getName());
                translationEntity.setDescription(transDto.getDescription());

                translationRepository.save(translationEntity);
            }
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
     * Helper-Methode: Weist einem Produkt eine Category zu
     * - Wenn categoryId angegeben: Suche nach ID
     * - Wenn categoryName angegeben: Case-insensitive Suche nach Name, erstelle neue falls nicht vorhanden
     * - Auto-Create ermöglicht flexible Category-Verwaltung (Groß-/Kleinschreibung wird ignoriert)
     */
    private void handleCategoryAssignment(ProductConfiguration product, Long categoryId, String categoryName) {
        if (categoryId != null) {
            // Priorität 1: categoryId
            categoryRepository.findById(categoryId)
                    .ifPresent(product::setCategory);
        } else if (categoryName != null) {
            // Priorität 2: categoryName (case-insensitive)
            // Damit es aufgrund Tippfehler nicht zu gefälschten Duplikaten kommt, wird die Suche case-insensitive durchgeführt
//            categoryRepository.findByNameIgnoreCase(categoryName)
//                    .ifPresentOrElse(
//                            product::setCategory,
//                            () -> {
//                                // Auto-Create neue Category
//                                Category newCat = new Category();
//                                newCat.setName(categoryName);
//                                Category savedCat = categoryRepository.save(newCat);
//                                product.setCategory(savedCat);
//                                loggingService.logInfo("Neue Category erstellt: " + categoryName, null);
//                            }
//                    );
        }
    }
}
