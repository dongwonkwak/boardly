package com.boardly.infrastructure.adapters.out.persistence.comment;

import org.springframework.stereotype.Component;

import com.boardly.features.comment.domain.Comment;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.CommentId;
import com.boardly.shared.common.value.UserId;

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
