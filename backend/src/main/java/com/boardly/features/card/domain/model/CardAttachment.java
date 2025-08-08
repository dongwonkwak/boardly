package com.boardly.features.card.domain.model;

import java.time.Instant;

import com.boardly.features.attachment.domain.model.AttachmentId;
import com.boardly.features.user.domain.model.User;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 첨부파일 도메인 모델
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CardAttachment extends BaseEntity {

    private AttachmentId attachmentId;
    private String fileName;
    private long fileSize;
    private String mimeType;
    private Instant uploadedAt;
    private User uploadedBy;
    private String downloadUrl;

    /**
     * 카드 첨부파일 생성
     */
    public static CardAttachment of(
            AttachmentId attachmentId,
            String fileName,
            long fileSize,
            String mimeType,
            Instant uploadedAt,
            User uploadedBy,
            String downloadUrl) {

        return CardAttachment.builder()
                .attachmentId(attachmentId)
                .fileName(fileName)
                .fileSize(fileSize)
                .mimeType(mimeType)
                .uploadedAt(uploadedAt)
                .uploadedBy(uploadedBy)
                .downloadUrl(downloadUrl)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CardAttachment that = (CardAttachment) obj;
        return attachmentId != null && attachmentId.equals(that.attachmentId);
    }

    @Override
    public int hashCode() {
        return attachmentId != null ? attachmentId.hashCode() : 0;
    }
}
