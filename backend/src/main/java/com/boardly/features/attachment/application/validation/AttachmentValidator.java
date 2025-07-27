package com.boardly.features.attachment.application.validation;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.boardly.features.attachment.application.port.input.UploadAttachmentCommand;
import com.boardly.features.attachment.application.port.input.UpdateAttachmentCommand;
import com.boardly.features.attachment.application.port.input.DeleteAttachmentCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

/**
 * 첨부파일 통합 검증기
 * 
 * <p>
 * 모든 첨부파일 관련 Command들의 입력 검증을 담당합니다.
 * UploadAttachmentCommand와 UpdateAttachmentCommand의 검증 로직을 통합하여 관리합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AttachmentValidator {

    private final CommonValidationRules commonValidationRules;
    private final ValidationMessageResolver messageResolver;

    // 상수 정의

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣\\s\\-_.,!?()]+$");

    // 허용되는 파일 타입들
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            // 이미지
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/svg+xml",
            // 문서
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // 텍스트
            "text/plain", "text/csv", "text/html", "text/css", "text/javascript",
            // 압축
            "application/zip", "application/x-rar-compressed", "application/x-7z-compressed",
            // 기타
            "application/json", "application/xml");

    // ==================== Upload Attachment Validation ====================

    /**
     * 첨부파일 업로드 커맨드 검증
     */
    public ValidationResult<UploadAttachmentCommand> validateUpload(UploadAttachmentCommand command) {
        return getUploadValidator().validate(command);
    }

    /**
     * 첨부파일 업로드 검증 (기존 테스트 호환성)
     */
    public ValidationResult<UploadAttachmentCommand> validate(UploadAttachmentCommand command) {
        return validateUpload(command);
    }

    private Validator<UploadAttachmentCommand> getUploadValidator() {
        return Validator.combine(
                // 카드 ID 검증: 필수
                commonValidationRules.cardIdRequired(UploadAttachmentCommand::cardId),
                // 업로더 ID 검증: 필수
                commonValidationRules.userIdRequired(UploadAttachmentCommand::uploaderId),
                // 파일 검증: 필수, 타입, 비어있지 않음
                fileRequired(),
                fileTypeValid(),
                fileNotEmpty());
    }

    // ==================== Update Attachment Validation ====================

    /**
     * 첨부파일 수정 커맨드 검증
     */
    public ValidationResult<UpdateAttachmentCommand> validateUpdate(UpdateAttachmentCommand command) {
        return getUpdateValidator().validate(command);
    }

    /**
     * 첨부파일 수정 검증 (기존 테스트 호환성)
     */
    public ValidationResult<UpdateAttachmentCommand> validate(UpdateAttachmentCommand command) {
        return validateUpdate(command);
    }

    private Validator<UpdateAttachmentCommand> getUpdateValidator() {
        return Validator.combine(
                // 첨부파일 ID 검증: 필수
                attachmentIdRequired(),
                // 파일명 검증: 필수, 패턴
                fileNameRequired(),
                fileNamePattern());
    }

    // ==================== Delete Attachment Validation ====================

    /**
     * 첨부파일 삭제 커맨드 검증
     */
    public ValidationResult<DeleteAttachmentCommand> validateDelete(DeleteAttachmentCommand command) {
        return getDeleteValidator().validate(command);
    }

    /**
     * 첨부파일 삭제 검증 (기존 테스트 호환성)
     */
    public ValidationResult<DeleteAttachmentCommand> validate(DeleteAttachmentCommand command) {
        return validateDelete(command);
    }

    private Validator<DeleteAttachmentCommand> getDeleteValidator() {
        return Validator.combine(
                // 첨부파일 ID 검증: 필수
                deleteAttachmentIdRequired(),
                // 요청자 ID 검증: 필수
                requesterIdRequired());
    }

    // ==================== Field Validators ====================

    /**
     * 첨부파일 ID 필수 검증
     */
    private Validator<UpdateAttachmentCommand> attachmentIdRequired() {
        return Validator.fieldWithMessage(
                UpdateAttachmentCommand::attachmentId,
                id -> id != null,
                "attachmentId",
                "validation.attachment.id.required",
                messageResolver);
    }

    /**
     * 파일 필수 검증
     */
    private Validator<UploadAttachmentCommand> fileRequired() {
        return Validator.fieldWithMessage(
                UploadAttachmentCommand::file,
                file -> file != null,
                "file",
                "validation.attachment.file.required",
                messageResolver);
    }

    /**
     * 파일 타입 검증
     */
    private Validator<UploadAttachmentCommand> fileTypeValid() {
        return Validator.fieldWithMessage(
                UploadAttachmentCommand::file,
                file -> file == null || ALLOWED_FILE_TYPES.contains(file.getContentType()),
                "file",
                "validation.attachment.file.type.not.allowed",
                messageResolver);
    }

    /**
     * 파일 비어있지 않음 검증
     */
    private Validator<UploadAttachmentCommand> fileNotEmpty() {
        return Validator.fieldWithMessage(
                UploadAttachmentCommand::file,
                file -> file == null || !file.isEmpty(),
                "file",
                "validation.attachment.file.empty",
                messageResolver);
    }

    /**
     * 파일명 필수 검증
     */
    private Validator<UpdateAttachmentCommand> fileNameRequired() {
        return Validator.fieldWithMessage(
                UpdateAttachmentCommand::fileName,
                name -> name != null && !name.trim().isEmpty(),
                "fileName",
                "validation.attachment.fileName.required",
                messageResolver);
    }

    /**
     * 파일명 패턴 검증 (HTML 태그 및 특수문자 방지)
     */
    private Validator<UpdateAttachmentCommand> fileNamePattern() {
        return Validator.fieldWithMessage(
                UpdateAttachmentCommand::fileName,
                name -> name == null || (isValidFileName(name) && !containsHtmlTags(name)),
                "fileName",
                "validation.attachment.fileName.invalid",
                messageResolver);
    }

    /**
     * 삭제용 첨부파일 ID 필수 검증
     */
    private Validator<DeleteAttachmentCommand> deleteAttachmentIdRequired() {
        return Validator.fieldWithMessage(
                DeleteAttachmentCommand::attachmentId,
                id -> id != null,
                "attachmentId",
                "validation.attachment.id.required",
                messageResolver);
    }

    /**
     * 요청자 ID 필수 검증
     */
    private Validator<DeleteAttachmentCommand> requesterIdRequired() {
        return Validator.fieldWithMessage(
                DeleteAttachmentCommand::requesterId,
                id -> id != null,
                "requesterId",
                "validation.attachment.uploaderId.required",
                messageResolver);
    }

    // ==================== Helper Methods ====================

    /**
     * HTML 태그 포함 여부 확인
     */
    private boolean containsHtmlTags(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return HTML_TAG_PATTERN.matcher(text).find();
    }

    /**
     * 파일명 유효성 검사
     */
    private boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        return FILE_NAME_PATTERN.matcher(fileName).matches();
    }

    /**
     * 파일 크기를 읽기 쉬운 형태로 반환
     */
    public static String getReadableFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}