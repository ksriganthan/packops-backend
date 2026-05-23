package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.Category;
import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.dto.ProductConfigurationCreateDto;
import ch.packops.packopsbackend.dto.ProductConfigurationDto;
import ch.packops.packopsbackend.dto.ProductConfigurationUpdateDto;
import ch.packops.packopsbackend.repository.CategoryRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * @author Kapischan Sriganthan
 */

/**
 * Unit Tests für ProcessService
 * Deckt zentrale Service-Logik ab:
 * - Produkt erstellen
 * - Partial Update
 * - Aktivieren / Deaktivieren
 * - Delegation an ValidationService
 * - Logging nach Änderungen
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

    @InjectMocks
    private ProductConfigurationService productConfigurationService;

    /**
     * Prüft, ob createProductConfiguration() die Werte aus dem CreateDto
     * korrekt in ein ProductConfiguration-Entity überträgt und speichert.
     */
    @Test
    void createProductConfiguration_savesCorrectValues() {
        ProductConfigurationCreateDto dto = new ProductConfigurationCreateDto();
        dto.setProductName("Kaffeebohnen");
        dto.setDescription("Premium Arabica");
        dto.setTargetWeight(250);
        dto.setTolerance(5);
        dto.setIcon("coffee");
        dto.setColor("brown");

        when(productConfigurationRepository.save(any(ProductConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductConfigurationDto result =
                productConfigurationService.createProductConfiguration(dto);

        assertEquals("Kaffeebohnen", result.getName());
        assertEquals("Premium Arabica", result.getDescription());
        assertEquals(250, result.getDefaultTargetWeight());
        assertEquals(5, result.getDefaultTolerance());
        assertEquals("coffee", result.getIcon());
        assertEquals("brown", result.getColor());

        verify(validationService, times(1)).validateProduct(dto);
        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    /**
     * Prüft mit ArgumentCaptor, ob das Entity, das an save() übergeben wird,
     * exakt die Werte aus dem CreateDto enthält.
     */
    @Test
    void createProductConfiguration_passesCorrectEntityToRepository() {
        ProductConfigurationCreateDto dto = new ProductConfigurationCreateDto();
        dto.setProductName("Äpfel");
        dto.setDescription("Rote Äpfel");
        dto.setTargetWeight(300);
        dto.setTolerance(10);
        dto.setIcon("apple");
        dto.setColor("red");

        when(productConfigurationRepository.save(any(ProductConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productConfigurationService.createProductConfiguration(dto);

        ArgumentCaptor<ProductConfiguration> captor =
                ArgumentCaptor.forClass(ProductConfiguration.class);

        verify(productConfigurationRepository).save(captor.capture());

        ProductConfiguration saved = captor.getValue();

        assertEquals("Äpfel", saved.getName());
        assertEquals("Rote Äpfel", saved.getDescription());
        assertEquals(300, saved.getDefaultTargetWeight());
        assertEquals(10, saved.getDefaultTolerance());
        assertEquals("apple", saved.getIcon());
        assertEquals("red", saved.getColor());
    }

    /**
     * Prüft Partial Update:
     * Nur Felder, die im UpdateDto nicht null sind, werden geändert.
     * Nicht mitgeschickte Felder bleiben unverändert.
     */
    @Test
    void updateProductConfiguration_partialUpdate_updatesOnlyProvidedFields() {
        ProductConfiguration existing = new ProductConfiguration();
        existing.setName("Kaffeebohnen");
        existing.setDescription("Alte Beschreibung");
        existing.setDefaultTargetWeight(250);
        existing.setDefaultTolerance(5);
        existing.setIcon("old-icon");
        existing.setColor("brown");
        existing.setActive(true);

        ProductConfigurationUpdateDto dto = new ProductConfigurationUpdateDto();
        dto.setTolerance(10);
        dto.setDescription("Neue Beschreibung");

        when(productConfigurationRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(productConfigurationRepository.save(any(ProductConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductConfigurationDto result =
                productConfigurationService.updateProductConfiguration(1L, dto);

        assertEquals("Kaffeebohnen", result.getName());
        assertEquals("Neue Beschreibung", result.getDescription());
        assertEquals(250, result.getDefaultTargetWeight());
        assertEquals(10, result.getDefaultTolerance());
        assertEquals("old-icon", result.getIcon());
        assertEquals("brown", result.getColor());

        verify(validationService, times(1)).validateProductUpdate(dto);
        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    /**
     * Prüft, ob bei einer nicht existierenden Produkt-ID eine RuntimeException geworfen wird.
     */
    @Test
    void updateProductConfiguration_productNotFound_throwsException() {
        ProductConfigurationUpdateDto dto = new ProductConfigurationUpdateDto();
        dto.setProductName("Neuer Name");

        when(productConfigurationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> productConfigurationService.updateProductConfiguration(999L, dto));

        verify(productConfigurationRepository, never()).save(any());
    }

    /**
     * Prüft, ob ein aktives Produkt deaktiviert wird.
     */
    @Test
    void activateOrDeactivateProductConfiguration_activeProduct_becomesInactive() {
        ProductConfiguration existing = new ProductConfiguration();
        existing.setName("Kaffeebohnen");
        existing.setActive(true);

        when(productConfigurationRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        productConfigurationService.activateOrDeactivateProductConfiguration(1L);

        assertFalse(existing.getActive());

        verify(productConfigurationRepository, times(1)).save(existing);
        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    /**
     * Prüft, ob ein inaktives Produkt wieder aktiviert wird.
     */
    @Test
    void activateOrDeactivateProductConfiguration_inactiveProduct_becomesActive() {
        ProductConfiguration existing = new ProductConfiguration();
        existing.setName("Kaffeebohnen");
        existing.setActive(false);

        when(productConfigurationRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        productConfigurationService.activateOrDeactivateProductConfiguration(1L);

        assertTrue(existing.getActive());

        verify(productConfigurationRepository, times(1)).save(existing);
        verify(loggingService, times(1)).logInfo(anyString(), isNull());
    }

    /**
     * Prüft, ob bei einer neuen categoryName eine neue Kategorie erstellt wird,
     * falls diese noch nicht existiert.
     */
    @Test
    void createProductConfiguration_withNewCategory_createsCategory() {
        ProductConfigurationCreateDto dto = new ProductConfigurationCreateDto();
        dto.setProductName("Bananen");
        dto.setTargetWeight(200);
        dto.setTolerance(5);
        dto.setCategoryName("Früchte");

        when(categoryRepository.findByNameIgnoreCase("Früchte"))
                .thenReturn(Optional.empty());

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(productConfigurationRepository.save(any(ProductConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductConfigurationDto result =
                productConfigurationService.createProductConfiguration(dto);

        assertEquals("Bananen", result.getName());
        assertEquals("Früchte", result.getCategoryName());

        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(productConfigurationRepository, times(1)).save(any(ProductConfiguration.class));
    }
}