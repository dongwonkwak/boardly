package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UpdateBoardListValidatorTest {

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        private UpdateBoardListValidator validator;

        @BeforeEach
        void setUp() {
                // ValidationMessageResolver Mock 설정
                lenient().when(validationMessageResolver.getMessage("validation.boardlist.title.required"))
                                .thenReturn("제목은 필수입니다");
                lenient().when(validationMessageResolver.getMessage("validation.boardlist.title.max.length"))
                                .thenReturn("제목은 100자를 초과할 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("validation.boardlist.title.invalid"))
                                .thenReturn("제목에 HTML 태그를 포함할 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("validation.boardlist.description.max.length"))
                                .thenReturn("설명은 500자를 초과할 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("validation.boardlist.description.invalid"))
                                .thenReturn("설명에 HTML 태그를 포함할 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("validation.boardlist.listId.required"))
                                .thenReturn("리스트 ID는 필수입니다");
                lenient().when(validationMessageResolver.getMessage("validation.boardlist.userId.required"))
                                .thenReturn("사용자 ID는 필수입니다");
                lenient().when(validationMessageResolver.getMessage("validation.boardlist.color.invalid"))
                                .thenReturn("유효하지 않은 색상 값입니다");

                validator = new UpdateBoardListValidator(validationMessageResolver);
        }

        @Test
        @DisplayName("유효한 UpdateBoardListCommand는 검증을 통과해야 한다")
        void validate_ValidCommand_ShouldPass() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "수정된 리스트",
                                "수정된 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("listId가 null인 경우 검증에 실패해야 한다")
        void validate_NullListId_ShouldFail() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                null,
                                new UserId(),
                                "수정된 리스트",
                                "수정된 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("listId");
                assertThat(result.getErrors().get(0).message()).isEqualTo("리스트 ID는 필수입니다");
        }

        @Test
        @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
        void validate_NullUserId_ShouldFail() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                null,
                                "수정된 리스트",
                                "수정된 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
                assertThat(result.getErrors().get(0).message()).isEqualTo("사용자 ID는 필수입니다");
        }

        @Test
        @DisplayName("제목이 최대 길이를 초과하는 경우 검증에 실패해야 한다")
        void validate_TitleExceedsMaxLength_ShouldFail() {
                // given
                String longTitle = "a".repeat(101);
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                longTitle,
                                "수정된 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("title");
                assertThat(result.getErrors().get(0).message()).isEqualTo("제목은 100자를 초과할 수 없습니다");
        }

        @Test
        @DisplayName("설명이 최대 길이를 초과하는 경우 검증에 실패해야 한다")
        void validate_DescriptionExceedsMaxLength_ShouldFail() {
                // given
                String longDescription = "a".repeat(501);
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "수정된 리스트",
                                longDescription,
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("description");
                assertThat(result.getErrors().get(0).message()).isEqualTo("설명은 500자를 초과할 수 없습니다");
        }

        @Test
        @DisplayName("제목에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
        void validate_TitleWithHtmlTag_ShouldFail() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "<script>alert('test')</script>",
                                "수정된 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("title");
                assertThat(result.getErrors().get(0).message()).isEqualTo("제목에 HTML 태그를 포함할 수 없습니다");
        }

        @Test
        @DisplayName("설명에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
        void validate_DescriptionWithHtmlTag_ShouldFail() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "수정된 리스트",
                                "<script>alert('test')</script>",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("description");
                assertThat(result.getErrors().get(0).message()).isEqualTo("설명에 HTML 태그를 포함할 수 없습니다");
        }

        @Test
        @DisplayName("유효하지 않은 색상으로 검증에 실패해야 한다")
        void validate_InvalidColor_ShouldFail() {
                // given
                // ListColor.of()는 유효하지 않은 색상을 받으면 기본 색상을 반환하므로,
                // 직접 ListColor 생성자를 사용하여 유효하지 않은 색상을 생성
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "수정된 리스트",
                                "수정된 설명",
                                new ListColor("invalid-color"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("color");
                assertThat(result.getErrors().get(0).message()).isEqualTo("유효하지 않은 색상 값입니다");
        }

        @Test
        @DisplayName("제목이 null인 경우 검증에 실패해야 한다")
        void validate_NullTitle_ShouldFail() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                null,
                                "수정된 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("title");
                assertThat(result.getErrors().get(0).message()).isEqualTo("제목은 필수입니다");
        }

        @Test
        @DisplayName("설명이 null인 경우 검증을 통과해야 한다")
        void validate_NullDescription_ShouldPass() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "수정된 리스트",
                                null,
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("색상이 null인 경우 검증을 통과해야 한다")
        void validate_NullColor_ShouldPass() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "수정된 리스트",
                                "수정된 설명",
                                null);

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("여러 필드가 유효하지 않은 경우 모든 오류가 반환되어야 한다")
        void validate_MultipleInvalidFields_ShouldReturnAllErrors() {
                // given
                String longTitle = "a".repeat(101);
                String longDescription = "a".repeat(501);
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                null,
                                null,
                                longTitle,
                                longDescription,
                                new ListColor("invalid-color"));

                // when
                ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(5);
                assertThat(result.getErrors()).extracting("field")
                                .containsExactlyInAnyOrder("listId", "userId", "title", "description", "color");
        }

        @Test
        @DisplayName("다른 유효한 색상들로도 검증을 통과해야 한다")
        void validate_OtherValidColors_ShouldPass() {
                // given
                String[] validColors = {
                                "#0079BF", "#D29034", "#519839", "#B04632", "#89609E",
                                "#CD5A91", "#4BBFDA", "#00AECC", "#838C91"
                };

                for (String color : validColors) {
                        UpdateBoardListCommand command = new UpdateBoardListCommand(
                                        new ListId(),
                                        new UserId(),
                                        "수정된 리스트",
                                        "수정된 설명",
                                        ListColor.of(color));

                        // when
                        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

                        // then
                        assertThat(result.isValid()).isTrue();
                }
        }
}