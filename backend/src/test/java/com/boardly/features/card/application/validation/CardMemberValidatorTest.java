package com.boardly.features.card.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.card.application.port.input.AssignCardMemberCommand;
import com.boardly.features.card.application.port.input.UnassignCardMemberCommand;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import com.boardly.shared.domain.common.Failure.FieldViolation;

import io.vavr.collection.Seq;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardMemberValidator 테스트")
class CardMemberValidatorTest {

        @Mock
        private CommonValidationRules commonValidationRules;

        @Mock
        private ValidationMessageResolver messageResolver;

        private CardMemberValidator cardMemberValidator;

        @BeforeEach
        void setUp() {
                cardMemberValidator = new CardMemberValidator(commonValidationRules, messageResolver);
        }

        @Nested
        @DisplayName("카드 멤버 할당 검증 테스트")
        class AssignCardMemberValidationTest {

                @Test
                @DisplayName("유효한 카드 멤버 할당 커맨드 검증 성공")
                void validateAssign_WithValidCommand_ShouldReturnSuccess() {
                        // given
                        CardId cardId = new CardId("card-123");
                        UserId memberId = new UserId("member-123");
                        UserId requesterId = new UserId("requester-456");

                        AssignCardMemberCommand command = new AssignCardMemberCommand(cardId, memberId, requesterId);

                        setupAllValidatorsToSuccess();

                        // when
                        ValidationResult<AssignCardMemberCommand> result = cardMemberValidator.validateAssign(command);

                        // then
                        assertThat(result.isValid()).isTrue();
                        assertThat(result.get()).isEqualTo(command);
                }

                @Test
                @DisplayName("카드 ID 검증 실패 시 할당 검증 실패")
                void validateAssign_WithInvalidCardId_ShouldReturnFailure() {
                        // given
                        CardId cardId = new CardId("card-123");
                        UserId memberId = new UserId("member-123");
                        UserId requesterId = new UserId("requester-456");

                        AssignCardMemberCommand command = new AssignCardMemberCommand(cardId, memberId, requesterId);

                        when(commonValidationRules.cardIdRequired(any()))
                                        .thenReturn(Validator.invalid("cardId", "카드 ID는 필수입니다"));
                        when(commonValidationRules.userIdRequired(any()))
                                        .thenReturn(Validator.valid());

                        // when
                        ValidationResult<AssignCardMemberCommand> result = cardMemberValidator.validateAssign(command);

                        // then
                        assertThat(result.isInvalid()).isTrue();
                        assertThat(result.getErrors()).hasSize(1);
                        FieldViolation violation = result.getErrors().get(0);
                        assertThat(violation.field()).isEqualTo("cardId");
                        assertThat(violation.message()).isEqualTo("카드 ID는 필수입니다");
                }

                @Test
                @DisplayName("멤버와 요청자가 동일한 사용자일 때 할당 검증 실패")
                void validateAssign_WithSameMemberAndRequester_ShouldReturnFailure() {
                        // given
                        CardId cardId = new CardId("card-123");
                        UserId sameUserId = new UserId("user-123");

                        AssignCardMemberCommand command = new AssignCardMemberCommand(cardId, sameUserId, sameUserId);

                        setupAllValidatorsToSuccess();
                        when(messageResolver.getMessage("validation.card.member.same.as.requester"))
                                        .thenReturn("멤버와 요청자가 동일한 사용자일 수 없습니다");

                        // when
                        ValidationResult<AssignCardMemberCommand> result = cardMemberValidator.validateAssign(command);

                        // then
                        assertThat(result.isInvalid()).isTrue();
                        assertThat(result.getErrors()).hasSize(1);
                        FieldViolation violation = result.getErrors().get(0);
                        assertThat(violation.field()).isEqualTo("memberId");
                        assertThat(violation.message()).isEqualTo("멤버와 요청자가 동일한 사용자일 수 없습니다");
                }
        }

        @Nested
        @DisplayName("카드 멤버 해제 검증 테스트")
        class UnassignCardMemberValidationTest {

                @Test
                @DisplayName("유효한 카드 멤버 해제 커맨드 검증 성공")
                void validateUnassign_WithValidCommand_ShouldReturnSuccess() {
                        // given
                        CardId cardId = new CardId("card-123");
                        UserId memberId = new UserId("member-123");
                        UserId requesterId = new UserId("requester-456");

                        UnassignCardMemberCommand command = new UnassignCardMemberCommand(cardId, memberId,
                                        requesterId);

                        setupAllValidatorsToSuccess();

                        // when
                        ValidationResult<UnassignCardMemberCommand> result = cardMemberValidator
                                        .validateUnassign(command);

                        // then
                        assertThat(result.isValid()).isTrue();
                        assertThat(result.get()).isEqualTo(command);
                }

