package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteBoardListValidator {
    
    private final ValidationMessageResolver messageResolver;
    
    public ValidationResult<DeleteBoardListCommand> validate(DeleteBoardListCommand command) {
        return getValidator().validate(command);
    }
    
    private Validator<DeleteBoardListCommand> getValidator() {
        return Validator.combine(
            listIdValidator(),
            userIdValidator()
        );
    }
    
    private Validator<DeleteBoardListCommand> listIdValidator() {
        return Validator.fieldWithMessage(
            DeleteBoardListCommand::listId,
            listId -> listId != null,
            "listId",
            "validation.boardlist.listId.required",
            messageResolver
        );
    }
    
    private Validator<DeleteBoardListCommand> userIdValidator() {
        return Validator.fieldWithMessage(
            DeleteBoardListCommand::userId,
            userId -> userId != null,
            "userId",
            "validation.boardlist.userId.required",
            messageResolver
        );
    }
} 