package com.boardly.domain.board;

import com.boardly.domain.user.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 보드 즐겨찾기 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardFavorite {

    private String id;
    private UserId userId;
    private BoardId boardId;
    private Instant createdAt;
    private Instant lastAccessedAt;

    @Builder
    public BoardFavorite(String id, UserId userId, BoardId boardId,
            Instant createdAt, Instant lastAccessedAt) {
        this.id = id;
        this.userId = userId;
        this.boardId = boardId;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
    }

    /**
     * 보드 즐겨찾기 생성 팩토리 메서드
     */
    public static BoardFavorite create(UserId userId, BoardId boardId) {
        Instant now = Instant.now();
        return BoardFavorite.builder()
                .id("bf_" + java.util.UUID.randomUUID().toString())
                .userId(userId)
                .boardId(boardId)
                .createdAt(now)
                .lastAccessedAt(now)
                .build();
    }

    /**
     * 최근 접근 시간 업데이트
     */
    public void updateLastAccessedAt() {
        this.lastAccessedAt = Instant.now();
    }

    /**
     * 특정 사용자의 즐겨찾기인지 확인
     */
    public boolean belongsToUser(UserId userId) {
        return this.userId.equals(userId);
    }

    /**
     * 특정 보드의 즐겨찾기인지 확인
     */
    public boolean isForBoard(BoardId boardId) {
        return this.boardId.equals(boardId);
    }
}
