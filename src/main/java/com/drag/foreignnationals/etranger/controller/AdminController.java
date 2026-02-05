package com.drag.foreignnationals.etranger.controller;

import com.drag.foreignnationals.etranger.entity.User;
import com.drag.foreignnationals.etranger.enums.Role;
import com.drag.foreignnationals.etranger.security.payload.SignupRequest;
import com.drag.foreignnationals.etranger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public User addUser(@RequestBody SignupRequest request) {
        return userService.addUser(request.getUsername(), request.getPassword(), request.getRole());
    }

    @PostMapping("/users/{id}/lock")
    public void lockUser(@PathVariable Long id) {
        userService.lockUser(id);
    }

    @PostMapping("/users/{id}/unlock")
    public void unlockUser(@PathVariable Long id) {
        userService.unlockUser(id);
    }

    @PostMapping("/users/{id}/role")
    public void changeRole(@PathVariable Long id, @RequestParam Role role) {
        userService.setRole(id, role);
    }

    @GetMapping("/users")
    public List<User> listUsers() {
        return userService.getAllUsers();
    }
}



