package com.boardly.features.board.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateBoardValidator {
  private final CommonValidationRules commonValidationRules;

  public ValidationResult<CreateBoardCommand> validate(CreateBoardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<CreateBoardCommand> getValidator() {
    return Validator.combine(
      commonValidationRules.titleComplete(CreateBoardCommand::title),
      commonValidationRules.descriptionComplete(CreateBoardCommand::description),
      commonValidationRules.userIdRequired(CreateBoardCommand::ownerId)
    );
  }
}
