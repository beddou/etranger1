package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.User;
import com.drag.foreignnationals.etranger.enums.Role;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.UserRepository;
import com.drag.foreignnationals.etranger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Add new user
    public User addUser(String username, String rawPassword, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setActive(true);
        user.setLocked(false);
        return userRepository.save(user);
    }

    // Lock user
    public void lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLocked(true);
        userRepository.save(user);
    }

    // Unlock user
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLocked(false);
        userRepository.save(user);
    }

    // Change role
    public void setRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    // List all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
