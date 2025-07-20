package com.boardly.features.boardlist.application.validation;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
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
class CreateBoardListValidatorTest {

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        private CreateBoardListValidator validator;

        @BeforeEach
        void setUp() {
                // ValidationMessageResolver Mock 설정
                lenient().when(validationMessageResolver.getMessage("validation.title.required"))
                                .thenReturn("제목은 필수입니다");
                lenient().when(validationMessageResolver.getMessage("validation.title.max.length", 100))
                                .thenReturn("제목은 100자를 초과할 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("validation.title.invalid"))
                                .thenReturn("제목에 HTML 태그를 포함할 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("validation.description.max.length", 500))
                                .thenReturn("설명은 500자를 초과할 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("validation.description.invalid"))
                                .thenReturn("설명에 HTML 태그를 포함할 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("validation.boardId.required"))
                                .thenReturn("보드 ID는 필수입니다");
                lenient().when(validationMessageResolver.getMessage("validation.userId.required"))
                                .thenReturn("사용자 ID는 필수입니다");
                lenient().when(validationMessageResolver.getMessage("validation.color.required"))
                                .thenReturn("리스트 색상은 필수입니다");
                lenient().when(validationMessageResolver.getMessage("validation.color.invalid"))
                                .thenReturn("유효하지 않은 색상 값입니다");

                CommonValidationRules commonValidationRules = new CommonValidationRules(validationMessageResolver);
                validator = new CreateBoardListValidator(commonValidationRules);
        }

        @Test
        @DisplayName("유효한 CreateBoardListCommand는 검증을 통과해야 한다")
        void validate_ValidCommand_ShouldPass() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "테스트 리스트",
                                "테스트 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("제목이 null인 경우 검증에 실패해야 한다")
        void validate_NullTitle_ShouldFail() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                null,
                                "테스트 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("title");
                assertThat(result.getErrors().get(0).message()).isEqualTo("제목은 필수입니다");
        }

        @Test
        @DisplayName("제목이 빈 문자열인 경우 검증에 실패해야 한다")
        void validate_EmptyTitle_ShouldFail() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "",
                                "테스트 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("title");
                assertThat(result.getErrors().get(0).message()).isEqualTo("제목은 필수입니다");
        }

        @Test
        @DisplayName("제목이 공백만 있는 경우 검증에 실패해야 한다")
        void validate_BlankTitle_ShouldFail() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "   ",
                                "테스트 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("title");
                assertThat(result.getErrors().get(0).message()).isEqualTo("제목은 필수입니다");
        }

        @Test
        @DisplayName("제목이 최대 길이를 초과하는 경우 검증에 실패해야 한다")
        void validate_TitleExceedsMaxLength_ShouldFail() {
                // given
                String longTitle = "a".repeat(101);
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                longTitle,
                                "테스트 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

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
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "테스트 리스트",
                                longDescription,
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("description");
                assertThat(result.getErrors().get(0).message()).isEqualTo("설명은 500자를 초과할 수 없습니다");
        }

        @Test
        @DisplayName("boardId가 null인 경우 검증에 실패해야 한다")
        void validate_NullBoardId_ShouldFail() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                null,
                                new UserId(),
                                "테스트 리스트",
                                "테스트 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("boardId");
                assertThat(result.getErrors().get(0).message()).isEqualTo("보드 ID는 필수입니다");
        }

        @Test
        @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
        void validate_NullUserId_ShouldFail() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                null,
                                "테스트 리스트",
                                "테스트 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
                assertThat(result.getErrors().get(0).message()).isEqualTo("사용자 ID는 필수입니다");
        }

        @Test
        @DisplayName("color가 null인 경우 검증에 실패해야 한다")
        void validate_NullColor_ShouldFail() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "테스트 리스트",
                                "테스트 설명",
                                null);

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("color");
                assertThat(result.getErrors().get(0).message()).isEqualTo("리스트 색상은 필수입니다");
        }

        @Test
        @DisplayName("여러 필드가 유효하지 않은 경우 모든 오류가 반환되어야 한다")
        void validate_MultipleInvalidFields_ShouldReturnAllErrors() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                null,
                                null,
                                "",
                                "a".repeat(501),
                                null);

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(5);
                assertThat(result.getErrors()).extracting("field")
                                .containsExactlyInAnyOrder("boardId", "userId", "title", "description", "color");
        }

        @Test
        @DisplayName("설명이 null인 경우 검증을 통과해야 한다")
        void validate_NullDescription_ShouldPass() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "테스트 리스트",
                                null,
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("제목에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
        void validate_TitleWithHtmlTag_ShouldFail() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "<script>alert('test')</script>",
                                "테스트 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

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
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "테스트 리스트",
                                "<script>alert('test')</script>",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

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
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "테스트 리스트",
                                "테스트 설명",
                                new ListColor("invalid-color"));

                // when
                ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                // then
                assertThat(result.isValid()).isFalse();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).field()).isEqualTo("color");
                assertThat(result.getErrors().get(0).message()).isEqualTo("유효하지 않은 색상 값입니다");
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
                        CreateBoardListCommand command = new CreateBoardListCommand(
                                        new BoardId(),
                                        new UserId(),
                                        "테스트 리스트",
                                        "테스트 설명",
                                        ListColor.of(color));

                        // when
                        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

                        // then
                        assertThat(result.isValid()).isTrue();
                }
        }

        @Test
        @DisplayName("validateCreateBoardList 메서드도 동일하게 동작해야 한다")
        void validateCreateBoardList_ShouldWorkSameAsValidate() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "테스트 리스트",
                                "테스트 설명",
                                ListColor.of("#0079BF"));

                // when
                ValidationResult<CreateBoardListCommand> result1 = validator.validate(command);
                ValidationResult<CreateBoardListCommand> result2 = validator.validateCreateBoardList(command);

                // then
                assertThat(result1.isValid()).isEqualTo(result2.isValid());
                if (result1.isValid()) {
                        assertThat(result1.get()).isEqualTo(result2.get());
                } else {
                        assertThat(result1.getErrors()).isEqualTo(result2.getErrors());
                }
        }
}