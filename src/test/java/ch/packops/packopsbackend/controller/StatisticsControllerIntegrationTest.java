package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.PackageUnit;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.domain.ProductConfigurationTranslation;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.repository.AuditLogRepository;
import ch.packops.packopsbackend.repository.PackageRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import ch.packops.packopsbackend.repository.UserRepository;
import ch.packops.packopsbackend.repository.UserSessionRepository;
import ch.packops.packopsbackend.security.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author David M.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private ProductConfigurationRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private PasswordService passwordService;

    private Long productConfigId;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        packageRepository.deleteAll();
        processRepository.deleteAll();
        productRepository.deleteAll();
        userSessionRepository.deleteAll();
        userRepository.deleteAll();

        // Admin User for Auth
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@test.ch");
        user.setPasswordHash(passwordService.hash("admin123"));
        user.setRole("admin");
        user.setLanguage("de");
        userRepository.save(user);

        ProductConfiguration product = new ProductConfiguration();
        
        ProductConfigurationTranslation pt = new ProductConfigurationTranslation();
        pt.setLanguageCode("de");
        pt.setName("Test Product");
        pt.setProductConfiguration(product);
        product.setTranslations(java.util.Collections.singletonList(pt));
        
        product = productRepository.save(product);
        productConfigId = product.getId();

        Process process = new Process();
        process.setTargetWeight(100);
        process.setTolerance(2);
        process.setStartTimestamp(LocalDateTime.now().minusMinutes(10));
        process.setEndTimestamp(LocalDateTime.now());
        process.setProductConfiguration(product);
        process = processRepository.save(process);

        PackageUnit pkg = new PackageUnit();
        pkg.setMeasuredWeight(100);
        pkg.setDeviation(0);
        pkg.setProcess(process);
        packageRepository.save(pkg);
    }

    private String loginAndGetToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "admin",
                          "password": "admin123"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int tokenStart = response.indexOf("\"usertoken\":\"") + "\"usertoken\":\"".length();
        int tokenEnd = response.indexOf("\"", tokenStart);
        return response.substring(tokenStart, tokenEnd);
    }

    @Test
    void testGetOverviewStatistics() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/statistics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPackages").value(1))
                .andExpect(jsonPath("$.averageWeight").value(100.0));
    }

    @Test
    void testGetProductStatistics() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/statistics/product/" + productConfigId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPackages").value(1))
                .andExpect(jsonPath("$.targetWeight").value(100));
    }
}
