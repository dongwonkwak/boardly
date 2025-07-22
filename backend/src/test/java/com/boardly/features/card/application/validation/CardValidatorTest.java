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

import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import com.boardly.shared.domain.common.Failure.FieldViolation;

import io.vavr.collection.Seq;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardValidator 테스트")
class CardValidatorTest {

    @Mock
    private CommonValidationRules commonValidationRules;

    @Mock
    private ValidationMessageResolver messageResolver;

    private CardValidator cardValidator;

    @BeforeEach
    void setUp() {
        cardValidator = new CardValidator(commonValidationRules, messageResolver);
    }

    @Nested
    @DisplayName("카드 생성 검증 테스트")
    class CreateCardValidationTest {

        @Test
        @DisplayName("유효한 카드 생성 커맨드 검증 성공")
        void validateCreate_WithValidCommand_ShouldReturnSuccess() {
            // given
            CreateCardCommand command = CreateCardCommand.of(
                    "테스트 카드",
                    "테스트 설명",
                    new ListId("list-123"),
                    new UserId("user-123"));

            when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.cardDescriptionComplete(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.listIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<CreateCardCommand> result = cardValidator.validateCreate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("제목 검증 실패 시 카드 생성 검증 실패")
        void validateCreate_WithInvalidTitle_ShouldReturnFailure() {
            // given
            CreateCardCommand command = CreateCardCommand.of(
                    "",
                    "테스트 설명",
                    new ListId("list-123"),
                    new UserId("user-123"));

            when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.invalid("title", "제목은 필수입니다"));
            when(commonValidationRules.cardDescriptionComplete(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.listIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<CreateCardCommand> result = cardValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.field()).isEqualTo("title");
            assertThat(violation.message()).isEqualTo("제목은 필수입니다");
        }

        @Test
        @DisplayName("여러 검증 실패 시 모든 오류 반환")
        void validateCreate_WithMultipleFailures_ShouldReturnAllErrors() {
            // given
            CreateCardCommand command = CreateCardCommand.of(
                    "",
                    null,
                    null,
                    null);

            when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.invalid("title", "제목은 필수입니다"));
            when(commonValidationRules.cardDescriptionComplete(any()))
                    .thenReturn(Validator.invalid("description", "설명이 너무 깁니다"));
            when(commonValidationRules.listIdRequired(any()))
                    .thenReturn(Validator.invalid("listId", "리스트 ID는 필수입니다"));
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.invalid("userId", "사용자 ID는 필수입니다"));

            // when
            ValidationResult<CreateCardCommand> result = cardValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(4);

            Seq<String> fields = result.getErrors().map(FieldViolation::field);
            assertThat(fields).contains("title", "description", "listId", "userId");
        }
    }

    @Nested
    @DisplayName("카드 수정 검증 테스트")
    class UpdateCardValidationTest {

