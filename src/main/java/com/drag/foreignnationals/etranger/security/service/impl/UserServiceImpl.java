package com.drag.foreignnationals.etranger.security.service.impl;

import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.security.dto.request.SignupRequest;
import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.enums.Role;
import com.drag.foreignnationals.etranger.security.repository.UserRepository;
import com.drag.foreignnationals.etranger.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Add new user
    public User addUser(SignupRequest signupRequest) {
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setLastName(signupRequest.getLastName());
        user.setFirstName(signupRequest.getFirstName());
        user.setDateOfBirth(signupRequest.getDateOfBirth());
        user.setRole(signupRequest.getRole());
        user.setActive(true);
        user.setLocked(false);
        return userRepository.save(user);
    }

    // Lock user
    public User lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "Nationality not found"));
        user.setLocked(true);
        return userRepository.save(user);
    }

    // Unlock user
    public User unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "Nationality not found"));
        user.setLocked(false);
        return userRepository.save(user);
    }

    // Change role
    public User setRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "Nationality not found"));
        user.setRole(role);
        return userRepository.save(user);
    }

    // List all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
