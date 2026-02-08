package com.drag.foreignnationals.etranger.security.controller;

import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.enums.Role;
import com.drag.foreignnationals.etranger.security.dto.request.SignupRequest;
import com.drag.foreignnationals.etranger.security.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Only ADMIN can manage users
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody SignupRequest request) {
        User user = userService.addUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getLastName(),
                        user.getFirstName(),
                        user.getRole(),
                        user.isLocked(),
                        user.isActive()
                ));
    }

    @PostMapping("/users/{id}/lock")
    public ResponseEntity<UserResponse> lockUser(@PathVariable Long id) {
        User user = userService.lockUser(id);
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getLastName(),
                user.getFirstName(),
                user.getRole(),
                user.isLocked(),
                user.isActive()
        ));
    }

    @PostMapping("/users/{id}/unlock")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable Long id) {
        User user = userService.unlockUser(id);
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getLastName(),
                user.getFirstName(),
                user.getRole(),
                user.isLocked(),
                user.isActive()
        ));
    }

    @PostMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long id, @RequestParam Role role) {
       User user = userService.setRole(id, role);
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getLastName(),
                user.getFirstName(),
                user.getRole(),
                user.isLocked(),
                user.isActive()
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getLastName(),
                        user.getFirstName(),
                        user.getRole(),
                        user.isLocked(),
                        user.isActive()))
                .toList());
    }

    public record UserResponse(
            Long id,
            String username,
            String LastName,
            String FirstName,
            Role role,
            boolean isLocked,
            boolean isActive
    ) {}
}



