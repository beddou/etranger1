package com.drag.foreignnationals.etranger.security.service.impl;

import com.drag.foreignnationals.etranger.enums.Role;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.security.dto.mapper.UserMapper;
import com.drag.foreignnationals.etranger.security.dto.request.SignupRequest;
import com.drag.foreignnationals.etranger.security.dto.response.UserResponse;
import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.security.repository.UserRepository;
import com.drag.foreignnationals.etranger.security.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    // Add new user
    public UserResponse addUser(SignupRequest signupRequest) {


        Optional<User> existing =
                userRepository.findByUsernameIncludeDeleted(signupRequest.getUsername());

        if (existing.isPresent()) {
            User user = existing.get();

            if (user.isDeleted()) {
                // restore account
                user.setDeleted(false);
                user.setDeletedAt(null);
                user.setActive(true);
                user.setLocked(false);
                user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
                user.setRole(signupRequest.getRole());

                return userMapper.toDTO(userRepository.save(user));
            }

            throw new BusinessException(
                    ErrorCode.ENTITY_ALREADY_EXISTS,
                    "Username already exists: " + signupRequest.getUsername());
        }

        User user = userMapper.toEntity(signupRequest);
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setActive(true);
        user.setLocked(false);

        return userMapper.toDTO(userRepository.save(user));



    }

    // delete user
    public void deleteUser(Long userId) {
        User user = findUserOrThrow(userId);
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();

        // Prevent Self-Deletion
        if (user.getUsername().equals(currentAdmin)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR, "You cannot delete your own account.");
        }
        userRepository.delete(user);

    }

    // Lock user
    public UserResponse lockUser(Long userId) {
        User user = findUserOrThrow(userId);
        // Prevent Admin from locking themselves
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getUsername().equals(currentAdmin))
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED, "Security Breach: You cannot lock your own account!");

        user.setLocked(true);
        return userMapper.toDTO(userRepository.save(user));
    }

    // Unlock user
    public UserResponse unlockUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setLocked(false);
        return userMapper.toDTO(userRepository.save(user));
    }

    // Change role
    public UserResponse setRole(Long userId, Role role) {
        User user = findUserOrThrow(userId);
        user.setRole(role);
        return userMapper.toDTO(userRepository.save(user));
    }

    // List all users

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll().stream()
                .map(userMapper::toDTO).toList();
    }

    // 5. Private helper to fix that "User" error message!
    @Transactional(readOnly = true)
    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "User not found with ID: " + userId));
    }
}
