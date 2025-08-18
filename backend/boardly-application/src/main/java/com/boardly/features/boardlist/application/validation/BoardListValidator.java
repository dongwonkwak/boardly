package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.command.CreateBoardListCommand;
import com.boardly.features.boardlist.application.command.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.query.GetBoardListsQuery;
import com.boardly.features.boardlist.application.command.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.command.UpdateBoardListPositionCommand;
import com.boardly.application.validation.CommonValidationRules;
import com.boardly.shared.common.value.ListColor;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import com.boardly.shared.validation.Validator;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardListValidator {

    private final CommonValidationRules commonValidationRules;
    private final MessageResolver messageResolver;

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    public ValidationResult<CreateBoardListCommand> validateCreateBoardList(CreateBoardListCommand command) {
        return getCreateBoardListValidator().validate(command);
    }

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

    public ValidationResult<DeleteBoardListCommand> validateDeleteBoardList(DeleteBoardListCommand command) {
        return Validator.combine(
                listIdValidator(DeleteBoardListCommand::listId),
                userIdValidator(DeleteBoardListCommand::userId)
        ).validate(command);
    }

    public ValidationResult<GetBoardListsQuery> validateGetBoardLists(GetBoardListsQuery command) {
        return Validator.combine(
                boardIdValidator(GetBoardListsQuery::boardId),
                userIdValidator(GetBoardListsQuery::userId)
        ).validate(command);
    }

    public ValidationResult<UpdateBoardListCommand> validateUpdateBoardList(UpdateBoardListCommand command) {
        return Validator.combine(
                listIdValidator(UpdateBoardListCommand::listId),
                userIdValidator(UpdateBoardListCommand::userId),
                titleValidator(),
                descriptionValidator(),
                colorValidator()
        ).validate(command);
    }

    public ValidationResult<UpdateBoardListPositionCommand> validateUpdateBoardListPosition(
            UpdateBoardListPositionCommand command) {
        return Validator.combine(
                listIdValidator(UpdateBoardListPositionCommand::listId),
                userIdValidator(UpdateBoardListPositionCommand::userId),
                newPositionValidator()
        ).validate(command);
    }

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
        return title.matches("^[a-zA-Z0-9가-힣\\s\\-_.,!?()]*$");
    }
}
