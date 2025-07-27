package com.boardly.features.card.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.card.application.port.input.AddCardLabelCommand;
import com.boardly.features.card.application.port.input.RemoveCardLabelCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

/**
 * 카드 라벨 추가/제거 검증기
 * 
 * <p>
 * 카드 라벨 추가 및 제거 관련 Command들의 입력 검증을 담당합니다.
 * AddCardLabelCommand와 RemoveCardLabelCommand의 검증 로직을 관리합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CardLabelValidator {

    private final CommonValidationRules commonValidationRules;
    private final ValidationMessageResolver messageResolver;

    /**
     * 카드 라벨 추가 커맨드 검증
     */
    public ValidationResult<AddCardLabelCommand> validateAdd(AddCardLabelCommand command) {
        return getAddValidator().validate(command);
    }

    /**
     * 카드 라벨 제거 커맨드 검증
     */
    public ValidationResult<RemoveCardLabelCommand> validateRemove(RemoveCardLabelCommand command) {
        return getRemoveValidator().validate(command);
    }

    /**
     * 카드 라벨 추가 검증 규칙
     */
    private Validator<AddCardLabelCommand> getAddValidator() {
        return Validator.combine(
                // 카드 ID 검증: 필수
                commonValidationRules.cardIdRequired(AddCardLabelCommand::cardId),
                // 라벨 ID 검증: 필수
                labelIdRequired(AddCardLabelCommand::labelId),
                // 요청자 ID 검증: 필수
                commonValidationRules.userIdRequired(AddCardLabelCommand::requesterId));
    }

    /**
     * 카드 라벨 제거 검증 규칙
     */
    private Validator<RemoveCardLabelCommand> getRemoveValidator() {
        return Validator.combine(
                // 카드 ID 검증: 필수
                commonValidationRules.cardIdRequired(RemoveCardLabelCommand::cardId),
                // 라벨 ID 검증: 필수
                labelIdRequired(RemoveCardLabelCommand::labelId),
                // 요청자 ID 검증: 필수
                commonValidationRules.userIdRequired(RemoveCardLabelCommand::requesterId));
    }

    /**
     * 라벨 ID 필수 검증
     */
    private <T> Validator<T> labelIdRequired(java.util.function.Function<T, Object> labelIdExtractor) {
        return Validator.fieldWithMessage(
                labelIdExtractor,
                labelId -> labelId != null,
                "labelId",
                "validation.label.id.required",
                messageResolver);
    }
}