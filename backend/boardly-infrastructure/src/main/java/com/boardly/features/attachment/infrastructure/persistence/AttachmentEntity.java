package com.boardly.infrastructure.adapters.out.persistence.attachment;

import com.boardly.features.attachment.domain.Attachment;
import com.boardly.shared.common.value.AttachmentId;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/**
 * 첨부파일 JPA 엔티티
 */
@Entity
@Table(
    name = "attachments",
    indexes = {
        @Index(name = "idx_attachment_card_id", columnList = "card_id"),
        @Index(name = "idx_attachment_uploader_id", columnList = "uploader_id"),
        @Index(name = "idx_attachment_created_at", columnList = "created_at"),
    }
)
public class AttachmentEntity {

    @Id
    @Column(name = "attachment_id", nullable = false, length = 50)
    private String attachmentId;

    @Column(name = "card_id", nullable = false, length = 50)
    private String cardId;

    @Column(name = "uploader_id", nullable = false, length = 50)
    private String uploaderId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    protected AttachmentEntity() {}

    /**
     * 도메인 객체로부터 엔티티 생성
     */
    public static AttachmentEntity from(Attachment attachment) {
        AttachmentEntity entity = new AttachmentEntity();
        entity.attachmentId = attachment.getAttachmentId().getId();
        entity.cardId = attachment.getCardId().getId();
        entity.uploaderId = attachment.getUploaderId().getId();
        entity.fileName = attachment.getFileName();
        entity.originalName = attachment.getOriginalName();
        entity.fileUrl = attachment.getFileUrl();
        entity.mimeType = attachment.getMimeType();
        entity.fileSize = attachment.getFileSize();
        entity.createdAt = attachment.getCreatedAt();
        entity.updatedAt = attachment.getUpdatedAt();
        return entity;
    }

    /**
     * 엔티티를 도메인 객체로 변환
     */
    public Attachment toDomainEntity() {
        return Attachment.restore(
            new AttachmentId(attachmentId),
            new CardId(cardId),
            new UserId(uploaderId),
            fileName,
            originalName,
            fileUrl,
            mimeType,
            fileSize,
            createdAt,
            updatedAt
        );
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(Attachment attachment) {
        this.fileName = attachment.getFileName();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setFileType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AttachmentEntity that = (AttachmentEntity) obj;
        return attachmentId != null && attachmentId.equals(that.attachmentId);
    }

    @Override
    public int hashCode() {
        return attachmentId != null ? attachmentId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format(
            "AttachmentEntity{attachmentId='%s', cardId='%s', fileName='%s', fileSize=%d}",
            attachmentId,
            cardId,
            fileName,
            fileSize
        );
    }
}
