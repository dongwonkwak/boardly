package com.boardly.api.adapters.in.rest.user.request;

/**
 * 사용자 업데이트 요청 DTO
 */
public record UpdateUserRequest(
        String firstName,
        String lastName
) {} 