package ch.packops.packopsbackend.service;
import ch.packops.packopsbackend.dto.UserCreateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Kapischan Sriganthan
 */

public class ValidationServiceTest {
       /**
     * Unit Tests für ValidationService
     * Phase-1-Referenz: Abschnitt 1.6.6, Seiten 34–39
     *   TC-UC02-03  — Zielgewicht 50–500g
     *   TC-UC02-04  — Toleranz positiv
     *   TC-UC02-05  — Toleranz negativ → Exception
     *   TC-UC04-05  — Passwort ≥ 6 Zeichen, E-Mail-Format
     */

        private ValidationService validationService;

        @BeforeEach
        void setUp() {
            // ValidationService hat keine Dependencies
            validationService = new ValidationService();
        }

        // TC-UC04-05: Passwort-Richtlinien

        @Test
        void validateUser_validPassword_noException() {
            UserCreateDto dto = validUser();
            dto.setPassword("abc123");
            assertDoesNotThrow(() -> validationService.validateUser(dto));
        }

        @Test
        void validateUser_tooShortPassword_throwsException() {
            UserCreateDto dto = validUser();
            dto.setPassword("ab12");
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> validationService.validateUser(dto)
            );
            assertTrue(ex.getMessage().contains("Password"));
        }

        @Test
        void validateUser_validEmail_noException() {
            UserCreateDto dto = validUser();
            dto.setEmail("kapi@packops.ch");
            assertDoesNotThrow(() -> validationService.validateUser(dto));
        }

        @Test
        void validateUser_invalidEmail_throwsException() {
            UserCreateDto dto = validUser();
            dto.setEmail("kein-at-zeichen.ch");
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> validationService.validateUser(dto)
            );
            assertTrue(ex.getMessage().contains("Email"));
        }

        //Hilfsmethoden


        private UserCreateDto validUser() {
            UserCreateDto dto = new UserCreateDto();
            dto.setUsername("testuser");
            dto.setEmail("test@packops.ch");
            dto.setPassword("sicher123");
            dto.setRole("operator");
            return dto;
        }
    }


