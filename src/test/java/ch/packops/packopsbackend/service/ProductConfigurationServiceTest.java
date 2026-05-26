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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * @author Kapischan Sriganthan
 * Unit Tests für ProductConfigurationService
 */
@ExtendWith(MockitoExtension.class) // JUnit soll Mockito in diesem Test aktivieren
public class ProductConfigurationServiceTest {

    @Mock
    private ProductConfigurationRepository productConfigurationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private LoggingService loggingService;

    @Mock
    private ProductConfigurationTranslationRepository translationRepository;

    @InjectMocks
    private ProductConfigurationService productConfigurationService;

    @Test
    void createProductConfiguration_savesCorrectValues() {
        ProductConfigurationCreateDto dto = new ProductConfigurationCreateDto();
        dto.setTargetWeight(250);
        dto.setTolerance(5);
        dto.setIcon("coffee");
        dto.setColor("brown");

        ArrayList<ProductConfigurationTranslation> translations = new ArrayList<>();
        ProductConfigurationTranslation translation = new ProductConfigurationTranslation();
        translation.setLanguageCode("de");
        translation.setName("Kaffeebohnen");
        translation.setDescription("Premium Arabica");
        translations.add(translation);
        dto.setTranslations(translations);

        when(productConfigurationRepository.save(any(ProductConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(translationRepository.findAllByProductConfiguration(any()))
                .thenReturn(translations);

        ProductConfigurationDto result =
                productConfigurationService.createProductConfiguration(dto);

        assertEquals(250, result.getDefaultTargetWeight());
        assertEquals(5, result.getDefaultTolerance());
        assertEquals("coffee", result.getIcon());
        assertEquals("brown", result.getColor());
        assertNotNull(result.getTranslations());
        assertEquals(1, result.getTranslations().size());

        verify(validationService, times(1)).validateProduct(dto);
        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    @Test
    void createProductConfiguration_passesCorrectEntityToRepository() {
        ProductConfigurationCreateDto dto = new ProductConfigurationCreateDto();
        dto.setTargetWeight(300);
        dto.setTolerance(10);
        dto.setIcon("apple");
        dto.setColor("red");

        when(productConfigurationRepository.save(any(ProductConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(translationRepository.findAllByProductConfiguration(any()))
                .thenReturn(new ArrayList<>());

        productConfigurationService.createProductConfiguration(dto);

        ArgumentCaptor<ProductConfiguration> captor =
                ArgumentCaptor.forClass(ProductConfiguration.class);

        verify(productConfigurationRepository).save(captor.capture());

        ProductConfiguration saved = captor.getValue();

        assertEquals(300, saved.getDefaultTargetWeight());
        assertEquals(10, saved.getDefaultTolerance());
        assertEquals("apple", saved.getIcon());
        assertEquals("red", saved.getColor());
    }

    @Test
    void updateProductConfiguration_partialUpdate_updatesOnlyProvidedFields() {
        ProductConfiguration existing = new ProductConfiguration();
        existing.setDefaultTargetWeight(250);
        existing.setDefaultTolerance(5);
        existing.setIcon("old-icon");
        existing.setColor("brown");
        existing.setActive(true);

        ProductConfigurationUpdateDto dto = new ProductConfigurationUpdateDto();
        dto.setTolerance(10);

        when(productConfigurationRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(productConfigurationRepository.save(any(ProductConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(translationRepository.findAllByProductConfiguration(any()))
                .thenReturn(new ArrayList<>());

        ProductConfigurationDto result =
                productConfigurationService.updateProductConfiguration(1L, dto);

        assertEquals(250, result.getDefaultTargetWeight());
        assertEquals(10, result.getDefaultTolerance());
        assertEquals("old-icon", result.getIcon());
        assertEquals("brown", result.getColor());

        verify(validationService, times(1)).validateProductUpdate(dto);
        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    @Test
    void updateProductConfiguration_productNotFound_throwsException() {
        ProductConfigurationUpdateDto dto = new ProductConfigurationUpdateDto();
        dto.setIcon("new-icon");

        when(productConfigurationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> productConfigurationService.updateProductConfiguration(999L, dto));

        verify(productConfigurationRepository, never()).save(any());
    }

    @Test
    void activateOrDeactivateProductConfiguration_activeProduct_becomesInactive() {
        ProductConfiguration existing = new ProductConfiguration();
        existing.setActive(true);

        when(productConfigurationRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        productConfigurationService.activateOrDeactivateProductConfiguration(1L);

        assertFalse(existing.getActive());

        verify(productConfigurationRepository, times(1)).save(existing);
        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    @Test
    void activateOrDeactivateProductConfiguration_inactiveProduct_becomesActive() {
        ProductConfiguration existing = new ProductConfiguration();
        existing.setActive(false);

        when(productConfigurationRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        productConfigurationService.activateOrDeactivateProductConfiguration(1L);

        assertTrue(existing.getActive());

        verify(productConfigurationRepository, times(1)).save(existing);
        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    @Test
    void createProductConfiguration_withCategoryName_doesNotCreateCategory() {
        ProductConfigurationCreateDto dto = new ProductConfigurationCreateDto();
        dto.setTargetWeight(200);
        dto.setTolerance(5);
        dto.setCategoryName("Früchte");

        when(productConfigurationRepository.save(any(ProductConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(translationRepository.findAllByProductConfiguration(any()))
                .thenReturn(new ArrayList<>());

        ProductConfigurationDto result =
                productConfigurationService.createProductConfiguration(dto);

        assertEquals(200, result.getDefaultTargetWeight());
        assertEquals(5, result.getDefaultTolerance());

        // Auto-Create ist auskommentiert, daher wird keine Category erstellt
        verify(categoryRepository, never()).save(any(Category.class));
        verify(productConfigurationRepository, times(1)).save(any(ProductConfiguration.class));
    }
}