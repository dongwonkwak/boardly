package com.boardly.features.attachment.domain;

import java.time.Instant;

import com.boardly.shared.common.value.AttachmentId;
import com.boardly.shared.domain.BaseEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment extends BaseEntity {

    private AttachmentId attachmentId;
    private CardId cardId;
    private UserId uploaderId;
    private String fileName;
    private String originalName;
    private String mimeType;
    private String fileUrl;
    private long fileSize;

    @Builder
    private Attachment(AttachmentId attachmentId, CardId cardId, UserId uploaderId, String fileName,
            String originalName, String mimeType, String fileUrl, long fileSize,
            Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
        this.attachmentId = attachmentId;
        this.cardId = cardId;
        this.uploaderId = uploaderId;
        this.fileName = fileName.trim();
        this.originalName = originalName.trim();
        this.mimeType = mimeType.trim();
        this.fileUrl = fileUrl.trim();
        this.fileSize = fileSize;
    }

    /**
     * 새 첨부파일 생성 (팩토리 메서드)
     */
    public static Attachment create(CardId cardId, UserId uploaderId, String fileName,
            String originalName, String mimeType, String fileUrl, long fileSize) {
        return Attachment.builder()
                .attachmentId(new AttachmentId())
                .cardId(cardId)
                .uploaderId(uploaderId)
                .fileName(fileName)
                .originalName(originalName)
                .mimeType(mimeType)
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .build();
    }

    /**
     * 기존 첨부파일 복원 (리포지토리용)
     */
    public static Attachment restore(AttachmentId attachmentId, CardId cardId, UserId uploaderId, String fileName,
            String originalName, String mimeType, String fileUrl, long fileSize,
            Instant createdAt, Instant updatedAt) {
        return Attachment.builder()
                .attachmentId(attachmentId)
                .cardId(cardId)
                .uploaderId(uploaderId)
                .fileName(fileName)
                .originalName(originalName)
                .mimeType(mimeType)
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * 파일명 변경
     */
    public void updateFileName(String newFileName) {
        this.fileName = newFileName.trim();
        markAsUpdated();
    }

    /**
     * 업로더인지 확인
     */
    public boolean isUploader(UserId userId) {
        return this.uploaderId.equals(userId);
    }

    /**
     * 이미지 파일인지 확인
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * 파일 크기를 읽기 쉬운 형태로 반환
     */
    public String getReadableFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Attachment other = (Attachment) obj;
        return attachmentId != null && attachmentId.equals(other.attachmentId);
    }

    @Override
    public int hashCode() {
        return attachmentId != null ? attachmentId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("Attachment{attachmentId='%s', cardId='%s', fileName='%s', fileSize=%d}",
                attachmentId, cardId, fileName, fileSize);
    }
}
