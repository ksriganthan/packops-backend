package ch.packops.packopsbackend.service.UnitTests;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.UserCreateDto;
import ch.packops.packopsbackend.dto.UserDto;
import ch.packops.packopsbackend.dto.UserUpdateDto;
import ch.packops.packopsbackend.repository.UserRepository;
import ch.packops.packopsbackend.security.PasswordService;
import ch.packops.packopsbackend.service.LoggingService;
import ch.packops.packopsbackend.service.UserService;
import ch.packops.packopsbackend.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Kapischan Sriganthan
 */

/**
 * Unit Tests für UserService
 * Phase-1-Referenz: Abschnitt 1.6.6, Seiten 34–39
 *   TC-UC05-04 — Benutzer-Suchmethode liefert alle Benutzer
 *   TC-UC06-02 — UserID existiert nicht → Exception
 *   TC-UC06-03 — Passwort wird gehasht (nicht Klartext gespeichert)
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private LoggingService loggingService;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserService userService;

    // ── TC-UC05-04: Alle Benutzer zurückgeben ─────────────────────

    /** TC-UC05-04: getUsers() gibt alle User als DTO-Liste zurück */
    @Test
    void getUsers_returnsAllUsers() {
        User user1 = createUser(1L, "admin", "admin@packops.ch", "admin");
        User user2 = createUser(2L, "operator", "op@packops.ch", "operator");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> result = userService.getUsers();

        assertEquals(2, result.size());
        assertEquals("admin", result.get(0).getUsername());
        assertEquals("admin@packops.ch", result.get(0).getEmail());
        assertEquals("operator", result.get(1).getUsername());
    }

    // ── TC-UC06-02: UserID existiert nicht → Exception ────────────

    /** TC-UC06-02: deleteUser() mit nicht-existierender ID → RuntimeException */
    @Test
    void deleteUser_notFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.deleteUser(999L)
        );
        assertTrue(ex.getMessage().contains("999") || ex.getMessage().contains("not found"));
    }

    /** TC-UC06-02: updateUser() mit nicht-existierender ID → RuntimeException */
    @Test
    void updateUser_notFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername("neuer-name");

        assertThrows(RuntimeException.class, () -> userService.updateUser(999L, dto));
    }

    // ── TC-UC06-03: Passwort wird gehasht ─────────────────────────

    /**
     * TC-UC06-03: updateUser() ruft passwordService.hash() auf —
     * das Klartext-Passwort wird nie direkt gespeichert.
     */
    @Test
    void updateUser_passwordIsHashed() {
        User existing = createUser(1L, "kapi", "kapi@packops.ch", "operator");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(passwordService.hash("neues-passwort123")).thenReturn("$2a$10$hashed_value");

        UserUpdateDto dto = new UserUpdateDto();
        dto.setPassword("neues-passwort123");

        userService.updateUser(1L, dto);

        // hash() muss aufgerufen worden sein
        verify(passwordService, times(1)).hash("neues-passwort123");

        // In der DB steht der Hash, nicht das Klartext-Passwort
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("$2a$10$hashed_value", captor.getValue().getPasswordHash());
    }

    /** TC-UC06-03: createUser() hasht das Passwort beim Erstellen */
    @Test
    void createUser_passwordIsHashedOnCreate() {
        when(passwordService.hash("start123")).thenReturn("$2a$10$hashed_on_create");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("neuer");
        dto.setEmail("neuer@packops.ch");
        dto.setPassword("start123");
        dto.setRole("viewer");

        userService.createUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("$2a$10$hashed_on_create", captor.getValue().getPasswordHash());
    }

    // ── Hilfsmethode ──────────────────────────────────────────────

    private User createUser(Long id, String username, String email, String role) {
        User user = new User();
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("User-ID konnte nicht gesetzt werden", e);
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setPasswordHash("$2a$10$dummy_hash");
        return user;
    }
}
