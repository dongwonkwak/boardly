package com.boardly.features.card.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CloneCardValidator {
  private final CommonValidationRules commonValidationRules;

  public ValidationResult<CloneCardCommand> validate(CloneCardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<CloneCardCommand> getValidator() {
    return Validator.combine(
        // 카드 ID 검증: 필수
        commonValidationRules.cardIdRequired(CloneCardCommand::cardId),
        // 새로운 제목 검증: 필수, 1-200자, HTML 태그 금지
        commonValidationRules.cardTitleComplete(CloneCardCommand::newTitle),
        // 사용자 ID 검증: 필수
        commonValidationRules.userIdRequired(CloneCardCommand::userId));
  }
}