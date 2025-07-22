package com.boardly.features.card.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

/**
 * 카드 통합 검증기
 * 
 * <p>
 * 모든 카드 관련 Command들의 입력 검증을 담당합니다.
 * Create, Update, Delete, Move, Clone 각각의 검증 로직을 통합하여 관리합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CardValidator {

    private final CommonValidationRules commonValidationRules;
    private final ValidationMessageResolver messageResolver;

    /**
     * 카드 생성 커맨드 검증
     */
    public ValidationResult<CreateCardCommand> validateCreate(CreateCardCommand command) {
        return getCreateValidator().validate(command);
    }

    /**
     * 카드 수정 커맨드 검증
     */
    public ValidationResult<UpdateCardCommand> validateUpdate(UpdateCardCommand command) {
        return getUpdateValidator().validate(command);
    }

    /**
     * 카드 삭제 커맨드 검증
     */
    public ValidationResult<DeleteCardCommand> validateDelete(DeleteCardCommand command) {
        return getDeleteValidator().validate(command);
    }

    /**
     * 카드 이동 커맨드 검증
     */
    public ValidationResult<MoveCardCommand> validateMove(MoveCardCommand command) {
        return getMoveValidator().validate(command);
    }

    /**
     * 카드 복제 커맨드 검증
     */
    public ValidationResult<CloneCardCommand> validateClone(CloneCardCommand command) {
        return getCloneValidator().validate(command);
    }

    /**
     * 카드 생성 검증 규칙
     */
    private Validator<CreateCardCommand> getCreateValidator() {
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

    /**
     * 카드 수정 검증 규칙
     */
    private Validator<UpdateCardCommand> getUpdateValidator() {
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

    /**
     * 카드 삭제 검증 규칙
     */
    private Validator<DeleteCardCommand> getDeleteValidator() {
        return Validator.combine(
                // 카드 ID 검증: 필수
                commonValidationRules.cardIdRequired(DeleteCardCommand::cardId),
                // 사용자 ID 검증: 필수
                commonValidationRules.userIdRequired(DeleteCardCommand::userId));
    }

    /**
     * 카드 이동 검증 규칙
     */
    private Validator<MoveCardCommand> getMoveValidator() {
        return Validator.combine(
                // 카드 ID 검증: 필수
                commonValidationRules.cardIdRequired(MoveCardCommand::cardId),
                // 새로운 위치 검증: 필수, 0 이상
                newPositionValidator(),
                // 사용자 ID 검증: 필수
                commonValidationRules.userIdRequired(MoveCardCommand::userId));
    }

    /**
     * 카드 복제 검증 규칙
     */
    private Validator<CloneCardCommand> getCloneValidator() {
        return Validator.combine(
                // 카드 ID 검증: 필수
                commonValidationRules.cardIdRequired(CloneCardCommand::cardId),
                // 새로운 제목 검증: 필수, 1-200자, HTML 태그 금지
                commonValidationRules.cardTitleComplete(CloneCardCommand::newTitle),
                // 사용자 ID 검증: 필수
                commonValidationRules.userIdRequired(CloneCardCommand::userId));
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