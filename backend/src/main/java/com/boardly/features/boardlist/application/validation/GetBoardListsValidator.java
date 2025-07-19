package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBoardListsValidator {
    
    private final ValidationMessageResolver messageResolver;
    
    public ValidationResult<GetBoardListsCommand> validate(GetBoardListsCommand command) {
        return getValidator().validate(command);
    }
    
    private Validator<GetBoardListsCommand> getValidator() {
        return Validator.combine(
            listIdValidator(),
            userIdValidator()
        );
    }
    
    private Validator<GetBoardListsCommand> listIdValidator() {
        return Validator.fieldWithMessage(
            GetBoardListsCommand::listId,
            listId -> listId != null,
            "listId",
            "validation.boardlist.listId.required",
            messageResolver
        );
    }
    
    private Validator<GetBoardListsCommand> userIdValidator() {
        return Validator.fieldWithMessage(
            GetBoardListsCommand::userId,
            userId -> userId != null,
            "userId",
            "validation.boardlist.userId.required",
            messageResolver
        );
    }
} 