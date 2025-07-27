package com.boardly.features.comment.domain.model;

import java.time.Instant;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    private CommentId commentId;
    private CardId cardId;
    private UserId authorId;
    private String content;
    private boolean edited;

    @Builder
    private Comment(CommentId commentId, CardId cardId, UserId authorId, String content, boolean edited,
            Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
        this.commentId = commentId;
        this.cardId = cardId;
        this.authorId = authorId;
        this.content = content.trim();
        this.edited = edited;
    }

    /**
     * 새 댓글 생성 (팩토리 메서드)
     */
    public static Comment create(CardId cardId, UserId authorId, String content) {
        return Comment.builder()
                .commentId(new CommentId())
                .cardId(cardId)
                .authorId(authorId)
                .content(content)
                .edited(false)
                .build();
    }

    /**
     * 기존 댓글 복원 (리포지토리용)
     */
    public static Comment restore(CommentId commentId, CardId cardId, UserId authorId,
            String content, boolean edited,
            Instant createdAt, Instant updatedAt) {
        return Comment.builder()
                .commentId(commentId)
                .cardId(cardId)
                .authorId(authorId)
                .content(content)
                .edited(edited)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * 댓글 내용 수정
     */
    public void updateContent(String newContent) {
        this.content = newContent.trim();
        this.edited = true;
        markAsUpdated();
    }

    /**
     * 댓글 작성자인지 확인
     */
    public boolean isAuthor(UserId userId) {
        return this.authorId.equals(userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Comment other = (Comment) obj;
        return commentId != null && commentId.equals(other.commentId);
    }

    @Override
    public int hashCode() {
        return commentId != null ? commentId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("Comment{commentId='%s', cardId='%s', authorId='%s', edited=%s}",
                commentId, cardId, authorId, edited);
    }
}