        @Test
        @DisplayName("유효한 카드 수정 커맨드 검증 성공")
        void validateUpdate_WithValidCommand_ShouldReturnSuccess() {
            // given
            UpdateCardCommand command = UpdateCardCommand.of(
                    new CardId("card-123"),
                    "수정된 제목",
                    "수정된 설명",
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.cardDescriptionComplete(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<UpdateCardCommand> result = cardValidator.validateUpdate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("카드 ID 검증 실패 시 수정 검증 실패")
        void validateUpdate_WithInvalidCardId_ShouldReturnFailure() {
            // given
            UpdateCardCommand command = UpdateCardCommand.of(
                    null,
                    "수정된 제목",
                    "수정된 설명",
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.invalid("cardId", "카드 ID는 필수입니다"));
            when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.cardDescriptionComplete(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<UpdateCardCommand> result = cardValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.field()).isEqualTo("cardId");
            assertThat(violation.message()).isEqualTo("카드 ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("카드 삭제 검증 테스트")
    class DeleteCardValidationTest {

        @Test
        @DisplayName("유효한 카드 삭제 커맨드 검증 성공")
        void validateDelete_WithValidCommand_ShouldReturnSuccess() {
            // given
            DeleteCardCommand command = DeleteCardCommand.of(
                    new CardId("card-123"),
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<DeleteCardCommand> result = cardValidator.validateDelete(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("사용자 ID 검증 실패 시 삭제 검증 실패")
        void validateDelete_WithInvalidUserId_ShouldReturnFailure() {
            // given
            DeleteCardCommand command = DeleteCardCommand.of(
                    new CardId("card-123"),
                    null);

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.invalid("userId", "사용자 ID는 필수입니다"));

            // when
            ValidationResult<DeleteCardCommand> result = cardValidator.validateDelete(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.field()).isEqualTo("userId");
            assertThat(violation.message()).isEqualTo("사용자 ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("카드 이동 검증 테스트")
    class MoveCardValidationTest {

        @Test
        @DisplayName("유효한 카드 이동 커맨드 검증 성공")
        void validateMove_WithValidCommand_ShouldReturnSuccess() {
            // given
            MoveCardCommand command = MoveCardCommand.of(
                    new CardId("card-123"),
                    new ListId("list-456"),
                    2,
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<MoveCardCommand> result = cardValidator.validateMove(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("위치 검증 실패 시 이동 검증 실패")
        void validateMove_WithInvalidPosition_ShouldReturnFailure() {
            // given
            MoveCardCommand command = MoveCardCommand.of(
                    new CardId("card-123"),
                    new ListId("list-456"),
                    -1,
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(messageResolver.getMessage("validation.position.required"))
                    .thenReturn("위치는 0 이상이어야 합니다");

            // when
            ValidationResult<MoveCardCommand> result = cardValidator.validateMove(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.field()).isEqualTo("newPosition");
            assertThat(violation.message()).isEqualTo("위치는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("위치가 null일 때 이동 검증 실패")
        void validateMove_WithNullPosition_ShouldReturnFailure() {
            // given
            MoveCardCommand command = MoveCardCommand.of(
                    new CardId("card-123"),
                    new ListId("list-456"),
                    null,
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(messageResolver.getMessage("validation.position.required"))
                    .thenReturn("위치는 필수입니다");

            // when
            ValidationResult<MoveCardCommand> result = cardValidator.validateMove(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.field()).isEqualTo("newPosition");
            assertThat(violation.message()).isEqualTo("위치는 필수입니다");
        }

        @Test
        @DisplayName("위치가 0일 때 이동 검증 성공")
        void validateMove_WithZeroPosition_ShouldReturnSuccess() {
            // given
            MoveCardCommand command = MoveCardCommand.of(
                    new CardId("card-123"),
                    new ListId("list-456"),
                    0,
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<MoveCardCommand> result = cardValidator.validateMove(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }
    }

    @Nested
    @DisplayName("카드 복제 검증 테스트")
    class CloneCardValidationTest {

        @Test
        @DisplayName("유효한 카드 복제 커맨드 검증 성공")
        void validateClone_WithValidCommand_ShouldReturnSuccess() {
            // given
            CloneCardCommand command = CloneCardCommand.of(
                    new CardId("card-123"),
                    "복제된 카드",
                    new ListId("list-456"),
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<CloneCardCommand> result = cardValidator.validateClone(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("새 제목 검증 실패 시 복제 검증 실패")
        void validateClone_WithInvalidNewTitle_ShouldReturnFailure() {
            // given
            CloneCardCommand command = CloneCardCommand.of(
                    new CardId("card-123"),
                    "",
                    new ListId("list-456"),
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.invalid("newTitle", "새 제목은 필수입니다"));
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<CloneCardCommand> result = cardValidator.validateClone(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            FieldViolation violation = result.getErrors().get(0);
            assertThat(violation.field()).isEqualTo("newTitle");
            assertThat(violation.message()).isEqualTo("새 제목은 필수입니다");
        }

        @Test
        @DisplayName("targetListId가 null인 경우 복제 검증 성공")
        void validateClone_WithNullTargetListId_ShouldReturnSuccess() {
            // given
            CloneCardCommand command = CloneCardCommand.of(
                    new CardId("card-123"),
                    "복제된 카드",
                    null,
                    new UserId("user-123"));

            when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.valid());
            when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // when
            ValidationResult<CloneCardCommand> result = cardValidator.validateClone(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }
    }

    @Nested
    @DisplayName("통합 검증 시나리오 테스트")
    class IntegrationValidationTest {

        @Test
        @DisplayName("모든 검증 메서드가 정상적으로 작동하는지 확인")
        void allValidationMethods_ShouldWorkCorrectly() {
            // given
            CreateCardCommand createCommand = CreateCardCommand.of(
                    "테스트 카드",
                    "테스트 설명",
                    new ListId("list-123"),
                    new UserId("user-123"));

            UpdateCardCommand updateCommand = UpdateCardCommand.of(
                    new CardId("card-123"),
                    "수정된 제목",
                    "수정된 설명",
                    new UserId("user-123"));

            DeleteCardCommand deleteCommand = DeleteCardCommand.of(
                    new CardId("card-123"),
                    new UserId("user-123"));

            MoveCardCommand moveCommand = MoveCardCommand.of(
                    new CardId("card-123"),
                    new ListId("list-456"),
                    2,
                    new UserId("user-123"));

            CloneCardCommand cloneCommand = CloneCardCommand.of(
                    new CardId("card-123"),
                    "복제된 카드",
                    new ListId("list-456"),
                    new UserId("user-123"));

            // 모든 검증기를 성공으로 설정 (lenient 사용)
            setupAllValidatorsToSuccess();

            // when & then
            assertThat(cardValidator.validateCreate(createCommand).isValid()).isTrue();
            assertThat(cardValidator.validateUpdate(updateCommand).isValid()).isTrue();
            assertThat(cardValidator.validateDelete(deleteCommand).isValid()).isTrue();
            assertThat(cardValidator.validateMove(moveCommand).isValid()).isTrue();
            assertThat(cardValidator.validateClone(cloneCommand).isValid()).isTrue();
        }

        private void setupAllValidatorsToSuccess() {
            // Create validation
            lenient().when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.cardDescriptionComplete(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.listIdRequired(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // Update validation
            lenient().when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.cardDescriptionComplete(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // Delete validation
            lenient().when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // Move validation
            lenient().when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());

            // Clone validation
            lenient().when(commonValidationRules.cardIdRequired(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.cardTitleComplete(any()))
                    .thenReturn(Validator.valid());
            lenient().when(commonValidationRules.userIdRequired(any()))
                    .thenReturn(Validator.valid());
        }
    }
}