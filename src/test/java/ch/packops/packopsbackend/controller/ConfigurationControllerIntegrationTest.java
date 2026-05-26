package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.repository.ConfigurationRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Kapischan Sriganthan
 */

@SpringBootTest
@AutoConfigureMockMvc
public class ConfigurationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        userSessionRepository.deleteAll();
        userRepository.deleteAll();
        configurationRepository.deleteAll();

        // Operator-User erstellen (hat Schreibrechte auf PUT /api/configuration)
        User operator = new User();
        operator.setUsername("operator");
        operator.setEmail("operator@test.ch");
        operator.setPasswordHash(passwordService.hash("operator123"));
        operator.setRole("operator");
        operator.setLanguage("de");
        userRepository.save(operator);

        // Default-Configuration erstellen (wird von updateConfiguration() erwartet)
        ch.packops.packopsbackend.domain.Configuration config = new ch.packops.packopsbackend.domain.Configuration();
        config.setTargetWeight(200);
        config.setTolerance(10);
        config.setMaxUnits(50);
        config.setMaxIterations(3);
        config.setLanguage("de");
        configurationRepository.save(config);
    }

    // TC-UC02-01: Gültige Config → 200 OK
    @Test
    void updateConfig_valid_returns200() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(put("/api/configuration")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "targetWeight": 250,
                          "tolerance": 5,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetWeight").value(250))
                .andExpect(jsonPath("$.tolerance").value(5))
                .andExpect(jsonPath("$.maxUnits").value(100))
                .andExpect(jsonPath("$.maxIterationsForReject").value(3));
    }

    /** TC-UC02-01: Grenzwert targetWeight = 50 → 200 OK */
    @Test
    void updateConfig_targetWeightMin_returns200() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(put("/api/configuration")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "targetWeight": 50,
                          "tolerance": 0,
                          "maxUnits": 10,
                          "maxIterationsForReject": 1
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetWeight").value(50));
    }

    // ── TC-UC02-02: Ungültige Config → 400 Bad Request ────────────

    @Test
    void updateConfig_tooLow_returns400() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(put("/api/configuration")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "targetWeight": 49,
                          "tolerance": 5,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateConfig_tooHigh_returns400() throws Exception {
        String token = loginAndGetToken("operator", "operator123");

        mockMvc.perform(put("/api/configuration")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "targetWeight": 501,
                          "tolerance": 5,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    /** Kein Token → 401 (Security greift vor dem Controller) */
    @Test
    void updateConfig_noToken_returns401() throws Exception {
        mockMvc.perform(put("/api/configuration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "targetWeight": 250,
                          "tolerance": 5,
                          "maxUnits": 100,
                          "maxIterationsForReject": 3
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    // Hilfsmethode

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
