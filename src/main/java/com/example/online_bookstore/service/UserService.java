package com.example.online_bookstore.service;

import com.example.online_bookstore.entity.User;
import com.example.online_bookstore.exception.ResourceNotFoundException;
import com.example.online_bookstore.exception.UnauthorizedException;
import com.example.online_bookstore.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Add methods for user management
    public User updateUserProfile(Long userId, User profileDto) {
        User user = getUserById(userId);
        user.setName(profileDto.getName());
        return userRepository.save(user);
    }

    // Implement this method:
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}