package com.boardly.features.card.domain.model;

import java.time.Instant;

import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.user.domain.model.User;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 댓글 도메인 모델
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CardComment extends BaseEntity {

    private CommentId commentId;
    private String content;
    private Instant createdAt;
    private User createdBy;
    private Instant updatedAt;
    private boolean isEdited;

    /**
     * 카드 댓글 생성
     */
    public static CardComment of(
            CommentId commentId,
            String content,
            Instant createdAt,
            User createdBy,
            Instant updatedAt,
            boolean isEdited) {

        return CardComment.builder()
                .commentId(commentId)
                .content(content)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .isEdited(isEdited)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CardComment that = (CardComment) obj;
        return commentId != null && commentId.equals(that.commentId);
    }

    @Override
    public int hashCode() {
        return commentId != null ? commentId.hashCode() : 0;
    }
}