                @Test
                @DisplayName("카드 ID 검증 실패 시 해제 검증 실패")
                void validateUnassign_WithInvalidCardId_ShouldReturnFailure() {
                        // given
                        CardId cardId = new CardId("card-123");
                        UserId memberId = new UserId("member-123");
                        UserId requesterId = new UserId("requester-456");

                        UnassignCardMemberCommand command = new UnassignCardMemberCommand(cardId, memberId,
                                        requesterId);

                        when(commonValidationRules.cardIdRequired(any()))
                                        .thenReturn(Validator.invalid("cardId", "카드 ID는 필수입니다"));
                        when(commonValidationRules.userIdRequired(any()))
                                        .thenReturn(Validator.valid());

                        // when
                        ValidationResult<UnassignCardMemberCommand> result = cardMemberValidator
                                        .validateUnassign(command);

                        // then
                        assertThat(result.isInvalid()).isTrue();
                        assertThat(result.getErrors()).hasSize(1);
                        FieldViolation violation = result.getErrors().get(0);
                        assertThat(violation.field()).isEqualTo("cardId");
                        assertThat(violation.message()).isEqualTo("카드 ID는 필수입니다");
                }

                @Test
                @DisplayName("멤버와 요청자가 동일한 사용자일 때 해제 검증 실패")
                void validateUnassign_WithSameMemberAndRequester_ShouldReturnFailure() {
                        // given
                        CardId cardId = new CardId("card-123");
                        UserId sameUserId = new UserId("user-123");

                        UnassignCardMemberCommand command = new UnassignCardMemberCommand(cardId, sameUserId,
                                        sameUserId);

                        setupAllValidatorsToSuccess();
                        when(messageResolver.getMessage("validation.card.member.same.as.requester"))
                                        .thenReturn("멤버와 요청자가 동일한 사용자일 수 없습니다");

                        // when
                        ValidationResult<UnassignCardMemberCommand> result = cardMemberValidator
                                        .validateUnassign(command);

                        // then
                        assertThat(result.isInvalid()).isTrue();
                        assertThat(result.getErrors()).hasSize(1);
                        FieldViolation violation = result.getErrors().get(0);
                        assertThat(violation.field()).isEqualTo("memberId");
                        assertThat(violation.message()).isEqualTo("멤버와 요청자가 동일한 사용자일 수 없습니다");
                }
        }

        @Nested
        @DisplayName("통합 검증 시나리오 테스트")
        class IntegrationValidationTest {

                @Test
                @DisplayName("모든 검증 메서드가 정상적으로 작동하는지 확인")
                void allValidationMethods_ShouldWorkCorrectly() {
                        // given
                        CardId cardId = new CardId("card-123");
                        UserId memberId = new UserId("member-123");
                        UserId requesterId = new UserId("requester-456");

                        AssignCardMemberCommand assignCommand = new AssignCardMemberCommand(cardId, memberId,
                                        requesterId);
                        UnassignCardMemberCommand unassignCommand = new UnassignCardMemberCommand(cardId, memberId,
                                        requesterId);

                        setupAllValidatorsToSuccess();

                        // when
                        ValidationResult<AssignCardMemberCommand> assignResult = cardMemberValidator
                                        .validateAssign(assignCommand);
                        ValidationResult<UnassignCardMemberCommand> unassignResult = cardMemberValidator
                                        .validateUnassign(unassignCommand);

                        // then
                        assertThat(assignResult.isValid()).isTrue();
                        assertThat(assignResult.get()).isEqualTo(assignCommand);

                        assertThat(unassignResult.isValid()).isTrue();
                        assertThat(unassignResult.get()).isEqualTo(unassignCommand);
                }

                @Test
                @DisplayName("다른 사용자 ID로 검증이 성공하는지 확인")
                void validationWithDifferentUserIds_ShouldSucceed() {
                        // given
                        CardId cardId = new CardId("card-123");
                        UserId memberId1 = new UserId("member-1");
                        UserId memberId2 = new UserId("member-2");
                        UserId requesterId = new UserId("requester-123");

                        AssignCardMemberCommand assignCommand1 = new AssignCardMemberCommand(cardId, memberId1,
                                        requesterId);
                        AssignCardMemberCommand assignCommand2 = new AssignCardMemberCommand(cardId, memberId2,
                                        requesterId);

                        setupAllValidatorsToSuccess();

                        // when
                        ValidationResult<AssignCardMemberCommand> result1 = cardMemberValidator
                                        .validateAssign(assignCommand1);
                        ValidationResult<AssignCardMemberCommand> result2 = cardMemberValidator
                                        .validateAssign(assignCommand2);

                        // then
                        assertThat(result1.isValid()).isTrue();
                        assertThat(result2.isValid()).isTrue();
                }
        }

        private void setupAllValidatorsToSuccess() {
                lenient().when(commonValidationRules.cardIdRequired(any()))
                                .thenReturn(Validator.valid());
                lenient().when(commonValidationRules.userIdRequired(any()))
                                .thenReturn(Validator.valid());
        }
}