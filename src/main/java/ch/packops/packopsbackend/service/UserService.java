package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.Role;
import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.UserCreateDto;
import ch.packops.packopsbackend.dto.UserDto;
import ch.packops.packopsbackend.dto.UserUpdateDto;
import ch.packops.packopsbackend.repository.UserRepository;
import ch.packops.packopsbackend.repository.UserSessionRepository;
import ch.packops.packopsbackend.security.TokenService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import ch.packops.packopsbackend.security.PasswordService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Kapischan
 */

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final ValidationService validationService;
    private final LoggingService loggingService;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, UserSessionRepository userSessionRepository,
                       ValidationService validationService,
                       LoggingService loggingService,
                       PasswordService passwordService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.validationService = validationService;
        this.loggingService = loggingService;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    // Domain → UserDto
    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setLanguage(user.getLanguage());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setActive(user.isActive());
        return dto;
    }

    public UserDto createUser(UserCreateDto dto) {
        // Validierung über ValidationService
        validationService.validateUser(dto);

        // Prüfung auf duplizierten Benutzernamen
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }

        // Prüfung auf duplizierte E-Mail
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordService.hash(dto.getPassword()));
        user.setRole(Role.matchRole(dto.getRole()));
        loggingService.logInfo("Benutzer erstellt: " + dto.getUsername(), null);

        if (dto.getLanguage() != null) {
            user.setLanguage(dto.getLanguage());
        }
        return toDto(userRepository.save(user));
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return toDto(user);
    }

    public UserDto updateUser(Long userId, UserUpdateDto dto) {

        // Validierung über ValidationService
        validationService.validateUserUpdate(dto);

        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            existing.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            existing.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existing.setPasswordHash(passwordService.hash(dto.getPassword()));
        }
        if (dto.getRole() != null && !dto.getRole().isEmpty()) {
            existing.setRole(Role.matchRole(dto.getRole()));
        }

        if (dto.getLanguage() != null && !dto.getLanguage().isEmpty()) {
            existing.setLanguage(dto.getLanguage());
        }
        loggingService.logInfo("Benutzer aktualisiert: " + userId, null);
        return toDto(userRepository.save(existing));
    }

    public void deactivateOrActivateUser(Long userId) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        existing.setActive(!existing.isActive());
        userRepository.save(existing);
        // Tokens aller Sessions des Benutzers invalidieren, wenn der Benutzer deaktiviert wird
        if (!existing.isActive()) {
            userSessionRepository.findAllByUserId(existing.getId()).forEach(
                            (session) ->
                                    tokenService.invalidateToken(session.getToken()
                                    )
                    );
        }
        String action = existing.isActive() ? "aktiviert" : "deaktiviert";
        loggingService.logInfo("Benutzer und Session " + action + ": " + userId, null);
    }

    public UserDto updatePassword(Long userId, UserUpdateDto dto) {
        validationService.validateUserUpdate(dto);

        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (dto.getOldPassword() != null && !dto.getOldPassword().isEmpty()) {
            if (passwordService.matches(dto.getOldPassword(), existing.getPasswordHash())) {
                existing.setPasswordHash(passwordService.hash(dto.getPassword()));
            } else {
                throw new IllegalArgumentException("Old password does not match");
            }
        }

        return toDto(userRepository.save(existing));
    }
}