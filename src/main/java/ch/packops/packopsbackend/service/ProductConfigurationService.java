package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.dto.ProductConfigurationDto;
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

    public ProductConfigurationService(
            ProductConfigurationRepository productConfigurationRepository,
            CategoryRepository categoryRepository) {
        this.productConfigurationRepository = productConfigurationRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductConfigurationDto> getProductConfigurations(String categoryName) {
        if (categoryName != null && !categoryName.isEmpty()) {
            return productConfigurationRepository.findByCategoryName(categoryName)
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
        return productConfigurationRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
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

    // DTO → Domain
    private ProductConfiguration toDomain(ProductConfigurationDto dto) {
        ProductConfiguration product = new ProductConfiguration();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setDefaultTargetWeight(dto.getDefaultTargetWeight());
        product.setDefaultTolerance(dto.getDefaultTolerance());
        product.setPackageUnits(dto.getPackageUnits());
        product.setIcon(dto.getIcon());
        product.setColor(dto.getColor());
        product.setActive(dto.getActive());
        if (dto.getCategoryName() != null) {
            categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(dto.getCategoryName()))
                    .findFirst()
                    .ifPresent(product::setCategory);
        }
        return product;
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

    public ProductConfigurationDto createProductConfiguration(ProductConfigurationDto dto) {
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (dto.getDefaultTargetWeight() < 50 || dto.getDefaultTargetWeight() > 500) {
            throw new IllegalArgumentException("TargetWeight must be between 50 and 500");
        }
        if (dto.getDefaultTolerance() < 0) {
            throw new IllegalArgumentException("Tolerance must be positive");
        }
        return toDto(productConfigurationRepository.save(toDomain(dto)));
    }

    public ProductConfigurationDto updateProductConfiguration(Long id, ProductConfigurationDto dto) {
        ProductConfiguration existing = productConfigurationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        if (dto.getDefaultTargetWeight() < 50 || dto.getDefaultTargetWeight() > 500) {
            throw new IllegalArgumentException("TargetWeight must be between 50 and 500");
        }
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setDefaultTargetWeight(dto.getDefaultTargetWeight());
        existing.setDefaultTolerance(dto.getDefaultTolerance());
        existing.setPackageUnits(dto.getPackageUnits());
        existing.setIcon(dto.getIcon());
        existing.setColor(dto.getColor());
        existing.setActive(dto.getActive());
        // Ist nötig für das Mapping von dto zu Domain, da Category bei DTO nur ein String ist
        if (dto.getCategoryName() != null) {
            categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(dto.getCategoryName()))
                    .findFirst()
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
