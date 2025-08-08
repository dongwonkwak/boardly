package com.boardly.features.card.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.boardly.features.card.application.port.input.AddCardLabelCommand;
import com.boardly.features.card.application.port.input.RemoveCardLabelCommand;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import com.boardly.shared.domain.common.Failure.FieldViolation;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardLabelValidator 테스트")
class CardLabelValidatorTest {

    @Mock
    private CommonValidationRules commonValidationRules;

    @Mock
    private ValidationMessageResolver messageResolver;

    private CardLabelValidator cardLabelValidator;

    @BeforeEach
    void setUp() {
        cardLabelValidator = new CardLabelValidator(
            commonValidationRules,
            messageResolver
        );
    }

    @Nested
    @DisplayName("카드 라벨 추가 검증 테스트")
    class AddCardLabelValidationTest {

        @Test
        @DisplayName("유효한 카드 라벨 추가 커맨드 검증 성공")
        void validateAdd_WithValidCommand_ShouldReturnSuccess() {
            // given
            CardId cardId = new CardId("card-123");
            LabelId labelId = new LabelId("label-123");
            UserId requesterId = new UserId("requester-456");

            AddCardLabelCommand command = new AddCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            setupAllValidatorsToSuccess();

            // when
            ValidationResult<AddCardLabelCommand> result =
                cardLabelValidator.validateAdd(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("카드 ID 검증 실패 시 라벨 추가 검증 실패")
        void validateAdd_WithInvalidCardId_ShouldReturnFailure() {
            // given
            CardId cardId = new CardId("card-123");
            LabelId labelId = new LabelId("label-123");
            UserId requesterId = new UserId("requester-456");

            AddCardLabelCommand command = new AddCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            when(commonValidationRules.cardIdRequired(any())).thenReturn(
                Validator.invalid("cardId", "카드 ID는 필수입니다")
            );
            when(commonValidationRules.userIdRequired(any())).thenReturn(
                Validator.valid()
            );

            // when
            ValidationResult<AddCardLabelCommand> result =
                cardLabelValidator.validateAdd(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("cardId");
            assertThat(violation.getMessage()).isEqualTo(
                "카드 ID는 필수입니다"
            );
        }

        @Test
        @DisplayName("라벨 ID 검증 실패 시 라벨 추가 검증 실패")
        void validateAdd_WithInvalidLabelId_ShouldReturnFailure() {
            // given
            CardId cardId = new CardId("card-123");
            LabelId labelId = null; // null 라벨 ID
            UserId requesterId = new UserId("requester-456");

            AddCardLabelCommand command = new AddCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            when(commonValidationRules.cardIdRequired(any())).thenReturn(
                Validator.valid()
            );
            when(commonValidationRules.userIdRequired(any())).thenReturn(
                Validator.valid()
            );

            // when
            ValidationResult<AddCardLabelCommand> result =
                cardLabelValidator.validateAdd(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("labelId");
        }

        @Test
        @DisplayName("요청자 ID 검증 실패 시 라벨 추가 검증 실패")
        void validateAdd_WithInvalidRequesterId_ShouldReturnFailure() {
            // given
            CardId cardId = new CardId("card-123");
            LabelId labelId = new LabelId("label-123");
            UserId requesterId = null; // null 요청자 ID

            AddCardLabelCommand command = new AddCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            when(commonValidationRules.cardIdRequired(any())).thenReturn(
                Validator.valid()
            );
            when(commonValidationRules.userIdRequired(any())).thenReturn(
                Validator.invalid("requesterId", "요청자 ID는 필수입니다")
            );

            // when
            ValidationResult<AddCardLabelCommand> result =
                cardLabelValidator.validateAdd(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("requesterId");
            assertThat(violation.getMessage()).isEqualTo(
                "요청자 ID는 필수입니다"
            );
        }

        @Test
        @DisplayName("여러 필드 검증 실패 시 모든 오류 반환")
        void validateAdd_WithMultipleInvalidFields_ShouldReturnAllErrors() {
            // given
            CardId cardId = null; // null 카드 ID
            LabelId labelId = null; // null 라벨 ID
            UserId requesterId = null; // null 요청자 ID

            AddCardLabelCommand command = new AddCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            when(commonValidationRules.cardIdRequired(any())).thenReturn(
                Validator.invalid("cardId", "카드 ID는 필수입니다")
            );
            when(commonValidationRules.userIdRequired(any())).thenReturn(
                Validator.invalid("requesterId", "요청자 ID는 필수입니다")
            );

            // when
            ValidationResult<AddCardLabelCommand> result =
                cardLabelValidator.validateAdd(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(3);

            Seq<String> errorFields = result
                .getErrors()
                .map(FieldViolation::getField);
            assertThat(errorFields).contains(
                "cardId",
                "labelId",
                "requesterId"
            );
        }
    }

    @Nested
    @DisplayName("카드 라벨 제거 검증 테스트")
    class RemoveCardLabelValidationTest {

        @Test
        @DisplayName("유효한 카드 라벨 제거 커맨드 검증 성공")
        void validateRemove_WithValidCommand_ShouldReturnSuccess() {
            // given
            CardId cardId = new CardId("card-123");
            LabelId labelId = new LabelId("label-123");
            UserId requesterId = new UserId("requester-456");

            RemoveCardLabelCommand command = new RemoveCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            setupAllValidatorsToSuccess();

            // when
            ValidationResult<RemoveCardLabelCommand> result =
                cardLabelValidator.validateRemove(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("카드 ID 검증 실패 시 라벨 제거 검증 실패")
        void validateRemove_WithInvalidCardId_ShouldReturnFailure() {
            // given
            CardId cardId = null; // null 카드 ID
            LabelId labelId = new LabelId("label-123");
            UserId requesterId = new UserId("requester-456");

            RemoveCardLabelCommand command = new RemoveCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            when(commonValidationRules.cardIdRequired(any())).thenReturn(
                Validator.invalid("cardId", "카드 ID는 필수입니다")
            );
            when(commonValidationRules.userIdRequired(any())).thenReturn(
                Validator.valid()
            );

            // when
            ValidationResult<RemoveCardLabelCommand> result =
                cardLabelValidator.validateRemove(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("cardId");
            assertThat(violation.getMessage()).isEqualTo(
                "카드 ID는 필수입니다"
            );
        }

        @Test
        @DisplayName("라벨 ID 검증 실패 시 라벨 제거 검증 실패")
        void validateRemove_WithInvalidLabelId_ShouldReturnFailure() {
            // given
            CardId cardId = new CardId("card-123");
            LabelId labelId = null; // null 라벨 ID
            UserId requesterId = new UserId("requester-456");

            RemoveCardLabelCommand command = new RemoveCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            when(commonValidationRules.cardIdRequired(any())).thenReturn(
                Validator.valid()
            );
            when(commonValidationRules.userIdRequired(any())).thenReturn(
                Validator.valid()
            );

            // when
            ValidationResult<RemoveCardLabelCommand> result =
                cardLabelValidator.validateRemove(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("labelId");
        }

        @Test
        @DisplayName("요청자 ID 검증 실패 시 라벨 제거 검증 실패")
        void validateRemove_WithInvalidRequesterId_ShouldReturnFailure() {
            // given
            CardId cardId = new CardId("card-123");
            LabelId labelId = new LabelId("label-123");
            UserId requesterId = null; // null 요청자 ID

            RemoveCardLabelCommand command = new RemoveCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            when(commonValidationRules.cardIdRequired(any())).thenReturn(
                Validator.valid()
            );
            when(commonValidationRules.userIdRequired(any())).thenReturn(
                Validator.invalid("requesterId", "요청자 ID는 필수입니다")
            );

            // when
            ValidationResult<RemoveCardLabelCommand> result =
                cardLabelValidator.validateRemove(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.getField()).isEqualTo("requesterId");
            assertThat(violation.getMessage()).isEqualTo(
                "요청자 ID는 필수입니다"
            );
        }

        @Test
        @DisplayName("여러 필드 검증 실패 시 모든 오류 반환")
        void validateRemove_WithMultipleInvalidFields_ShouldReturnAllErrors() {
            // given
            CardId cardId = null; // null 카드 ID
            LabelId labelId = null; // null 라벨 ID
            UserId requesterId = null; // null 요청자 ID

            RemoveCardLabelCommand command = new RemoveCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            when(commonValidationRules.cardIdRequired(any())).thenReturn(
                Validator.invalid("cardId", "카드 ID는 필수입니다")
            );
            when(commonValidationRules.userIdRequired(any())).thenReturn(
                Validator.invalid("requesterId", "요청자 ID는 필수입니다")
            );

            // when
            ValidationResult<RemoveCardLabelCommand> result =
                cardLabelValidator.validateRemove(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(3);

            Seq<String> errorFields = result
                .getErrors()
                .map(FieldViolation::getField);
            assertThat(errorFields).contains(
                "cardId",
                "labelId",
                "requesterId"
            );
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
            LabelId labelId = new LabelId("label-123");
            UserId requesterId = new UserId("requester-456");

            AddCardLabelCommand addCommand = new AddCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );
            RemoveCardLabelCommand removeCommand = new RemoveCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            setupAllValidatorsToSuccess();

            // when
            ValidationResult<AddCardLabelCommand> addResult =
                cardLabelValidator.validateAdd(addCommand);
            ValidationResult<RemoveCardLabelCommand> removeResult =
                cardLabelValidator.validateRemove(removeCommand);

            // then
            assertThat(addResult.isValid()).isTrue();
            assertThat(addResult.get()).isEqualTo(addCommand);

            assertThat(removeResult.isValid()).isTrue();
            assertThat(removeResult.get()).isEqualTo(removeCommand);
        }

        @Test
        @DisplayName("다른 ID 값으로 검증이 성공하는지 확인")
        void validationWithDifferentIds_ShouldSucceed() {
            // given
            CardId cardId = new CardId("different-card-456");
            LabelId labelId = new LabelId("different-label-789");
            UserId requesterId = new UserId("different-requester-101");

            AddCardLabelCommand addCommand = new AddCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );
            RemoveCardLabelCommand removeCommand = new RemoveCardLabelCommand(
                cardId,
                labelId,
                requesterId
            );

            setupAllValidatorsToSuccess();

            // when
            ValidationResult<AddCardLabelCommand> addResult =
                cardLabelValidator.validateAdd(addCommand);
            ValidationResult<RemoveCardLabelCommand> removeResult =
                cardLabelValidator.validateRemove(removeCommand);

            // then
            assertThat(addResult.isValid()).isTrue();
            assertThat(removeResult.isValid()).isTrue();
        }
    }

    /**
     * 모든 검증기를 성공으로 설정하는 헬퍼 메서드
     */
    private void setupAllValidatorsToSuccess() {
        lenient()
            .when(commonValidationRules.cardIdRequired(any()))
            .thenReturn(Validator.valid());
        lenient()
            .when(commonValidationRules.userIdRequired(any()))
            .thenReturn(Validator.valid());
    }
}
