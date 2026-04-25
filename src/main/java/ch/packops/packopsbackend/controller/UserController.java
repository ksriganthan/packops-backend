package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.UserCreateDto;
import ch.packops.packopsbackend.dto.UserDto;
import ch.packops.packopsbackend.dto.UserUpdateDto;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users
    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestParam String token,
            @RequestBody UserCreateDto dto) {
        // TODO: Token-Validierung
        try {
            UserDto created = userService.createUser(dto);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam String token) {
        // TODO: Token-Validierung
        try {
            List<UserDto> users = userService.getUsers();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long userId,
            @RequestParam String token) {
        // TODO: Token-Validierung
        try {
            UserDto user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/users/{userId}
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestParam String token,
            @RequestBody UserUpdateDto dto) {
        // TODO: Token-Validierung
        try {
            UserDto updated = userService.updateUser(userId, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/users/{userId}
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            @RequestParam String token) {
        // TODO: Token-Validierung
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/users/{userId}/language
    @PutMapping("/{userId}/language")
    public ResponseEntity<?> changeLanguage(
            @PathVariable Long userId,
            @RequestParam String token,
            @RequestParam String langCode) {
        // TODO: Token-Validierung
        try {
            UserDto updated = userService.changeLanguage(userId, langCode);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}