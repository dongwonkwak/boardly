package com.boardly.features.board.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateBoardValidator {
  
  private final CommonValidationRules commonValidationRules;

  public ValidationResult<UpdateBoardCommand> validate(UpdateBoardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<UpdateBoardCommand> getValidator() {
    return Validator.combine(
      commonValidationRules.boardIdRequired(UpdateBoardCommand::boardId),
      commonValidationRules.userIdRequired(UpdateBoardCommand::requestedBy),
      commonValidationRules.titleOptional(UpdateBoardCommand::title),
      commonValidationRules.descriptionComplete(UpdateBoardCommand::description)
    );
  }
}
