package com.boardly.features.board.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateBoardValidator {
  private final ValidationMessageResolver messageResolver;

  public ValidationResult<CreateBoardCommand> validate(CreateBoardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<CreateBoardCommand> getValidator() {
    return Validator.combine(
      titleValidator(),
      BoardValidationRules.descriptionValidator(CreateBoardCommand::description, messageResolver),
      ownerValidator()
    );
  }

  private Validator<CreateBoardCommand> titleValidator() {
    return Validator.chain(
      Validator.fieldWithMessage(
        CreateBoardCommand::title,
        title -> title != null && !title.trim().isEmpty(),
        "title",
        "validation.board.title.required",
        messageResolver
      ),
      BoardValidationRules.titleValidator(CreateBoardCommand::title, messageResolver)
    );
  }

  private Validator<CreateBoardCommand> ownerValidator() {
    return Validator.fieldWithMessage(
      CreateBoardCommand::ownerId,
      ownerId -> ownerId != null,
      "ownerId",
      "validation.board.owner.required",
      messageResolver
    );
  }
}
