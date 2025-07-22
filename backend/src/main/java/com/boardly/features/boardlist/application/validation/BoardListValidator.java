package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class BoardListValidator {

    private final CommonValidationRules commonValidationRules;
    private final ValidationMessageResolver messageResolver;

    // 상수 정의
    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    // ==================== Create BoardList Validation ====================

    /**
     * 보드 리스트 생성 검증
     */
    public ValidationResult<CreateBoardListCommand> validateCreateBoardList(CreateBoardListCommand command) {
        return getCreateBoardListValidator().validate(command);
    }

    /**
     * 보드 리스트 생성 검증 (기존 테스트 호환성)
     */
    public ValidationResult<CreateBoardListCommand> validate(CreateBoardListCommand command) {
        return validateCreateBoardList(command);
    }

    private Validator<CreateBoardListCommand> getCreateBoardListValidator() {
        return Validator.combine(
                commonValidationRules.titleComplete(CreateBoardListCommand::title),
                commonValidationRules.descriptionComplete(CreateBoardListCommand::description),
                commonValidationRules.boardIdRequired(CreateBoardListCommand::boardId),
                commonValidationRules.userIdRequired(CreateBoardListCommand::userId),
                commonValidationRules.listColorRequired(CreateBoardListCommand::color));
    }

    // ==================== Delete BoardList Validation ====================

    /**
     * 보드 리스트 삭제 검증
     */
    public ValidationResult<DeleteBoardListCommand> validateDeleteBoardList(DeleteBoardListCommand command) {
        return getDeleteBoardListValidator().validate(command);
    }

    private Validator<DeleteBoardListCommand> getDeleteBoardListValidator() {
        return Validator.combine(
                listIdValidator(DeleteBoardListCommand::listId),
                userIdValidator(DeleteBoardListCommand::userId));
    }

    // ==================== Get BoardLists Validation ====================

    /**
     * 보드 리스트 조회 검증
     */
    public ValidationResult<GetBoardListsCommand> validateGetBoardLists(GetBoardListsCommand command) {
        return getGetBoardListsValidator().validate(command);
    }

    private Validator<GetBoardListsCommand> getGetBoardListsValidator() {
        return Validator.combine(
                boardIdValidator(GetBoardListsCommand::boardId),
                userIdValidator(GetBoardListsCommand::userId));
    }

    // ==================== Update BoardList Validation ====================

    /**
     * 보드 리스트 수정 검증
     */
    public ValidationResult<UpdateBoardListCommand> validateUpdateBoardList(UpdateBoardListCommand command) {
        return getUpdateBoardListValidator().validate(command);
    }

    private Validator<UpdateBoardListCommand> getUpdateBoardListValidator() {
        return Validator.combine(
                listIdValidator(UpdateBoardListCommand::listId),
                userIdValidator(UpdateBoardListCommand::userId),
                titleValidator(),
                descriptionValidator(),
                colorValidator());
    }

    // ==================== Update BoardList Position Validation
    // ====================

    /**
     * 보드 리스트 위치 수정 검증
     */
    public ValidationResult<UpdateBoardListPositionCommand> validateUpdateBoardListPosition(
            UpdateBoardListPositionCommand command) {
        return getUpdateBoardListPositionValidator().validate(command);
    }

    private Validator<UpdateBoardListPositionCommand> getUpdateBoardListPositionValidator() {
        return Validator.combine(
                listIdValidator(UpdateBoardListPositionCommand::listId),
                userIdValidator(UpdateBoardListPositionCommand::userId),
                newPositionValidator());
    }

    // ==================== Common Validators ====================

    private <T> Validator<T> listIdValidator(java.util.function.Function<T, Object> listIdExtractor) {
        return Validator.fieldWithMessage(
                listIdExtractor,
                listId -> listId != null,
                "listId",
                "validation.boardlist.listId.required",
                messageResolver);
    }

    private <T> Validator<T> userIdValidator(java.util.function.Function<T, Object> userIdExtractor) {
        return Validator.fieldWithMessage(
                userIdExtractor,
                userId -> userId != null,
                "userId",
                "validation.boardlist.userId.required",
                messageResolver);
    }

    private <T> Validator<T> boardIdValidator(java.util.function.Function<T, Object> boardIdExtractor) {
        return Validator.fieldWithMessage(
                boardIdExtractor,
                boardId -> boardId != null,
                "boardId",
                "validation.boardlist.boardId.required",
                messageResolver);
    }

    private Validator<UpdateBoardListCommand> titleValidator() {
        return Validator.chain(
                Validator.fieldWithMessage(
                        UpdateBoardListCommand::title,
                        title -> title != null && !title.trim().isEmpty(),
                        "title",
                        "validation.boardlist.title.required",
                        messageResolver),
                Validator.fieldWithMessage(
                        UpdateBoardListCommand::title,
                        title -> title == null || title.length() <= TITLE_MAX_LENGTH,
                        "title",
                        "validation.boardlist.title.max.length",
                        messageResolver),
                Validator.fieldWithMessage(
                        UpdateBoardListCommand::title,
                        title -> title == null || isTitleValid(title),
                        "title",
                        "validation.boardlist.title.invalid",
                        messageResolver));
    }

    private Validator<UpdateBoardListCommand> descriptionValidator() {
        return Validator.chain(
                Validator.fieldWithMessage(
                        UpdateBoardListCommand::description,
                        description -> description == null || description.length() <= DESCRIPTION_MAX_LENGTH,
                        "description",
                        "validation.boardlist.description.max.length",
                        messageResolver),
                Validator.fieldWithMessage(
                        UpdateBoardListCommand::description,
                        description -> description == null || !isHtmlTag(description),
                        "description",
                        "validation.boardlist.description.invalid",
                        messageResolver));
    }

    private Validator<UpdateBoardListCommand> colorValidator() {
        return Validator.fieldWithMessage(
                UpdateBoardListCommand::color,
                color -> color == null || ListColor.isValidColor(color.color()),
                "color",
                "validation.boardlist.color.invalid",
                messageResolver);
    }

    private Validator<UpdateBoardListPositionCommand> newPositionValidator() {
        return Validator.fieldWithMessage(
                UpdateBoardListPositionCommand::newPosition,
                position -> position >= 0,
                "newPosition",
                "validation.boardlist.position.invalid",
                messageResolver);
    }

    // ==================== Helper Methods ====================

    private boolean isHtmlTag(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return HTML_TAG_PATTERN.matcher(text).find();
    }

    private boolean isTitleValid(String title) {
        if (isHtmlTag(title)) {
            return false;
        }
        // 제목은 한글, 영문, 숫자, 공백, 특수문자 일부만 허용
        return title.matches("^[a-zA-Z0-9가-힣\\s\\-_.,!?()]*$");
    }
}