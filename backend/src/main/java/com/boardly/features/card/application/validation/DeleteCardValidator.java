package com.boardly.features.card.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteCardValidator {
  private final CommonValidationRules commonValidationRules;

  public ValidationResult<DeleteCardCommand> validate(DeleteCardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<DeleteCardCommand> getValidator() {
    return Validator.combine(
        // 카드 ID 검증: 필수
        commonValidationRules.cardIdRequired(DeleteCardCommand::cardId),
        // 사용자 ID 검증: 필수
        commonValidationRules.userIdRequired(DeleteCardCommand::userId));
  }
}