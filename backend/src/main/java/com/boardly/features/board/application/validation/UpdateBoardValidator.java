package com.boardly.features.board.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateBoardValidator {
  
  private final ValidationMessageResolver messageResolver;

  public ValidationResult<UpdateBoardCommand> validate(UpdateBoardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<UpdateBoardCommand> getValidator() {
    return Validator.combine(
      BoardValidationRules.boardIdValidator(UpdateBoardCommand::boardId, messageResolver),
      BoardValidationRules.requestedByValidator(UpdateBoardCommand::requestedBy, messageResolver),
      BoardValidationRules.titleValidator(UpdateBoardCommand::title, messageResolver),
      BoardValidationRules.descriptionValidator(UpdateBoardCommand::description, messageResolver)
    );
  }
}
