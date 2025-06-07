// File: src/main/java/com/project/QLDSV_HTC/service/UserService.java
package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.entity.User;
import com.project.QLDSV_HTC.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public Optional<User> findById(String username) {
        return userRepo.findById(username);
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsById(username);
    }

    public User save(User user) {
        return userRepo.save(user);
    }

    public void delete(String username) {
        userRepo.deleteById(username);
    }
}
