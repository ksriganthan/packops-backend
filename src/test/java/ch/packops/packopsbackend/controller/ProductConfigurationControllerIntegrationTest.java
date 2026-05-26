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

    // TC-UC08-02: Gültiges Produkt erstellen → 200 OK

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

    // TC-UC08-03: Ungültiges Gewicht → 400

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

    //TC-UC08-06: Viewer → 403 Forbidden

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

    @Test
    void getProducts_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProducts_withToken_returns200() throws Exception {
        String token = loginAndGetToken("viewer", "viewer123");

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // Hilfsmethoden

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
