package com.drag.foreignnationals.etranger.security.controller;

import com.drag.foreignnationals.etranger.security.dto.response.UserResponse;
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

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody SignupRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.addUser(request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PatchMapping("/users/{id}/lock")
    public ResponseEntity<UserResponse> lockUser(@PathVariable Long id) {

        return ResponseEntity.ok(userService.lockUser(id));
    }

    @PatchMapping("/users/{id}/unlock")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable Long id) {

        return ResponseEntity.ok(userService.unlockUser(id));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long id, @RequestParam Role role) {

        return ResponseEntity.ok(userService.setRole(id, role));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


}



