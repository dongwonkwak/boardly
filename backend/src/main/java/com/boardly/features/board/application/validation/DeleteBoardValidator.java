package com.boardly.features.board.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.DeleteBoardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteBoardValidator {

    private final CommonValidationRules commonValidationRules;

    public ValidationResult<DeleteBoardCommand> validate(DeleteBoardCommand command) {
        return getValidator().validate(command);
    }

    private Validator<DeleteBoardCommand> getValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(DeleteBoardCommand::boardId),
                commonValidationRules.userIdRequired(DeleteBoardCommand::requestedBy));
    }
}