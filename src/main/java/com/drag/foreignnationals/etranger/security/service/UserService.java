package com.drag.foreignnationals.etranger.security.service;

import com.drag.foreignnationals.etranger.security.dto.request.SignupRequest;
import com.drag.foreignnationals.etranger.security.dto.response.UserResponse;
import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.enums.Role;

import java.util.List;

public interface UserService {
    public UserResponse addUser(SignupRequest signupRequest);
    public void deleteUser(Long userId);
    public UserResponse lockUser(Long userId);
    public UserResponse unlockUser(Long userId);
    public UserResponse setRole(Long userId, Role role);
    public List<UserResponse> getAllUsers();

}
