package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.UserCreateDto;
import ch.packops.packopsbackend.dto.UserDto;
import ch.packops.packopsbackend.dto.UserUpdateDto;
import ch.packops.packopsbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import ch.packops.packopsbackend.security.PasswordService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kapischan
 */

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final LoggingService loggingService;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository,
                       ValidationService validationService,
                       LoggingService loggingService,
                       PasswordService passwordService) {
        this.userRepository = userRepository;
        this.validationService = validationService;
        this.loggingService = loggingService;
        this.passwordService = passwordService;
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
        return dto;
    }

    public UserDto createUser(UserCreateDto dto) {
        // Validierung über ValidationService
        validationService.validateUser(dto);

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordService.hash(dto.getPassword()));
        user.setRole(dto.getRole());
        loggingService.logInfo("Benutzer erstellt: " + dto.getUsername(), null);
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
            existing.setRole(dto.getRole());
        }
        loggingService.logInfo("Benutzer aktualisiert: " + userId, null);
        return toDto(userRepository.save(existing));
    }

    public void deleteUser(Long userId) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        userRepository.delete(existing);
        loggingService.logInfo("Benutzer gelöscht: " + userId, null);
    }

    public UserDto changeLanguage(Long userId, String langCode) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (langCode == null || langCode.isEmpty()) {
            throw new IllegalArgumentException("Language code cannot be empty");
        }

        existing.setLanguage(langCode);
        return toDto(userRepository.save(existing));
    }
}