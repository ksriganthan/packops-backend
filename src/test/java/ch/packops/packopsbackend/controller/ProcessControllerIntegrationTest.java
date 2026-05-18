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
 * Integrationstests für ProcessController
 * Phase-1-Referenz: Abschnitt 1.6.6, Seiten 34–39
 *   TC-UC01-02 — Start-Request mit ungültigen Parametern → Server liefert Fehler (400)
 *   TC-UC02-02 — Zielgewicht <50 oder >500 → Request abgelehnt
 *   TC-UC02-05 — Toleranz negativ → Fehler, keine Speicherung
 *   TC-UC03-02 — Datenbank leer → API liefert leere Liste
 *   TC-UC03-03 — Service wirft Fehler → API liefert Fehlerstatus
 *
 * Teststrategie: @SpringBootTest + MockMvc + H2 In-Memory DB
 * → Vollständiges Zusammenspiel: Security → Controller → Service → Validation → DB
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
    private PasswordService passwordService;

    private Long productConfigId;

    @BeforeEach
    void setUp() {
        // Cleanup
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

        // Globale Configuration erstellen (für Cascading-Tests)
        Configuration config = new Configuration();
        config.setTargetWeight(250);
        config.setTolerance(10);
        config.setMaxUnits(100);
        config.setMaxIterations(3);
        configurationRepository.save(config);
    }

    // ── TC-UC01-02: Start mit ungültigen Parametern → 400 ─────────────

    /**
     * TC-UC01-02: POST /api/process/start mit fehlendem productConfigurationId
     * → HTTP 400 Bad Request
     */
    @Test
    void startProcess_missingProductConfigId_returns400() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(post("/api/process/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "targetWeight": 250,
                          "tolerance": 10,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-UC01-02: POST /api/process/start mit nicht-existierender productConfigurationId
     * → HTTP 500 (RuntimeException: ProductConfiguration not found)
     */
    @Test
    void startProcess_nonExistingProductConfigId_returns500() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(post("/api/process/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productConfigurationId": 99999,
                          "targetWeight": 250,
                          "tolerance": 10,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """))
                .andExpect(status().is5xxServerError());
    }

    // ── TC-UC02-02: Zielgewicht außerhalb 50–500 → 400 ────────────────

    /**
     * TC-UC02-02: targetWeight = 49 (unter Minimum) → 400 Bad Request
     */
    @Test
    void startProcess_targetWeightTooLow_returns400() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(post("/api/process/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productConfigurationId": %d,
                          "targetWeight": 49,
                          "tolerance": 10,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """.formatted(productConfigId)))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-UC02-02: targetWeight = 501 (über Maximum) → 400 Bad Request
     */
    @Test
    void startProcess_targetWeightTooHigh_returns400() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(post("/api/process/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productConfigurationId": %d,
                          "targetWeight": 501,
                          "tolerance": 10,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """.formatted(productConfigId)))
                .andExpect(status().isBadRequest());
    }

    // ── TC-UC02-05: Toleranz negativ → 400 ────────────────────────────

    /**
     * TC-UC02-05: tolerance = -1 → 400 Bad Request
     */
    @Test
    void startProcess_negativeTolerance_returns400() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(post("/api/process/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productConfigurationId": %d,
                          "targetWeight": 250,
                          "tolerance": -1,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """.formatted(productConfigId)))
                .andExpect(status().isBadRequest());
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

        mockMvc.perform(get("/api/process/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is5xxServerError());
    }

    // ── Erfolgreicher Prozess-Start (Happy Path) ──────────────────────

    /**
     * POST /api/process/start mit gültigen Parametern
     * → HTTP 200, Prozess wird erstellt
     */
    @Test
    void startProcess_validParameters_returns200() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(post("/api/process/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productConfigurationId": %d,
                          "targetWeight": 250,
                          "tolerance": 10,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """.formatted(productConfigId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetWeight").value(250))
                .andExpect(jsonPath("$.tolerance").value(10));
    }

    /**
     * Cascading Configuration Test:
     * ProductConfiguration-Defaults werden von Configuration überschrieben
     */
    @Test
    void startProcess_withoutDTO_usesCascadingDefaults() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(post("/api/process/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productConfigurationId": %d
                        }
                        """.formatted(productConfigId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetWeight").value(250))
                .andExpect(jsonPath("$.tolerance").value(10));
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
                        """.formatted(productConfigId)))
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
                        """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int tokenStart = response.indexOf("\"usertoken\":\"") + "\"usertoken\":\"".length();
        int tokenEnd = response.indexOf("\"", tokenStart);
        return response.substring(tokenStart, tokenEnd);
    }
}
