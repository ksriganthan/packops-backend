package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * @author Kapischan Sriganthan
 */

/**
 * Integrationstests für ProductConfigurationController
 *   TC-UC08-02 — Produkt mit gültigem Gewicht erstellen → 200 OK
 *   TC-UC08-03 — Produkt targetWeight ausserhalb 50–500 → 400 Bad Request
 *   TC-UC08-04 — Produkt negative Toleranz → 400 Bad Request
 *   TC-UC08-05 — Produkt löschen → 204 No Content
 *   TC-UC08-06 — Viewer darf kein Produkt erstellen → 403 Forbidden
 *
 * Teststrategie: @SpringBootTest + MockMvc + H2 In-Memory DB
 * → Vollständiges Zusammenspiel: Controller → Service → Validation → DB
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ProductConfigurationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private ProductConfigurationRepository productConfigurationRepository;

    @Autowired
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        userSessionRepository.deleteAll();
        userRepository.deleteAll();
        productConfigurationRepository.deleteAll();

        // Admin (darf Produkte erstellen/löschen)
        createUser("admin", "admin@test.ch", "admin123", "admin");
        // Viewer (darf nur lesen)
        createUser("viewer", "viewer@test.ch", "viewer123", "viewer");
    }

    // ── TC-UC08-02: Gültiges Produkt erstellen → 200 OK ───────────

    /**
     * TC-UC08-02: POST /api/products mit gültigen Werten (targetWeight 50–500)
     * → HTTP 200, Antwort enthält den Produktnamen
     */
    @Test
    void createProduct_valid_returns200() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token) // Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productName": "Kaffeebohnen",
                          "targetWeight": 250,
                          "tolerance": 5,
                          "description": "Premium Arabica"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kaffeebohnen"))
                .andExpect(jsonPath("$.defaultTargetWeight").value(250));
    }

    /**
     * TC-UC08-02: Grenzwert targetWeight = 50 (Minimum) → 200 OK
     */
    @Test
    void createProduct_boundaryMinWeight_returns200() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productName": "Leichtes Produkt",
                          "targetWeight": 50,
                          "tolerance": 0
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultTargetWeight").value(50));
    }

    // ── TC-UC08-03: Ungültiges Gewicht → 400 ──────────────────────

    /**
     * TC-UC08-03: targetWeight = 49 (unter Minimum 50g) → 400 Bad Request
     * ValidationService.validateProduct() wirft IllegalArgumentException
     */
    @Test
    void createProduct_tooLowWeight_returns400() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productName": "Zu leicht",
                          "targetWeight": 49,
                          "tolerance": 5
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-UC08-03: targetWeight = 501 (über Maximum 500g) → 400 Bad Request
     */
    @Test
    void createProduct_tooHighWeight_returns400() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productName": "Zu schwer",
                          "targetWeight": 501,
                          "tolerance": 5
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    // ── TC-UC08-04: Negative Toleranz → 400 ───────────────────────

    /**
     * TC-UC08-04: tolerance = -1 → 400 Bad Request
     */
    @Test
    void createProduct_negativeTolerance_returns400() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productName": "Falsche Toleranz",
                          "targetWeight": 250,
                          "tolerance": -1
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    // ── TC-UC08-05: Produkt löschen → 204 ─────────────────────────

    /**
     * TC-UC08-05: DELETE /api/products/{id} mit existierender ID → 204 No Content
     */
    @Test
    void deleteProduct_existing_returns204() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");

        // Zuerst ein Produkt erstellen
        String createResponse = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productName": "Zu löschendes Produkt",
                          "targetWeight": 200,
                          "tolerance": 3
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // Z.B. Antwort: {"id":1,"name":"Zu löschendes Produkt","defaultTargetWeight":200,"defaultTolerance":3,"description":null}

        // ID aus der Antwort extrahieren
        int idStart = createResponse.indexOf("\"id\":") + "\"id\":".length();
        int idEnd = createResponse.indexOf(",", idStart);
        String productId = createResponse.substring(idStart, idEnd).trim();

        // Produkt löschen
        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    // ── TC-UC08-06: Viewer → 403 Forbidden ────────────────────────

    /**
     * TC-UC08-06: Viewer hat keine Schreibrechte auf POST /api/products → 403
     * (Spring Security: nur admin darf Produkte erstellen)
     */
    @Test
    void createProduct_viewerForbidden_returns403() throws Exception {
        String viewerToken = loginAndGetToken("viewer", "viewer123");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "productName": "Verbotenes Produkt",
                          "targetWeight": 250,
                          "tolerance": 5
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    /**
     * GET /api/products ohne Token → 401 Unauthorized
     */
    @Test
    void getProducts_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * GET /api/products mit gültigem Token → 200 OK (alle Rollen dürfen lesen)
     */
    @Test
    void getProducts_withToken_returns200() throws Exception {
        String token = loginAndGetToken("viewer", "viewer123");

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ── Hilfsmethoden ─────────────────────────────────────────────

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
