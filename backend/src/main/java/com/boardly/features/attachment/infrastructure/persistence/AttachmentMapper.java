package com.boardly.features.attachment.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.boardly.features.attachment.domain.model.Attachment;
import com.boardly.features.attachment.domain.model.AttachmentId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;

@Component
public class AttachmentMapper {
    /**
     * 도메인 객체를 엔티티로 변환
     */
    public AttachmentEntity toEntity(Attachment attachment) {
        return AttachmentEntity.from(attachment);
    }

    /**
     * 엔티티를 도메인 객체로 변환
     */
    public Attachment toDomain(AttachmentEntity entity) {
        return Attachment.restore(
                new AttachmentId(entity.getAttachmentId()),
                new CardId(entity.getCardId()),
                new UserId(entity.getUploaderId()),
                entity.getFileName(),
                entity.getOriginalName(),
                entity.getFileUrl(),
                entity.getFileType(),
                entity.getFileSize(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
