package com.securefilesharing.service;

import com.securefilesharing.model.User;
import java.util.List;

public interface UserService {
    User findById(Long id);
    List<User> findAll();
    User save(User user);
}
