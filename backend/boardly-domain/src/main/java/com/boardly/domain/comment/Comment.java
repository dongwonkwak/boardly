package com.boardly.domain.comment;

import com.boardly.domain.card.CardId;
import com.boardly.domain.user.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 댓글 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    private CommentId id;
    private String content;
    private CardId cardId;
    private UserId authorId;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    public Comment(CommentId id, String content, CardId cardId, UserId authorId,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.content = content;
        this.cardId = cardId;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 댓글 생성 팩토리 메서드
     */
    public static Comment create(String content, CardId cardId, UserId authorId) {
        return Comment.builder()
                .id(CommentId.generate())
                .content(content)
                .cardId(cardId)
                .authorId(authorId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 댓글 내용 업데이트
     */
    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = Instant.now();
    }

    /**
     * 댓글 작성자인지 확인
     */
    public boolean isAuthor(UserId userId) {
        return this.authorId.equals(userId);
    }
}
