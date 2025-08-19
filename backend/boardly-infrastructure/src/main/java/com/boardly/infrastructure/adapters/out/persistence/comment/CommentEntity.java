package com.boardly.infrastructure.adapters.out.persistence.comment;

import java.time.Instant;

import com.boardly.features.comment.domain.Comment;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.CommentId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 JPA 엔티티
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_card_id", columnList = "card_id"),
        @Index(name = "idx_comment_author_id", columnList = "author_id"),
        @Index(name = "idx_comment_created_at", columnList = "created_at"),
        @Index(name = "idx_comments_workspace_id", columnList = "workspace_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentEntity {

    @Id
    @Column(name = "comment_id", nullable = false, length = 50)
    private String commentId;

    @Column(name = "card_id", nullable = false, length = 50)
    private String cardId;

    @Column(name = "author_id", nullable = false, length = 50)
    private String authorId;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    // 멀티테넌시를 위한 워크스페이스 식별자 (schema.md 반영)
    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "edited", nullable = false, columnDefinition = "boolean default false")
    private boolean edited = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private CommentEntity(
            String commentId,
            String cardId,
            String authorId,
            String content,
            String workspaceId,
            boolean edited,
            Instant createdAt,
            Instant updatedAt) {
        this.commentId = commentId;
        this.cardId = cardId;
        this.authorId = authorId;
        this.content = content;
        this.workspaceId = workspaceId;
        this.edited = edited;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 도메인 객체로부터 엔티티 생성
     */
    public static CommentEntity from(Comment comment) {
        return CommentEntity.builder()
                .commentId(comment.getCommentId().getId())
                .cardId(comment.getCardId().getId())
                .authorId(comment.getAuthorId().getId())
                .content(comment.getContent())
                .workspaceId(comment.getWorkspaceId().getId())
                .edited(comment.isEdited())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티를 도메인 객체로 변환
     */
    public Comment toDomainEntity() {
        return Comment.restore(
                new CommentId(commentId),
                new CardId(cardId),
                new WorkspaceId(workspaceId),
                new UserId(authorId),
                content,
                edited,
                createdAt,
                updatedAt);
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(Comment comment) {
        this.content = comment.getContent();
        this.workspaceId = comment.getWorkspaceId().getId();
        this.edited = comment.isEdited();
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CommentEntity that = (CommentEntity) obj;
        return commentId != null && commentId.equals(that.commentId);
    }

    @Override
    public int hashCode() {
        return commentId != null ? commentId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("CommentEntity{commentId='%s', cardId='%s', authorId='%s', workspaceId='%s', edited=%s}",
                commentId, cardId, authorId, workspaceId, edited);
    }
}
