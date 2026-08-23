package com.joaodev.trackflowapi.auth.dto;

import com.joaodev.trackflowapi.auth.domain.User;

import java.time.LocalDateTime;

public record UserResponse(Long id, String email, String role, boolean active, LocalDateTime createdAt) {
    public static UserResponse from (User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isActive(), user.getCreatedAt());
    }
}
