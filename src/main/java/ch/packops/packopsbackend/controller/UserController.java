package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.UserCreateDto;
import ch.packops.packopsbackend.dto.UserDto;
import ch.packops.packopsbackend.dto.UserUpdateDto;
import ch.packops.packopsbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String ROLE_ADMIN = "admin";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users
    // Nur Admin gemäss SecurityConfig
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateDto dto) {
        UserDto created = userService.createUser(dto);
        return ResponseEntity.ok(created);
    }

    // GET /api/users
    // Nur Admin gemäss SecurityConfig
    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    // GET /api/users/{userId}
    // Admin darf alle sehen, User darf sich selbst sehen
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long userId,
            @AuthenticationPrincipal Jwt jwt) {

        if (!isAdmin(jwt) && !isOwnUser(jwt, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // PUT /api/users/{userId}
    // Admin darf alle bearbeiten, User darf sich selbst bearbeiten
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateDto dto,
            @AuthenticationPrincipal Jwt jwt) {

        if (!isAdmin(jwt) && !isOwnUser(jwt, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.updateUser(userId, dto));
    }

    // DELETE /api/users/{userId}
    // Nur Admin gemäss SecurityConfig
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/users/{userId}/language
    // Admin darf alle ändern, User darf eigene Sprache ändern
    @PutMapping("/{userId}/language")
    public ResponseEntity<UserDto> changeLanguage(
            @PathVariable Long userId,
            @RequestParam String langCode,
            @AuthenticationPrincipal Jwt jwt) {

        if (!isAdmin(jwt) && !isOwnUser(jwt, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.changeLanguage(userId, langCode));
    }

    private boolean isAdmin(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    private boolean isOwnUser(Jwt jwt, Long userId) {
        Long currentUserId = jwt.getClaim("userId");
        return currentUserId != null && currentUserId.equals(userId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}