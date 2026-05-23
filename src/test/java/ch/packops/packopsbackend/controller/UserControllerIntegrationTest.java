package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.User;
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
 * Integrationstests für UserController
 *   TC-UC04-02 — Der neue Benutzer ist in der Datenbank vorhanden
 *   TC-UC04-04 — Benutzername existiert bereits → API liefert Fehler
 *   TC-UC05-02 — Klick auf User → API liefert Details
 *   TC-UC07-02 — Falsche Daten → Fehlermeldung
 *   TC-UC07-03 — Logout → Token wird ungültig
 *
 * Teststrategie: @SpringBootTest + MockMvc + H2 In-Memory DB
 * → Vollständiges Zusammenspiel: Controller → Service → DB
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        userSessionRepository.deleteAll();
        userRepository.deleteAll();

        // Admin (darf Benutzer verwalten)
        createUser("admin", "admin@test.ch", "admin123", "admin");
        // Operator (normaler Benutzer)
        createUser("operator", "operator@test.ch", "operator123", "operator");
    }

    // ── TC-UC04-02: Benutzer ist in der Datenbank vorhanden ───────────

    /**
     * TC-UC04-02: POST /api/users mit gültigen Daten
     * → HTTP 200, Benutzer ist in DB gespeichert
     */
    @Test
    void createUser_valid_userExistsInDatabase() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "newuser",
                          "email": "newuser@test.ch",
                          "password": "secure123",
                          "role": "operator"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@test.ch"));

        // Prüfung: Benutzer existiert in Datenbank
        assert userRepository.findByUsername("newuser").isPresent();
    }

    // ── TC-UC04-04: Benutzername existiert bereits → Fehler ───────────

    /**
     * TC-UC04-04: POST /api/users mit existierendem Benutzernamen
     * → HTTP 400 Bad Request
     */
    @Test
    void createUser_duplicateUsername_returns400() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "admin",
                          "email": "duplicate@test.ch",
                          "password": "test123",
                          "role": "operator"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    // ── TC-UC05-02: API liefert Benutzer-Details ──────────────────────

    /**
     * TC-UC05-02: GET /api/users/{id} mit gültigem User
     * → HTTP 200, korrekte Detaildaten
     */
    @Test
    void getUser_existingId_returnsDetails() throws Exception {
        String token = loginAndGetToken("admin", "admin123");
        User operator = userRepository.findByUsername("operator").orElseThrow();

        mockMvc.perform(get("/api/users/" + operator.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("operator"))
                .andExpect(jsonPath("$.email").value("operator@test.ch"))
                .andExpect(jsonPath("$.role").value("operator"));
    }

    /**
     * TC-UC05-02: GET /api/users/{id} mit nicht-existierender ID
     * → HTTP 404 oder 500 (Exception)
     */
    @Test
    void getUser_nonExistingId_returnsError() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/users/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is5xxServerError());
    }

    // ── TC-UC06-05: Login mit gelöschtem Account → Fehler ─────────────


    // ── TC-UC07-02: Falsche Daten → Fehlermeldung ─────────────────────

    /**
     * TC-UC07-02: POST /api/auth/login mit falschem Passwort
     * → HTTP 401, keine Token-Ausgabe
     */
    @Test
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "admin",
                          "password": "falsches_passwort"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC-UC07-02: POST /api/auth/login mit falschem Benutzernamen
     * → HTTP 401
     */
    @Test
    void login_wrongUsername_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "nichtexistent",
                          "password": "admin123"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    // ── TC-UC07-03: Logout → Token wird ungültig ──────────────────────

    /**
     * TC-UC07-03: POST /api/auth/logout → Token wird ungültig
     * → weitere Requests mit diesem Token werden abgelehnt (401)
     */
    @Test
    void logout_invalidatesToken_returns401OnNextRequest() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Weiterer Request mit altem Token
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    // ── TC-UC09-04: Sprachwechsel ─────────────────────────────────────

    /**
     * TC-UC09-04: PUT /api/users/{id}/language mit gültiger Sprache
     * → HTTP 200, Sprache wird aktualisiert
     */
    @Test
    void changeLanguage_valid_returns200() throws Exception {
        String token = loginAndGetToken("admin", "admin123");
        User admin = userRepository.findByUsername("admin").orElseThrow();

        mockMvc.perform(put("/api/users/" + admin.getId() + "/language")
                        .header("Authorization", "Bearer " + token)
                        .param("langCode", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("en"));
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
