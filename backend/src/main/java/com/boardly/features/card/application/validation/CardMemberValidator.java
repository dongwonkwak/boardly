package com.boardly.features.card.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.card.application.port.input.AssignCardMemberCommand;
import com.boardly.features.card.application.port.input.UnassignCardMemberCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

/**
 * 카드 멤버 할당/해제 검증기
 * 
 * <p>
 * 카드 멤버 할당 및 해제 관련 Command들의 입력 검증을 담당합니다.
 * AssignCardMemberCommand와 UnassignCardMemberCommand의 검증 로직을 관리합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CardMemberValidator {

    private final CommonValidationRules commonValidationRules;
    private final ValidationMessageResolver messageResolver;

    /**
     * 카드 멤버 할당 커맨드 검증
     */
    public ValidationResult<AssignCardMemberCommand> validateAssign(AssignCardMemberCommand command) {
        return getAssignValidator().validate(command);
    }

    /**
     * 카드 멤버 해제 커맨드 검증
     */
    public ValidationResult<UnassignCardMemberCommand> validateUnassign(UnassignCardMemberCommand command) {
        return getUnassignValidator().validate(command);
    }

    /**
     * 카드 멤버 할당 검증 규칙
     */
    private Validator<AssignCardMemberCommand> getAssignValidator() {
        return Validator.combine(
                // 카드 ID 검증: 필수
                commonValidationRules.cardIdRequired(AssignCardMemberCommand::cardId),
                // 멤버 ID 검증: 필수
                commonValidationRules.userIdRequired(AssignCardMemberCommand::memberId),
                // 요청자 ID 검증: 필수
                commonValidationRules.userIdRequired(AssignCardMemberCommand::requesterId),
                // 멤버와 요청자가 다른지 검증
                assignMemberNotEqualToRequesterValidator());
    }

    /**
     * 카드 멤버 해제 검증 규칙
     */
    private Validator<UnassignCardMemberCommand> getUnassignValidator() {
        return Validator.combine(
                // 카드 ID 검증: 필수
                commonValidationRules.cardIdRequired(UnassignCardMemberCommand::cardId),
                // 멤버 ID 검증: 필수
                commonValidationRules.userIdRequired(UnassignCardMemberCommand::memberId),
                // 요청자 ID 검증: 필수
                commonValidationRules.userIdRequired(UnassignCardMemberCommand::requesterId),
                // 멤버와 요청자가 다른지 검증
                unassignMemberNotEqualToRequesterValidator());
    }

    /**
     * 멤버와 요청자가 다른지 검증 (할당)
     * 멤버 할당 시 멤버와 요청자가 동일한 사용자여서는 안됩니다.
     */
    private Validator<AssignCardMemberCommand> assignMemberNotEqualToRequesterValidator() {
        return Validator.fieldWithMessage(
                command -> command.memberId().equals(command.requesterId()),
                isSame -> !isSame,
                "memberId",
                "validation.card.member.same.as.requester",
                messageResolver);
    }

    /**
     * 멤버와 요청자가 다른지 검증 (해제)
     * 멤버 해제 시 멤버와 요청자가 동일한 사용자여서는 안됩니다.
     */
    private Validator<UnassignCardMemberCommand> unassignMemberNotEqualToRequesterValidator() {
        return Validator.fieldWithMessage(
                command -> command.memberId().equals(command.requesterId()),
                isSame -> !isSame,
                "memberId",
                "validation.card.member.same.as.requester",
                messageResolver);
    }
}