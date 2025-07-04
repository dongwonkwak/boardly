package com.boardly.features.user.presentation.request;

/**
 * 사용자 등록 요청 DTO
 */
public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName
) {} 