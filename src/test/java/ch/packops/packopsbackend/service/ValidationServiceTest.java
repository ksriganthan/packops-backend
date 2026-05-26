package ch.packops.packopsbackend.service;
import ch.packops.packopsbackend.dto.ConfigurationDto;
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

        //  TC-UC02-03: Zielgewicht Grenzwerte

        @Test
        void validateConfig_boundaryMin_noException() {
            ConfigurationDto dto = validConfig();
            dto.setTargetWeight(50);
            assertDoesNotThrow(() -> validationService.validateConfiguration(dto));
        }

        @Test
        void validateConfig_boundaryMax_noException() {
            ConfigurationDto dto = validConfig();
            dto.setTargetWeight(500);
            assertDoesNotThrow(() -> validationService.validateConfiguration(dto));
        }

        @Test
        void validateConfig_validTargetWeight_noException() {
            ConfigurationDto dto = validConfig();
            dto.setTargetWeight(250);
            assertDoesNotThrow(() -> validationService.validateConfiguration(dto));
        }

        @Test
        void validateConfig_tooLow_throwsException() {
            ConfigurationDto dto = validConfig();
            dto.setTargetWeight(49);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> validationService.validateConfiguration(dto)
            );
            assertTrue(ex.getMessage().contains("TargetWeight"));
        }

        @Test
        void validateConfig_tooHigh_throwsException() {
            ConfigurationDto dto = validConfig();
            dto.setTargetWeight(501);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> validationService.validateConfiguration(dto)
            );
            assertTrue(ex.getMessage().contains("TargetWeight"));
        }

        // TC-UC02-04: Toleranz positiv

        @Test
        void validateConfig_validTolerance_noException() {
            ConfigurationDto dto = validConfig();
            dto.setTolerance(10);
            assertDoesNotThrow(() -> validationService.validateConfiguration(dto));
        }

        @Test
        void validateConfig_zeroTolerance_noException() {
            ConfigurationDto dto = validConfig();
            dto.setTolerance(0);
            assertDoesNotThrow(() -> validationService.validateConfiguration(dto));
        }

        //TC-UC02-05: Toleranz negativ → Fehler, keine Speicherung

        @Test
        void validateConfig_negativeTolerance_throwsException() {
            ConfigurationDto dto = validConfig();
            dto.setTolerance(-1);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> validationService.validateConfiguration(dto)
            );
            assertTrue(ex.getMessage().contains("Tolerance"));
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

        private ConfigurationDto validConfig() {
            ConfigurationDto dto = new ConfigurationDto();
            dto.setTargetWeight(250);
            dto.setTolerance(5);
            dto.setMaxUnits(100);
            dto.setMaxIterationsForReject(3);
            return dto;
        }

        private UserCreateDto validUser() {
            UserCreateDto dto = new UserCreateDto();
            dto.setUsername("testuser");
            dto.setEmail("test@packops.ch");
            dto.setPassword("sicher123");
            dto.setRole("operator");
            return dto;
        }
    }


