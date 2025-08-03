package com.boardly.features.board.presentation.response;

import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.user.domain.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;

/**
 * 보드 멤버 응답 DTO
 * 
 * @param userId       사용자 ID
 * @param firstName    이름
 * @param lastName     성
 * @param email        이메일
 * @param role         역할
 * @param permissions  권한 목록
 * @param joinedAt     참여 시간
 * @param lastActiveAt 마지막 활동 시간
 * @param isActive     활성 상태
 * 
 * @since 1.0.0
 */
public record BoardMemberResponse(
        String userId,
        String firstName,
        String lastName,
        String email,
        String role,
        List<String> permissions,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant joinedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant lastActiveAt,
        boolean isActive) {

    /**
     * BoardMember와 User 도메인 모델을 BoardMemberResponse로 변환합니다.
     * 
     * @param boardMember 보드 멤버 정보
     * @param user        사용자 정보
     * @return BoardMemberResponse 객체
     */
    public static BoardMemberResponse from(BoardMember boardMember, User user) {
        return new BoardMemberResponse(
                boardMember.getUserId().getId(),
                user.getUserProfile().firstName(),
                user.getUserProfile().lastName(),
                user.getEmail(),
                boardMember.getRole().name().toLowerCase(),
                getPermissions(boardMember.getRole()),
                boardMember.getCreatedAt(),
                boardMember.getUpdatedAt(), // lastActiveAt은 별도 필드가 없으므로 updatedAt 사용
                boardMember.isActive());
    }

    /**
     * 역할에 따른 권한 목록을 반환합니다.
     */
    private static List<String> getPermissions(com.boardly.features.board.domain.model.BoardRole role) {
        return switch (role) {
            case OWNER -> List.of("all");
            case ADMIN -> List.of("edit", "delete", "invite");
            case EDITOR -> List.of("edit");
            case VIEWER -> List.of("read");
        };
    }
}