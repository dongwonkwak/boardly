package com.boardly.features.board.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RemoveBoardMemberValidator {
    private final CommonValidationRules commonValidationRules;

    public ValidationResult<RemoveBoardMemberCommand> validate(RemoveBoardMemberCommand command) {
        return getValidator().validate(command);
    }

    private Validator<RemoveBoardMemberCommand> getValidator() {
        return Validator.combine(
                commonValidationRules.boardIdRequired(RemoveBoardMemberCommand::boardId),
                commonValidationRules.userIdRequired(RemoveBoardMemberCommand::targetUserId),
                commonValidationRules.userIdRequired(RemoveBoardMemberCommand::requestedBy));
    }
}