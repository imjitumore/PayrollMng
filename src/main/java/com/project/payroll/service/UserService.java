package com.project.payroll.service;

import org.springframework.stereotype.Service;
import com.project.payroll.entity.User;
import com.project.payroll.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // save or update profile
    public User updateProfile(User user) {
        return repository.save(user);
    }

    // optional helper
    public User findByUsername(String username) {
        return repository.findByUsername(username).orElse(null);
    }
}
