package com.boardly.features.board.application.query;

import com.boardly.shared.common.value.UserId;

/**
 * 사용자 보드 목록 조회 쿼리
 * 
 * @param ownerId 보드 소유자 ID
 * @param includeArchived 아카이브된 보드 포함 여부 (기본값: false)
 */
public record GetUserBoardsQuery(
    UserId ownerId,
    boolean includeArchived
) {
    /**
     * 활성 보드만 조회하는 커맨드를 생성합니다.
     */
    public static GetUserBoardsQuery activeOnly(UserId ownerId) {
        return new GetUserBoardsQuery(ownerId, false);
    }

    /**
     * 모든 보드(활성 + 아카이브)를 조회하는 커맨드를 생성합니다.
     */
    public static GetUserBoardsQuery all(UserId ownerId) {
        return new GetUserBoardsQuery(ownerId, true);
    }
} 