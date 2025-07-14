package com.boardly.features.board.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArchiveBoardValidator {
  
  private final ValidationMessageResolver messageResolver;

  public ValidationResult<ArchiveBoardCommand> validate(ArchiveBoardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<ArchiveBoardCommand> getValidator() {
    return Validator.combine(
      BoardValidationRules.boardIdValidator(ArchiveBoardCommand::boardId, messageResolver),
      BoardValidationRules.requestedByValidator(ArchiveBoardCommand::requestedBy, messageResolver)
    );
  }
}
