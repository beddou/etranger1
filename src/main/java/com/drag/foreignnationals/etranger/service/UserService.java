package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.entity.User;
import com.drag.foreignnationals.etranger.enums.Role;

import java.util.List;

public interface UserService {
    public User addUser(String username, String rawPassword, Role role);
    public void lockUser(Long userId);
    public void unlockUser(Long userId);
    public void setRole(Long userId, Role role);
    public List<User> getAllUsers();

}
