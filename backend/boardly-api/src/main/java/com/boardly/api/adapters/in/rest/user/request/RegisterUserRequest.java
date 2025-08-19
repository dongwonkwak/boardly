package com.boardly.api.adapters.in.rest.user.request;

/**
 * 사용자 등록 요청 DTO
 */
public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName
) {} 