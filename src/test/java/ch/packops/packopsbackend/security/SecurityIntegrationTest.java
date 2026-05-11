package ch.packops.packopsbackend.security;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ch.packops.packopsbackend.repository.UserSessionRepository;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

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

        createUser("admin", "admin@test.ch", "admin123", "admin");
        createUser("operator", "operator@test.ch", "operator123", "operator");
        createUser("viewer", "viewer@test.ch", "viewer123", "viewer");
    }

    @Test
    void loginWithValidCredentialsReturnsJwt() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usertoken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.role").value("admin"));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCannotAccessUserManagement() throws Exception {
        String token = loginAndExtractToken("viewer", "viewer123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessUserManagement() throws Exception {
        String token = loginAndExtractToken("admin", "admin123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void logoutInvalidatesToken() throws Exception {
        String token = loginAndExtractToken("admin", "admin123");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private void createUser(String username, String email, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordService.hash(password));
        user.setRole(role);
        user.setLanguage("de");

        userRepository.save(user);
    }

    private String loginAndExtractToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int tokenStart = response.indexOf("\"usertoken\":\"") + "\"usertoken\":\"".length();
        int tokenEnd = response.indexOf("\"", tokenStart);

        return response.substring(tokenStart, tokenEnd);
    }
}