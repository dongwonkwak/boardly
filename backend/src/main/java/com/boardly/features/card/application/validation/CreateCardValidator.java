package com.boardly.features.card.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateCardValidator {
  private final CommonValidationRules commonValidationRules;

  public ValidationResult<CreateCardCommand> validate(CreateCardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<CreateCardCommand> getValidator() {
    return Validator.combine(
        // 제목 검증: 필수, 1-200자, HTML 태그 금지
        commonValidationRules.cardTitleComplete(CreateCardCommand::title),
        // 설명 검증: 선택사항, 2000자까지, HTML 태그 금지
        commonValidationRules.cardDescriptionComplete(CreateCardCommand::description),
        // 리스트 ID 검증: 필수
        commonValidationRules.listIdRequired(CreateCardCommand::listId),
        // 사용자 ID 검증: 필수
        commonValidationRules.userIdRequired(CreateCardCommand::userId));
  }
}