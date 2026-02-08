package com.drag.foreignnationals.etranger.security.service;

import com.drag.foreignnationals.etranger.security.dto.request.SignupRequest;
import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.enums.Role;

import java.util.List;

public interface UserService {
    public User addUser(SignupRequest signupRequest);
    public User lockUser(Long userId);
    public User unlockUser(Long userId);
    public User setRole(Long userId, Role role);
    public List<User> getAllUsers();

}
