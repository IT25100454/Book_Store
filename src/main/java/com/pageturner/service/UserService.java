package com.pageturner.service;

import com.pageturner.model.User;
import java.util.List;

public interface UserService {
    User saveUser(User user);
    User findByUsername(String username);
    List<User> getAllUsers();
    User getUserById(Long id);
    void updateUser(User user);
    long getTotalUsersCount();
    void deleteUser(Long id);
    long countByRole(String role);
    User findById(Long id);
    void changePassword(User user, String newRawPassword);
}
