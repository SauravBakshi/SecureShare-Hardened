package com.securefilesharing.controller;

import com.securefilesharing.model.User;
import com.securefilesharing.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.securefilesharing.security.CurrentUser;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserSummary>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll().stream().map(UserSummary::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        if (!CurrentUser.id().equals(id) && !SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.findById(id));
    }

    public record UserSummary(Long userId, String username) {
        static UserSummary from(User u) { return new UserSummary(u.getId(), u.getUsername()); }
    }
}
