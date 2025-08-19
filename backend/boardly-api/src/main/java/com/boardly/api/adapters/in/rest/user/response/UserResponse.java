package com.boardly.api.adapters.in.rest.user.response;

import com.boardly.features.user.domain.User;

/**
 * 사용자 응답 DTO
 */
public record UserResponse(
        String userId,
        String email,
        String firstName,
        String lastName,
        boolean isActive
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId().getId(),
                user.getEmail(),
                user.getUserProfile().firstName(),
                user.getUserProfile().lastName(),
                user.isActive()
        );
    }
} 