package com.boardly.features.comment.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;

@Component
public class CommentMapper {

    /**
     * 도메인 객체를 엔티티로 변환
     */
    public CommentEntity toEntity(Comment comment) {
        return CommentEntity.from(comment);
    }

    /**
     * 엔티티를 도메인 객체로 변환
     */
    public Comment toDomain(CommentEntity entity) {
        return Comment.restore(
                new CommentId(entity.getCommentId()),
                new CardId(entity.getCardId()),
                new UserId(entity.getAuthorId()),
                entity.getContent(),
                entity.isEdited(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
