package com.joaodev.trackflowapi.auth.service;

import com.joaodev.trackflowapi.auth.domain.User;
import com.joaodev.trackflowapi.auth.dto.CreateUserRequest;
import com.joaodev.trackflowapi.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new EmailAlreadyRegisteredException(request.email());
        });

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(true)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    public List<User> listUsers() {
        return userRepository.findByDeletedFalse();
    }

    public User setActive(Long userId, boolean active) {
        User user = getUserById(userId);
        user.setActive(active);
        return userRepository.save(user);
    }

    public User changeRole(Long userId, String newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId, String currentUserEmail) {
        User user = getUserById(userId);

        if (user.getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new SelfDeletionNotAllowedException();
        }

        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}