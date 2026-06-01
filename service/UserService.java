package com.servitrust.service;

import java.util.List;
import com.servitrust.model.User;
import com.servitrust.dto.RegisterRequest;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    List<User>getAllUsers();
    User login(String email, String password);
    User register(RegisterRequest request);
}
