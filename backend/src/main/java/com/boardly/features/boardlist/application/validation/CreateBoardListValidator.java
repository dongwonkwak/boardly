package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateBoardListValidator {
    
    private final ValidationMessageResolver messageResolver;
    
    public ValidationResult<CreateBoardListCommand> validate(CreateBoardListCommand command) {
        return getValidator().validate(command);
    }
    
    private Validator<CreateBoardListCommand> getValidator() {
        return Validator.combine(
            titleValidator(),
            descriptionValidator(),
            boardIdValidator(),
            userIdValidator(),
            colorValidator()
        );
    }
    
    private Validator<CreateBoardListCommand> titleValidator() {
        return Validator.chain(
            Validator.fieldWithMessage(
                CreateBoardListCommand::title,
                title -> title != null && !title.trim().isEmpty(),
                "title",
                "validation.boardlist.title.required",
                messageResolver
            ),
            Validator.fieldWithMessage(
                CreateBoardListCommand::title,
                title -> title == null || title.length() <= CommonValidationRules.TITLE_MAX_LENGTH,
                "title",
                "validation.boardlist.title.max.length",
                messageResolver
            ),
            Validator.fieldWithMessage(
                CreateBoardListCommand::title,
                title -> title == null || !CommonValidationRules.HTML_TAG_PATTERN.matcher(title).find(),
                "title",
                "validation.boardlist.title.invalid",
                messageResolver
            )
        );
    }
    
    private Validator<CreateBoardListCommand> descriptionValidator() {
        return Validator.chain(
            Validator.fieldWithMessage(
                CreateBoardListCommand::description,
                description -> description == null || description.length() <= CommonValidationRules.DESCRIPTION_MAX_LENGTH,
                "description",
                "validation.boardlist.description.max.length",
                messageResolver
            ),
            Validator.fieldWithMessage(
                CreateBoardListCommand::description,
                description -> description == null || !CommonValidationRules.HTML_TAG_PATTERN.matcher(description).find(),
                "description",
                "validation.boardlist.description.invalid",
                messageResolver
            )
        );
    }
    
    private Validator<CreateBoardListCommand> boardIdValidator() {
        return Validator.fieldWithMessage(
            CreateBoardListCommand::boardId,
            boardId -> boardId != null,
            "boardId",
            "validation.boardlist.boardId.required",
            messageResolver
        );
    }
    
    private Validator<CreateBoardListCommand> userIdValidator() {
        return Validator.fieldWithMessage(
            CreateBoardListCommand::userId,
            userId -> userId != null,
            "userId",
            "validation.boardlist.userId.required",
            messageResolver
        );
    }
    
    private Validator<CreateBoardListCommand> colorValidator() {
        return Validator.chain(
            Validator.fieldWithMessage(
                CreateBoardListCommand::color,
                color -> color != null,
                "color",
                "validation.boardlist.color.required",
                messageResolver
            ),
            Validator.fieldWithMessage(
                CreateBoardListCommand::color,
                color -> color == null || ListColor.isValidColor(color.color()),
                "color",
                "validation.boardlist.color.invalid",
                messageResolver
            )
        );
    }
    

} 