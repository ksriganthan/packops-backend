package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.domain.User;
import ch.packops.packopsbackend.dto.UserCreateDto;
import ch.packops.packopsbackend.dto.UserDto;
import ch.packops.packopsbackend.dto.UserUpdateDto;
import ch.packops.packopsbackend.security.AuthService;
import ch.packops.packopsbackend.security.AuthorizationService;
import ch.packops.packopsbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Kapischan
 */

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final AuthorizationService authorizationService;

    public UserController(UserService userService,
                          AuthService authService,
                          AuthorizationService authorizationService) {
        this.userService = userService;
        this.authService = authService;
        this.authorizationService = authorizationService;
    }


    // POST /api/users
    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestParam String token,
            @RequestBody UserCreateDto dto) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canManageUsers(user)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            UserDto created = userService.createUser(dto);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam String token) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canManageUsers(user)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            List<UserDto> users = userService.getUsers();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // GET /api/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long userId,
            @RequestParam String token) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canManageUsers(user) && !user.getId().equals(userId)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            UserDto dto = userService.getUserById(userId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // PUT /api/users/{userId}
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestParam String token,
            @RequestBody UserUpdateDto dto) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canManageUsers(user) && !user.getId().equals(userId)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            UserDto updated = userService.updateUser(userId, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // DELETE /api/users/{userId}
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            @RequestParam String token) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canManageUsers(user)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            userService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    // PUT /api/users/{userId}/language
    @PutMapping("/{userId}/language")
    public ResponseEntity<?> changeLanguage(
            @PathVariable Long userId,
            @RequestParam String token,
            @RequestParam String langCode) {
        try {
            User user = authService.authenticate(token);
            if (!authorizationService.canManageUsers(user) && !user.getId().equals(userId)) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            UserDto updated = userService.changeLanguage(userId, langCode);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }
}