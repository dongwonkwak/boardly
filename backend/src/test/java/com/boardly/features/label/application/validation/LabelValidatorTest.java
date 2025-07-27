package com.boardly.features.label.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.label.application.port.input.CreateLabelCommand;
import com.boardly.features.label.application.port.input.DeleteLabelCommand;
import com.boardly.features.label.application.port.input.UpdateLabelCommand;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("LabelValidator 테스트")
class LabelValidatorTest {

    @Mock
    private MessageSource messageSource;

    private LabelValidator labelValidator;
    private ValidationMessageResolver messageResolver;
    private CommonValidationRules commonValidationRules;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);

        // 기본 메시지 설정 - lenient로 설정하여 불필요한 stubbing 허용
        lenient().when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenAnswer(invocation -> {
                    String code = invocation.getArgument(0);
                    Object[] args = invocation.getArgument(1);
                    StringBuilder message = new StringBuilder(code);
                    if (args != null) {
                        for (Object arg : args) {
                            message.append(" ").append(arg);
                        }
                    }
                    return message.toString();
                });

        messageResolver = new ValidationMessageResolver(messageSource);
        commonValidationRules = new CommonValidationRules(messageResolver);
        labelValidator = new LabelValidator(commonValidationRules, messageResolver);
    }

    private CreateLabelCommand createValidCreateCommand() {
        return new CreateLabelCommand(
                new BoardId("board-1"),
                new UserId("user-1"),
                "테스트 라벨",
                "#FF0000");
    }

    private UpdateLabelCommand createValidUpdateCommand() {
        return new UpdateLabelCommand(
                new LabelId("label-1"),
                new UserId("user-1"),
                "수정된 라벨",
                "#00FF00");
    }

    private DeleteLabelCommand createValidDeleteCommand() {
        return new DeleteLabelCommand(
                new LabelId("label-1"),
                new UserId("user-1"));
    }

    @Nested
    @DisplayName("CreateLabelCommand 검증 테스트")
    class CreateLabelCommandValidationTest {

        @Test
        @DisplayName("유효한 라벨 생성 정보는 검증을 통과해야 한다")
        void validateCreateLabel_withValidData_shouldBeValid() {
            // given
            CreateLabelCommand command = createValidCreateCommand();

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("라벨 이름이 없으면 검증에 실패해야 한다")
        void validateCreateLabel_withoutName_shouldBeInvalid() {
            // given
            CreateLabelCommand command = new CreateLabelCommand(
                    new BoardId("board-1"),
                    new UserId("user-1"),
                    "",
                    "#FF0000");

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("name")
                    && error.message().contains("validation.label.name.required"));
        }

        @Test
        @DisplayName("라벨 이름이 최대 길이를 초과하면 검증에 실패해야 한다")
        void validateCreateLabel_withNameTooLong_shouldBeInvalid() {
            // given
            String longName = "a".repeat(51); // 51자
            CreateLabelCommand command = new CreateLabelCommand(
                    new BoardId("board-1"),
                    new UserId("user-1"),
                    longName,
                    "#FF0000");

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("name")
                    && error.message().contains("validation.label.name.max.length"));
        }

        @Test
        @DisplayName("라벨 이름에 HTML 태그가 포함되면 검증에 실패해야 한다")
        void validateCreateLabel_withHtmlInName_shouldBeInvalid() {
            // given
            CreateLabelCommand command = new CreateLabelCommand(
                    new BoardId("board-1"),
                    new UserId("user-1"),
                    "<script>alert('test')</script>",
                    "#FF0000");

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(
                    error -> error.field().equals("name") && error.message().contains("validation.label.name.invalid"));
        }

        @Test
        @DisplayName("라벨 색상이 없으면 검증에 실패해야 한다")
        void validateCreateLabel_withoutColor_shouldBeInvalid() {
            // given
            CreateLabelCommand command = new CreateLabelCommand(
                    new BoardId("board-1"),
                    new UserId("user-1"),
                    "테스트 라벨",
                    "");

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("color")
                    && error.message().contains("validation.label.color.required"));
        }

        @Test
        @DisplayName("잘못된 hex 색상 형식이면 검증에 실패해야 한다")
        void validateCreateLabel_withInvalidHexColor_shouldBeInvalid() {
            // given
            CreateLabelCommand command = new CreateLabelCommand(
                    new BoardId("board-1"),
                    new UserId("user-1"),
                    "테스트 라벨",
                    "red");

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("color")
                    && error.message().contains("validation.label.color.invalid"));
        }

        @Test
        @DisplayName("올바른 6자리 hex 색상은 검증을 통과해야 한다")
        void validateCreateLabel_withValid6DigitHexColor_shouldBeValid() {
            // given
            CreateLabelCommand command = new CreateLabelCommand(
                    new BoardId("board-1"),
                    new UserId("user-1"),
                    "테스트 라벨",
                    "#FF0000");

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("올바른 3자리 hex 색상은 검증을 통과해야 한다")
        void validateCreateLabel_withValid3DigitHexColor_shouldBeValid() {
            // given
            CreateLabelCommand command = new CreateLabelCommand(
                    new BoardId("board-1"),
                    new UserId("user-1"),
                    "테스트 라벨",
                    "#F00");

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("보드 ID가 없으면 검증에 실패해야 한다")
        void validateCreateLabel_withoutBoardId_shouldBeInvalid() {
            // given
            CreateLabelCommand command = new CreateLabelCommand(
                    null,
                    new UserId("user-1"),
                    "테스트 라벨",
                    "#FF0000");

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("boardId")
                    && error.message().contains("validation.boardId.required"));
        }

        @Test
        @DisplayName("사용자 ID가 없으면 검증에 실패해야 한다")
        void validateCreateLabel_withoutUserId_shouldBeInvalid() {
            // given
            CreateLabelCommand command = new CreateLabelCommand(
                    new BoardId("board-1"),
                    null,
                    "테스트 라벨",
                    "#FF0000");

            // when
            ValidationResult<CreateLabelCommand> result = labelValidator.validateCreateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("requesterId")
                    && error.message().contains("validation.label.userId.required"));
        }
    }

    @Nested
    @DisplayName("UpdateLabelCommand 검증 테스트")
    class UpdateLabelCommandValidationTest {

        @Test
        @DisplayName("유효한 라벨 수정 정보는 검증을 통과해야 한다")
        void validateUpdateLabel_withValidData_shouldBeValid() {
            // given
            UpdateLabelCommand command = createValidUpdateCommand();

            // when
            ValidationResult<UpdateLabelCommand> result = labelValidator.validateUpdateLabel(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("라벨 이름이 최대 길이를 초과하면 검증에 실패해야 한다")
        void validateUpdateLabel_withNameTooLong_shouldBeInvalid() {
            // given
            String longName = "a".repeat(51); // 51자
            UpdateLabelCommand command = new UpdateLabelCommand(
                    new LabelId("label-1"),
                    new UserId("user-1"),
                    longName,
                    "#FF0000");

            // when
            ValidationResult<UpdateLabelCommand> result = labelValidator.validateUpdateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("name")
                    && error.message().contains("validation.label.name.max.length"));
        }

        @Test
        @DisplayName("라벨 이름에 HTML 태그가 포함되면 검증에 실패해야 한다")
        void validateUpdateLabel_withHtmlInName_shouldBeInvalid() {
            // given
            UpdateLabelCommand command = new UpdateLabelCommand(
                    new LabelId("label-1"),
                    new UserId("user-1"),
                    "<script>alert('test')</script>",
                    "#FF0000");

            // when
            ValidationResult<UpdateLabelCommand> result = labelValidator.validateUpdateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(
                    error -> error.field().equals("name") && error.message().contains("validation.label.name.invalid"));
        }

        @Test
        @DisplayName("잘못된 hex 색상 형식이면 검증에 실패해야 한다")
        void validateUpdateLabel_withInvalidHexColor_shouldBeInvalid() {
            // given
            UpdateLabelCommand command = new UpdateLabelCommand(
                    new LabelId("label-1"),
                    new UserId("user-1"),
                    "수정된 라벨",
                    "blue");

            // when
            ValidationResult<UpdateLabelCommand> result = labelValidator.validateUpdateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("color")
                    && error.message().contains("validation.label.color.invalid"));
        }

        @Test
        @DisplayName("빈 이름과 색상은 검증을 통과해야 한다 (선택적 필드)")
        void validateUpdateLabel_withEmptyNameAndColor_shouldBeValid() {
            // given
            UpdateLabelCommand command = new UpdateLabelCommand(
                    new LabelId("label-1"),
                    new UserId("user-1"),
                    "",
                    "");

            // when
            ValidationResult<UpdateLabelCommand> result = labelValidator.validateUpdateLabel(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("라벨 ID가 없으면 검증에 실패해야 한다")
        void validateUpdateLabel_withoutLabelId_shouldBeInvalid() {
            // given
            UpdateLabelCommand command = new UpdateLabelCommand(
                    null,
                    new UserId("user-1"),
                    "수정된 라벨",
                    "#FF0000");

            // when
            ValidationResult<UpdateLabelCommand> result = labelValidator.validateUpdateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("labelId")
                    && error.message().contains("validation.label.id.required"));
        }

        @Test
        @DisplayName("사용자 ID가 없으면 검증에 실패해야 한다")
        void validateUpdateLabel_withoutUserId_shouldBeInvalid() {
            // given
            UpdateLabelCommand command = new UpdateLabelCommand(
                    new LabelId("label-1"),
                    null,
                    "수정된 라벨",
                    "#FF0000");

            // when
            ValidationResult<UpdateLabelCommand> result = labelValidator.validateUpdateLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("userId")
                    && error.message().contains("validation.label.userId.required"));
        }
    }

    @Nested
    @DisplayName("DeleteLabelCommand 검증 테스트")
    class DeleteLabelCommandValidationTest {

        @Test
        @DisplayName("유효한 라벨 삭제 정보는 검증을 통과해야 한다")
        void validateDeleteLabel_withValidData_shouldBeValid() {
            // given
            DeleteLabelCommand command = createValidDeleteCommand();

            // when
            ValidationResult<DeleteLabelCommand> result = labelValidator.validateDeleteLabel(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("라벨 ID가 없으면 검증에 실패해야 한다")
        void validateDeleteLabel_withoutLabelId_shouldBeInvalid() {
            // given
            DeleteLabelCommand command = new DeleteLabelCommand(
                    null,
                    new UserId("user-1"));

            // when
            ValidationResult<DeleteLabelCommand> result = labelValidator.validateDeleteLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("labelId")
                    && error.message().contains("validation.label.id.required"));
        }

        @Test
        @DisplayName("사용자 ID가 없으면 검증에 실패해야 한다")
        void validateDeleteLabel_withoutUserId_shouldBeInvalid() {
            // given
            DeleteLabelCommand command = new DeleteLabelCommand(
                    new LabelId("label-1"),
                    null);

            // when
            ValidationResult<DeleteLabelCommand> result = labelValidator.validateDeleteLabel(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).anyMatch(error -> error.field().equals("userId")
                    && error.message().contains("validation.label.userId.required"));
        }
    }
}