package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.dto.ProductConfigurationCreateDto;
import ch.packops.packopsbackend.dto.ProductConfigurationDto;
import ch.packops.packopsbackend.dto.ProductConfigurationUpdateDto;
import ch.packops.packopsbackend.repository.CategoryRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
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

    public ProductConfigurationService(
            ProductConfigurationRepository productConfigurationRepository,
            CategoryRepository categoryRepository,
            ValidationService validationService) {
        this.productConfigurationRepository = productConfigurationRepository;
        this.categoryRepository = categoryRepository;
        this.validationService = validationService;
    }
        // Domain → DTO
    private ProductConfigurationDto toDto(ProductConfiguration product) {
        ProductConfigurationDto dto = new ProductConfigurationDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setDefaultTargetWeight(product.getDefaultTargetWeight());
        dto.setDefaultTolerance(product.getDefaultTolerance());
        dto.setPackageUnits(product.getPackageUnits());
        dto.setIcon(product.getIcon());
        dto.setColor(product.getColor());
        dto.setActive(product.getActive());
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }

    public List<ProductConfigurationDto> getProductConfigurations(String categoryName) {
        if (categoryName != null && !categoryName.isEmpty()) {
            return productConfigurationRepository.findByCategoryName(categoryName)
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
        return productConfigurationRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ProductConfigurationDto> getProductConfigurationsByCategory(String categoryName) {
        if (categoryName != null && !categoryName.isEmpty()) {
            return productConfigurationRepository.findByCategoryName(categoryName)
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
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
        product.setName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setDefaultTargetWeight(dto.getTargetWeight());
        product.setDefaultTolerance(dto.getTolerance());
        product.setIcon(dto.getIcon());
        product.setColor(dto.getColor());

        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                    .ifPresent(product::setCategory);
        }
        return toDto(productConfigurationRepository.save(product));
    }

    public ProductConfigurationDto updateProductConfiguration(Long id, ProductConfigurationUpdateDto dto) {
        // Validierung über ValidationService
        validationService.validateProductUpdate(dto);

        ProductConfiguration existing = productConfigurationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        existing.setName(dto.getProductName());
        existing.setDescription(dto.getDescription());
        existing.setDefaultTargetWeight(dto.getTargetWeight());
        existing.setDefaultTolerance(dto.getTolerance());
        existing.setIcon(dto.getIcon());
        existing.setColor(dto.getColor());

        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                    .ifPresent(existing::setCategory);
        }
        return toDto(productConfigurationRepository.save(existing));
    }

    public void deleteProductConfiguration(Long id) {
        ProductConfiguration existing = productConfigurationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productConfigurationRepository.delete(existing);
    }
}
