package com.boardly.features.board.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArchiveBoardValidator {
  
  private final CommonValidationRules commonValidationRules;

  public ValidationResult<ArchiveBoardCommand> validate(ArchiveBoardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<ArchiveBoardCommand> getValidator() {
    return Validator.combine(
      commonValidationRules.boardIdRequired(ArchiveBoardCommand::boardId),
      commonValidationRules.userIdRequired(ArchiveBoardCommand::requestedBy)
    );
  }
}
