package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateBoardListPositionValidator {
    
    private final ValidationMessageResolver messageResolver;
    
    public ValidationResult<UpdateBoardListPositionCommand> validate(UpdateBoardListPositionCommand command) {
        return getValidator().validate(command);
    }
    
    private Validator<UpdateBoardListPositionCommand> getValidator() {
        return Validator.combine(
            listIdValidator(),
            userIdValidator(),
            newPositionValidator()
        );
    }
    
    private Validator<UpdateBoardListPositionCommand> listIdValidator() {
        return Validator.fieldWithMessage(
            UpdateBoardListPositionCommand::listId,
            listId -> listId != null,
            "listId",
            "validation.boardlist.listId.required",
            messageResolver
        );
    }
    
    private Validator<UpdateBoardListPositionCommand> userIdValidator() {
        return Validator.fieldWithMessage(
            UpdateBoardListPositionCommand::userId,
            userId -> userId != null,
            "userId",
            "validation.boardlist.userId.required",
            messageResolver
        );
    }
    
    private Validator<UpdateBoardListPositionCommand> newPositionValidator() {
        return Validator.fieldWithMessage(
            UpdateBoardListPositionCommand::newPosition,
            position -> position >= 0,
            "newPosition",
            "validation.boardlist.position.invalid",
            messageResolver
        );
    }
} 