package com.boardly.features.user.application.dto;

/**
 * 사용자 이름 정보 DTO
 */
public record UserNameDto(String firstName, String lastName) {

    /**
     * 기본 사용자 이름 DTO 생성
     */
    public static UserNameDto defaultUser() {
        return new UserNameDto("사용자", "이름");
    }
}