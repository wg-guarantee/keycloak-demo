package com.webank.app.service;

import com.webank.app.entity.User;
import com.webank.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public User createUser(String username, String email, String phone) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已存在: " + email);
        }
        
        User user = User.builder()
            .username(username)
            .email(email)
            .phone(phone)
            .emailVerified(false)
            .phoneVerified(false)
            .accountActivated(false)
            .build();
        
        return userRepository.save(user);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @Transactional
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setAccountActivated(true);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }
    
    @Transactional
    public User verifyEmail(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setEmailVerified(true);
        return userRepository.save(user);
    }
    
    @Transactional
    public User verifyPhone(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setPhoneVerified(true);
        return userRepository.save(user);
    }
}
