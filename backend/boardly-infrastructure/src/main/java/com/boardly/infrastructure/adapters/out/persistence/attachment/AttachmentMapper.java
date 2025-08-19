package com.boardly.infrastructure.adapters.out.persistence.attachment;

import com.boardly.features.attachment.domain.Attachment;
import com.boardly.shared.common.value.AttachmentId;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;
import org.springframework.stereotype.Component;

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
            entity.getMimeType(),
            entity.getFileSize(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
