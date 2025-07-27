package com.boardly.features.label.application.validation;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.boardly.features.label.application.port.input.CreateLabelCommand;
import com.boardly.features.label.application.port.input.DeleteLabelCommand;
import com.boardly.features.label.application.port.input.UpdateLabelCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LabelValidator {

    private final CommonValidationRules commonValidationRules;
    private final ValidationMessageResolver messageResolver;

    // 상수 정의
    private static final int LABEL_NAME_MAX_LENGTH = 50;
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    // ==================== Create Label Validation ====================

    /**
     * 라벨 생성 검증
     */
    public ValidationResult<CreateLabelCommand> validateCreateLabel(CreateLabelCommand command) {
        return getCreateLabelValidator().validate(command);
    }

    /**
     * 라벨 생성 검증 (기존 테스트 호환성)
     */
    public ValidationResult<CreateLabelCommand> validate(CreateLabelCommand command) {
        return validateCreateLabel(command);
    }

    private Validator<CreateLabelCommand> getCreateLabelValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(CreateLabelCommand::boardId),
                requesterIdRequired(),
                labelNameValidator(),
                labelColorValidator());
    }

    // ==================== Update Label Validation ====================

    /**
     * 라벨 수정 검증
     */
    public ValidationResult<UpdateLabelCommand> validateUpdateLabel(UpdateLabelCommand command) {
        return getUpdateLabelValidator().validate(command);
    }

    /**
     * 라벨 수정 검증 (기존 테스트 호환성)
     */
    public ValidationResult<UpdateLabelCommand> validate(UpdateLabelCommand command) {
        return validateUpdateLabel(command);
    }

    private Validator<UpdateLabelCommand> getUpdateLabelValidator() {
        return Validator.combine(
                updateLabelIdRequired(),
                updateUserIdRequired(),
                updateLabelNameValidator(),
                updateLabelColorValidator());
    }

    // ==================== Delete Label Validation ====================

    /**
     * 라벨 삭제 검증
     */
    public ValidationResult<DeleteLabelCommand> validateDeleteLabel(DeleteLabelCommand command) {
        return getDeleteLabelValidator().validate(command);
    }

    /**
     * 라벨 삭제 검증 (기존 테스트 호환성)
     */
    public ValidationResult<DeleteLabelCommand> validate(DeleteLabelCommand command) {
        return validateDeleteLabel(command);
    }

    private Validator<DeleteLabelCommand> getDeleteLabelValidator() {
        return Validator.combine(
                labelIdRequired(),
                userIdRequired());
    }

    // ==================== Field Validators ====================

    /**
     * 라벨 이름 검증
     */
    private Validator<CreateLabelCommand> labelNameValidator() {
        return Validator.chain(
                labelNameRequired(),
                labelNameMaxLength(),
                labelNamePattern());
    }

    /**
     * 라벨 이름 필수 검증
     */
    private Validator<CreateLabelCommand> labelNameRequired() {
        return Validator.fieldWithMessage(
                CreateLabelCommand::name,
                name -> name != null && !name.trim().isEmpty(),
                "name",
                "validation.label.name.required",
                messageResolver);
    }

    /**
     * 라벨 이름 최대 길이 검증
     */
    private Validator<CreateLabelCommand> labelNameMaxLength() {
        return Validator.fieldWithMessage(
                CreateLabelCommand::name,
                name -> name == null || name.length() <= LABEL_NAME_MAX_LENGTH,
                "name",
                "validation.label.name.max.length",
                messageResolver,
                LABEL_NAME_MAX_LENGTH);
    }

    /**
     * 라벨 이름 패턴 검증 (HTML 태그 방지)
     */
    private Validator<CreateLabelCommand> labelNamePattern() {
        return Validator.fieldWithMessage(
                CreateLabelCommand::name,
                name -> name == null || !containsHtmlTags(name),
                "name",
                "validation.label.name.invalid",
                messageResolver);
    }

    /**
     * 라벨 색상 검증
     */
    private Validator<CreateLabelCommand> labelColorValidator() {
        return Validator.chain(
                labelColorRequired(),
                labelColorPattern());
    }

    /**
     * 라벨 색상 필수 검증
     */
    private Validator<CreateLabelCommand> labelColorRequired() {
        return Validator.fieldWithMessage(
                CreateLabelCommand::color,
                color -> color != null && !color.trim().isEmpty(),
                "color",
                "validation.label.color.required",
                messageResolver);
    }

    /**
     * 라벨 색상 hex 패턴 검증
     */
    private Validator<CreateLabelCommand> labelColorPattern() {
        return Validator.fieldWithMessage(
                CreateLabelCommand::color,
                color -> color == null || HEX_COLOR_PATTERN.matcher(color).matches(),
                "color",
                "validation.label.color.invalid",
                messageResolver);
    }

    /**
     * 라벨 ID 필수 검증
     */
    private Validator<DeleteLabelCommand> labelIdRequired() {
        return Validator.fieldWithMessage(
                DeleteLabelCommand::labelId,
                labelId -> labelId != null,
                "labelId",
                "validation.label.id.required",
                messageResolver);
    }

    /**
     * 사용자 ID 필수 검증
     */
    private Validator<DeleteLabelCommand> userIdRequired() {
        return Validator.fieldWithMessage(
                DeleteLabelCommand::userId,
                userId -> userId != null,
                "userId",
                "validation.label.userId.required",
                messageResolver);
    }

    /**
     * 요청자 ID 필수 검증 (Create)
     */
    private Validator<CreateLabelCommand> requesterIdRequired() {
        return Validator.fieldWithMessage(
                CreateLabelCommand::requesterId,
                requesterId -> requesterId != null,
                "requesterId",
                "validation.label.userId.required",
                messageResolver);
    }

    // ==================== Update Label Field Validators ====================

    /**
     * 라벨 ID 필수 검증 (Update)
     */
    private Validator<UpdateLabelCommand> updateLabelIdRequired() {
        return Validator.fieldWithMessage(
                UpdateLabelCommand::labelId,
                labelId -> labelId != null,
                "labelId",
                "validation.label.id.required",
                messageResolver);
    }

    /**
     * 사용자 ID 필수 검증 (Update)
     */
    private Validator<UpdateLabelCommand> updateUserIdRequired() {
        return Validator.fieldWithMessage(
                UpdateLabelCommand::requesterId,
                userId -> userId != null,
                "userId",
                "validation.label.userId.required",
                messageResolver);
    }

    /**
     * 라벨 이름 검증 (Update)
     */
    private Validator<UpdateLabelCommand> updateLabelNameValidator() {
        return Validator.chain(
                updateLabelNameMaxLength(),
                updateLabelNamePattern());
    }

    /**
     * 라벨 이름 최대 길이 검증 (Update)
     */
    private Validator<UpdateLabelCommand> updateLabelNameMaxLength() {
        return Validator.fieldWithMessage(
                UpdateLabelCommand::name,
                name -> name == null || name.length() <= LABEL_NAME_MAX_LENGTH,
                "name",
                "validation.label.name.max.length",
                messageResolver,
                LABEL_NAME_MAX_LENGTH);
    }

    /**
     * 라벨 이름 패턴 검증 (Update)
     */
    private Validator<UpdateLabelCommand> updateLabelNamePattern() {
        return Validator.fieldWithMessage(
                UpdateLabelCommand::name,
                name -> name == null || name.isEmpty() || !containsHtmlTags(name),
                "name",
                "validation.label.name.invalid",
                messageResolver);
    }

    /**
     * 라벨 색상 검증 (Update)
     */
    private Validator<UpdateLabelCommand> updateLabelColorValidator() {
        return Validator.chain(
                updateLabelColorPattern());
    }

    /**
     * 라벨 색상 hex 패턴 검증 (Update)
     */
    private Validator<UpdateLabelCommand> updateLabelColorPattern() {
        return Validator.fieldWithMessage(
                UpdateLabelCommand::color,
                color -> color == null || color.isEmpty() || HEX_COLOR_PATTERN.matcher(color).matches(),
                "color",
                "validation.label.color.invalid",
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
        return text.matches(".*<[^>]*>.*");
    }
}