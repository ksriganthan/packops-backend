package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.Configuration;
import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.repository.*;
import ch.packops.packopsbackend.security.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Kapischan Sriganthan
 */

/**
 * Integrationstests
 *   TC-UC03-02 — Datenbank leer → API liefert leere Liste
 *   TC-UC03-03 — Service wirft Fehler → API liefert Fehlerstatus
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ProcessControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProductConfigurationRepository productConfigurationRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordService passwordService;

    private Long productConfigId;

    @BeforeEach
    void setUp() {
        // Cleanup aller relevanten Tabellen, damit jeder Test mit einem sauberen Zustand startet
        auditLogRepository.deleteAll();
        processRepository.deleteAll();
        userSessionRepository.deleteAll();
        userRepository.deleteAll();
        productConfigurationRepository.deleteAll();
        configurationRepository.deleteAll();

        // Users erstellen
        createUser("admin", "admin@test.ch", "admin123", "admin");
        createUser("operator", "operator@test.ch", "operator123", "operator");

        // ProductConfiguration erstellen
        ProductConfiguration productConfig = new ProductConfiguration();
        productConfig.setName("Kaffeebohnen");
        productConfig.setDefaultTargetWeight(250);
        productConfig.setDefaultTolerance(10);
        productConfig.setPackageUnits(100);
        productConfig.setActive(true);
        productConfigId = productConfigurationRepository.save(productConfig).getId();

        // Globale Configuration erstellen
        Configuration config = new Configuration();
        config.setTargetWeight(250);
        config.setTolerance(10);
        config.setMaxUnits(100);
        config.setMaxIterations(3);
        configurationRepository.save(config);
    }

       // ── TC-UC03-02: Datenbank leer → leere Liste ──────────────────────

    /**
     * TC-UC03-02: GET /api/process (Datenbank leer)
     * → HTTP 200, leere Liste
     */
    @Test
    void getProcesses_emptyDatabase_returnsEmptyList() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(get("/api/process")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── TC-UC03-03: Service wirft Fehler → Fehlerstatus ───────────────

    /**
     * TC-UC03-03: GET /api/process/{id} mit nicht-existierender ID
     * → HTTP 500 (Service wirft RuntimeException)
     */
    @Test
    void getProcess_nonExistingId_returns500() throws Exception {
        String token = loginAndGetToken("operator", "operator123");
        Long maxId = processRepository.findAll().stream()
                .map(p -> p.getId())
                .max(Long::compareTo)
                .orElse(0L);
        long nonExistingId = maxId + 1000;

        mockMvc.perform(get("/api/process/" + nonExistingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is5xxServerError());
    }

    // ── Admin sieht alle Prozesse, Operator nur eigene ────────────────

    /**
     * Operator erstellt Prozess → Admin sieht ihn, Operator auch
     */
    @Test
    void getProcesses_adminSeesAll_operatorSeesOwn() throws Exception {
        String operatorToken = loginAndGetToken("operator", "operator123");
        String adminToken = loginAndGetToken("admin", "admin123");

        // Operator erstellt Prozess
        mockMvc.perform(post("/api/process/start")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productConfigurationId": %d,
                          "targetWeight": 250,
                          "tolerance": 10,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """.formatted(productConfigId))) // ProductConfigID - siehe ganz oben -> wird dynamisch erzeugt
                .andExpect(status().isOk());

        // Admin sieht alle Prozesse
        mockMvc.perform(get("/api/process")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Operator sieht nur eigene
        mockMvc.perform(get("/api/process")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── Hilfsmethoden ─────────────────────────────────────────────────

    private void createUser(String username, String email, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordService.hash(password));
        user.setRole(role);
        user.setLanguage("de");
        userRepository.save(user);
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password))) // Die Werte von Args werden eingesetzt
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int tokenStart = response.indexOf("\"usertoken\":\"") + "\"usertoken\":\"".length();
        int tokenEnd = response.indexOf("\"", tokenStart);
        return response.substring(tokenStart, tokenEnd);
    }
}
