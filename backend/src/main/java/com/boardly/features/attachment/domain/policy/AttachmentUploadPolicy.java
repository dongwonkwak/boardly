package com.boardly.features.attachment.domain.policy;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.attachment.domain.repository.AttachmentRepository;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 첨부파일 업로드 정책
 * 
 * <p>
 * 첨부파일 업로드와 관련된 비즈니스 규칙을 정의하고 검증합니다.
 * 파일 크기 제한, 카드당 첨부파일 개수 제한, 파일명 길이 등의 정책을 관리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentUploadPolicy {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentPolicyConfig policyConfig;

    /**
     * 첨부파일 업로드가 가능한지 검증합니다.
     */
    public Either<Failure, Void> canUploadAttachment(CardId cardId, MultipartFile file) {
        log.debug("첨부파일 업로드 정책 검증 시작: cardId={}, fileName={}, fileSize={}",
                cardId.getId(), file.getOriginalFilename(), file.getSize());

        return checkFileNameLength(file)
                .flatMap(v -> checkFileSize(file))
                .flatMap(v -> checkAttachmentCountLimit(cardId))
                .peek(v -> log.debug("첨부파일 업로드 정책 검증 성공: cardId={}, fileName={}",
                        cardId.getId(), file.getOriginalFilename()));
    }

    /**
     * 파일 크기 제한을 확인합니다.
     */
    private Either<Failure, Void> checkFileSize(MultipartFile file) {
        long maxFileSizeBytes = (long) policyConfig.getMaxFileSizeMB() * 1024 * 1024;
        long fileSize = file.getSize();

        log.debug("파일 크기 확인: fileName={}, fileSize={}, maxFileSize={}",
                file.getOriginalFilename(), fileSize, maxFileSizeBytes);

        if (fileSize > maxFileSizeBytes) {
            log.warn("파일 크기 제한 초과: fileName={}, fileSize={}, maxFileSize={}",
                    file.getOriginalFilename(), fileSize, maxFileSizeBytes);
            return Either.left(Failure.ofPermissionDenied(
                    String.format("파일 크기는 최대 %dMB까지 업로드할 수 있습니다. (현재: %.2fMB)",
                            policyConfig.getMaxFileSizeMB(), fileSize / (1024.0 * 1024.0))));
        }

        return Either.right(null);
    }

    /**
     * 카드당 첨부파일 개수 제한을 확인합니다.
     */
    private Either<Failure, Void> checkAttachmentCountLimit(CardId cardId) {
        int currentCount = attachmentRepository.countByCardId(cardId);
        int maxAttachments = policyConfig.getMaxAttachmentsPerCard();

        log.debug("카드당 첨부파일 개수 확인: cardId={}, currentCount={}, maxCount={}",
                cardId.getId(), currentCount, maxAttachments);

        if (currentCount >= maxAttachments) {
            log.warn("카드당 첨부파일 개수 제한 초과: cardId={}, currentCount={}, maxCount={}",
                    cardId.getId(), currentCount, maxAttachments);
            return Either.left(Failure.ofPermissionDenied(
                    String.format("카드당 최대 %d개의 첨부파일만 업로드할 수 있습니다. (현재: %d개)",
                            maxAttachments, currentCount)));
        }

        return Either.right(null);
    }

    /**
     * 파일명 길이 제한을 확인합니다.
     */
    private Either<Failure, Void> checkFileNameLength(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            log.warn("파일명이 null입니다: fileName=null");
            return Either.left(Failure.ofInputError("파일명이 유효하지 않습니다."));
        }

        int maxFileNameLength = policyConfig.getMaxFileNameLength();
        int fileNameLength = fileName.length();

        log.debug("파일명 길이 확인: fileName={}, fileNameLength={}, maxLength={}",
                fileName, fileNameLength, maxFileNameLength);

        if (fileNameLength > maxFileNameLength) {
            log.warn("파일명 길이 제한 초과: fileName={}, fileNameLength={}, maxLength={}",
                    fileName, fileNameLength, maxFileNameLength);
            return Either.left(Failure.ofInputError(
                    String.format("파일명은 최대 %d자까지 입력할 수 있습니다. (현재: %d자)",
                            maxFileNameLength, fileNameLength)));
        }

        return Either.right(null);
    }

    /**
     * 최대 파일 크기를 MB 단위로 반환합니다.
     */
    public int getMaxFileSizeMB() {
        return policyConfig.getMaxFileSizeMB();
    }

    /**
     * 카드당 최대 첨부파일 개수를 반환합니다.
     */
    public int getMaxAttachmentsPerCard() {
        return policyConfig.getMaxAttachmentsPerCard();
    }

    /**
     * 파일명 최대 길이를 반환합니다.
     */
    public int getMaxFileNameLength() {
        return policyConfig.getMaxFileNameLength();
    }

    /**
     * 추가 업로드 가능한 첨부파일 개수를 반환합니다.
     */
    public int getAvailableAttachmentSlots(CardId cardId) {
        int currentCount = attachmentRepository.countByCardId(cardId);
        int maxAttachments = policyConfig.getMaxAttachmentsPerCard();
        return Math.max(0, maxAttachments - currentCount);
    }

    /**
     * 기본 최대 파일 크기를 반환합니다.
     */
    public static int getDefaultMaxFileSizeMB() {
        return AttachmentPolicyConfig.Defaults.MAX_FILE_SIZE_MB;
    }

    /**
     * 기본 카드당 최대 첨부파일 개수를 반환합니다.
     */
    public static int getDefaultMaxAttachmentsPerCard() {
        return AttachmentPolicyConfig.Defaults.MAX_ATTACHMENTS_PER_CARD;
    }

    /**
     * 기본 파일명 최대 길이를 반환합니다.
     */
    public static int getDefaultMaxFileNameLength() {
        return AttachmentPolicyConfig.Defaults.MAX_FILE_NAME_LENGTH;
    }
}