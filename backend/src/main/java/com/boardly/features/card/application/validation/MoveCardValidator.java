package com.boardly.features.card.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MoveCardValidator {
  private final CommonValidationRules commonValidationRules;
  private final ValidationMessageResolver messageResolver;

  public ValidationResult<MoveCardCommand> validate(MoveCardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<MoveCardCommand> getValidator() {
    return Validator.combine(
        // 카드 ID 검증: 필수
        commonValidationRules.cardIdRequired(MoveCardCommand::cardId),
        // 새로운 위치 검증: 필수, 0 이상
        newPositionValidator(),
        // 사용자 ID 검증: 필수
        commonValidationRules.userIdRequired(MoveCardCommand::userId));
  }

  /**
   * 새로운 위치 검증 (필수, 0 이상)
   */
  private Validator<MoveCardCommand> newPositionValidator() {
    return Validator.fieldWithMessage(
        MoveCardCommand::newPosition,
        position -> position != null && position >= 0,
        "newPosition",
        "validation.position.required",
        messageResolver);
  }
}