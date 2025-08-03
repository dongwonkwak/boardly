package com.boardly.features.board.presentation.response;

/**
 * 카드 사용자 응답 DTO
 * 
 * @param userId    사용자 ID
 * @param firstName 이름
 * @param lastName  성
 * 
 * @since 1.0.0
 */
public record CardUserResponse(
        String userId,
        String firstName,
        String lastName) {

    /**
     * 사용자 정보로 CardUserResponse를 생성합니다.
     * 
     * @param userId    사용자 ID
     * @param firstName 이름
     * @param lastName  성
     * @return CardUserResponse 객체
     */
    public static CardUserResponse of(String userId, String firstName, String lastName) {
        return new CardUserResponse(userId, firstName, lastName);
    }
}