package com.boardly.features.board.presentation.response;

/**
 * 카드 담당자 응답 DTO
 * 
 * @param userId    사용자 ID
 * @param firstName 이름
 * @param lastName  성
 * @param email     이메일
 * 
 * @since 1.0.0
 */
public record CardAssigneeResponse(
        String userId,
        String firstName,
        String lastName,
        String email) {

    /**
     * 사용자 정보로 CardAssigneeResponse를 생성합니다.
     * 
     * @param userId    사용자 ID
     * @param firstName 이름
     * @param lastName  성
     * @param email     이메일
     * @return CardAssigneeResponse 객체
     */
    public static CardAssigneeResponse of(String userId, String firstName, String lastName, String email) {
        return new CardAssigneeResponse(userId, firstName, lastName, email);
    }
}