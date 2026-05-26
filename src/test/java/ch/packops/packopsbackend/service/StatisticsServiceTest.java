package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.PackageUnit;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.domain.ProductConfigurationTranslation;
import ch.packops.packopsbackend.dto.StatisticsDto;
import ch.packops.packopsbackend.repository.PackageRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author David M.
 */
@ExtendWith(MockitoExtension.class)
public class StatisticsServiceTest {

    @Mock
    private ProcessRepository processRepository;
    @Mock
    private PackageRepository packageRepository;
    @Mock
    private ProductConfigurationRepository productRepository;
    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private StatisticsService statisticsService;

    private Process process1;
    private Process process2;
    private PackageUnit pkg1;
    private PackageUnit pkg2;
    private PackageUnit pkg3;
    private ProductConfiguration product1;

    @BeforeEach
    void setUp() {
        product1 = new ProductConfiguration();
        try {
            java.lang.reflect.Field idField = ProductConfiguration.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(product1, 1L);
        } catch (Exception e) {
            fail("ProductConfiguration-ID konnte nicht gesetzt werden");
        }
        
        ProductConfigurationTranslation pt = new ProductConfigurationTranslation();
        pt.setLanguageCode("de");
        pt.setName("Test Product");
        pt.setProductConfiguration(product1);
        product1.setTranslations(Collections.singletonList(pt));

        process1 = new Process();
        try {
            java.lang.reflect.Field idField = Process.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(process1, 10L);
        } catch (Exception e) {
            fail("Process-ID konnte nicht gesetzt werden");
        }
        process1.setTargetWeight(100);
        process1.setTolerance(2);
        process1.setProductConfiguration(product1);
        process1.setStartTimestamp(LocalDateTime.now().minusMinutes(5));
        process1.setEndTimestamp(LocalDateTime.now());
        process1.setDeadlocksDetected(2);

        process2 = new Process();
        try {
            java.lang.reflect.Field idField = Process.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(process2, 20L);
        } catch (Exception e) {
            fail("Process-ID konnte nicht gesetzt werden");
        }
        process2.setTargetWeight(500);
        process2.setTolerance(5);
        process2.setStartTimestamp(LocalDateTime.now().minusMinutes(10));
        process2.setEndTimestamp(LocalDateTime.now());
        process2.setDeadlocksDetected(1);

        pkg1 = new PackageUnit();
        pkg1.setMeasuredWeight(100);
        pkg1.setDeviation(0);
        pkg1.setProcess(process1);

        pkg2 = new PackageUnit();
        pkg2.setMeasuredWeight(102);
        pkg2.setDeviation(2);
        pkg2.setProcess(process1);

        pkg3 = new PackageUnit();
        pkg3.setMeasuredWeight(500);
        pkg3.setDeviation(0);
        pkg3.setProcess(process2);
    }

    @Test
    void testGetOverviewStatistics_Abweichungshistogramm() {
        when(processRepository.findAll()).thenReturn(Arrays.asList(process1, process2));
        when(packageRepository.findAll()).thenReturn(Arrays.asList(pkg1, pkg2, pkg3));
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product1));

        StatisticsDto result = statisticsService.getOverviewStatistics("de");

        verify(loggingService).logInfo(anyString(), isNull());
        assertNotNull(result);
        assertEquals(2, result.getTotalProcesses());
        assertEquals(3, result.getTotalPackages());
        assertEquals(3, result.getDeadlocksDetected());
        
        assertFalse(result.getWeightDistribution().isEmpty());
    }

    @Test
    void testGetProductStatistics() {
        when(processRepository.findByProductConfigurationId(1L)).thenReturn(Collections.singletonList(process1));
        when(packageRepository.findByProcessProductConfigurationId(1L)).thenReturn(Arrays.asList(pkg1, pkg2));
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product1));

        StatisticsDto result = statisticsService.getProductStatistics(1L, "de");

        verify(loggingService).logInfo(anyString(), isNull());
        assertEquals(1, result.getTotalProcesses());
        assertEquals(2, result.getTotalPackages());
        assertEquals(2, result.getDeadlocksDetected());
        assertEquals(101.0, result.getAverageWeight());
        assertEquals(1.0, result.getAverageGiveaway());
    }
}
