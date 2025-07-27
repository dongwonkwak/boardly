package com.boardly.features.comment.infrastructure.persistence;

import java.time.Instant;

import com.boardly.features.comment.domain.model.Comment;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.user.domain.model.UserId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * 댓글 JPA 엔티티
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_card_id", columnList = "card_id"),
        @Index(name = "idx_comment_author_id", columnList = "author_id"),
        @Index(name = "idx_comment_created_at", columnList = "created_at")
})
public class CommentEntity {

    @Id
    @Column(name = "comment_id", nullable = false)
    private String commentId;

    @Column(name = "card_id", nullable = false)
    private String cardId;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "edited", nullable = false, columnDefinition = "boolean default false")
    private boolean edited = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // 기본 생성자 (JPA 필수)
    protected CommentEntity() {
    }

    /**
     * 도메인 객체로부터 엔티티 생성
     */
    public static CommentEntity from(Comment comment) {
        CommentEntity entity = new CommentEntity();
        entity.commentId = comment.getCommentId().getId();
        entity.cardId = comment.getCardId().getId();
        entity.authorId = comment.getAuthorId().getId();
        entity.content = comment.getContent();
        entity.edited = comment.isEdited();
        entity.createdAt = comment.getCreatedAt();
        entity.updatedAt = comment.getUpdatedAt();
        return entity;
    }

    /**
     * 엔티티를 도메인 객체로 변환
     */
    public Comment toDomainEntity() {
        return Comment.restore(
                new CommentId(commentId),
                new CardId(cardId),
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
        this.edited = comment.isEdited();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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
        return String.format("CommentEntity{commentId='%s', cardId='%s', authorId='%s', edited=%s}",
                commentId, cardId, authorId, edited);
    }
}
