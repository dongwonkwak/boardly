package com.boardly.features.card.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.card.application.port.input.GetCardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

/**
 * 카드 조회 명령 검증기
 * 
 * <p>
 * 카드 조회 요청의 입력 데이터 유효성을 검증합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GetCardValidator {

  private final CommonValidationRules commonValidationRules;

  /**
   * 카드 조회 명령 검증
   * 
   * @param command 조회 명령
   * @return 검증 결과
   */
  public ValidationResult<GetCardCommand> validate(GetCardCommand command) {
    return getValidator().validate(command);
  }

  private Validator<GetCardCommand> getValidator() {
    return Validator.combine(
        commonValidationRules.cardIdRequired(GetCardCommand::cardId),
        commonValidationRules.userIdRequired(GetCardCommand::userId));
  }
}