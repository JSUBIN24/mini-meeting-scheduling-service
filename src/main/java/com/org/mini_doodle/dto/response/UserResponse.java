package com.org.mini_doodle.dto.response;

import com.org.mini_doodle.domain.User;

public record UserResponse(Long id, String email, String name) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}