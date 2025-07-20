package com.boardly.features.card.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateCardValidator {
  private final CommonValidationRules commonValidationRules;

  public ValidationResult<UpdateCardCommand> validate(UpdateCardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<UpdateCardCommand> getValidator() {
    return Validator.combine(
        // 카드 ID 검증: 필수
        commonValidationRules.cardIdRequired(UpdateCardCommand::cardId),
        // 제목 검증: 필수, 1-200자, HTML 태그 금지
        commonValidationRules.cardTitleComplete(UpdateCardCommand::title),
        // 설명 검증: 선택사항, 2000자까지, HTML 태그 금지
        commonValidationRules.cardDescriptionComplete(UpdateCardCommand::description),
        // 사용자 ID 검증: 필수
        commonValidationRules.userIdRequired(UpdateCardCommand::userId));
  }
}