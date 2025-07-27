package com.boardly.features.comment.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

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

import com.boardly.features.comment.application.port.input.CreateCommentCommand;
import com.boardly.features.comment.application.port.input.UpdateCommentCommand;
import com.boardly.features.comment.application.port.input.DeleteCommentCommand;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentValidator 테스트")
class CommentValidatorTest {

    @Mock
    private MessageSource messageSource;

    private CommentValidator commentValidator;
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
        commentValidator = new CommentValidator(commonValidationRules, messageResolver);
    }

    @Nested
    @DisplayName("CreateCommentCommand 검증 테스트")
    class CreateCommentCommandValidationTest {

        @Test
        @DisplayName("유효한 댓글 생성 커맨드는 검증을 통과해야 한다")
        void validateCreate_WithValidCommand_ShouldBeValid() {
            // given
            CreateCommentCommand command = new CreateCommentCommand(
                    new CardId("card-1"),
                    new UserId("user-1"),
                    "테스트 댓글 내용");

            // when
            ValidationResult<CreateCommentCommand> result = commentValidator.validateCreate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("카드 ID가 null이면 검증에 실패해야 한다")
        void validateCreate_WithNullCardId_ShouldBeInvalid() {
            // given
            CreateCommentCommand command = new CreateCommentCommand(
                    null,
                    new UserId("user-1"),
                    "테스트 댓글 내용");

            // when
            ValidationResult<CreateCommentCommand> result = commentValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("cardId");
        }

        @Test
        @DisplayName("작성자 ID가 null이면 검증에 실패해야 한다")
        void validateCreate_WithNullAuthorId_ShouldBeInvalid() {
            // given
            CreateCommentCommand command = new CreateCommentCommand(
                    new CardId("card-1"),
                    null,
                    "테스트 댓글 내용");

            // when
            ValidationResult<CreateCommentCommand> result = commentValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("authorId");
        }

        @Test
        @DisplayName("댓글 내용이 null이면 검증에 실패해야 한다")
        void validateCreate_WithNullContent_ShouldBeInvalid() {
            // given
            CreateCommentCommand command = new CreateCommentCommand(
                    new CardId("card-1"),
                    new UserId("user-1"),
                    null);

            // when
            ValidationResult<CreateCommentCommand> result = commentValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("content");
        }

        @Test
        @DisplayName("댓글 내용이 빈 문자열이면 검증에 실패해야 한다")
        void validateCreate_WithEmptyContent_ShouldBeInvalid() {
            // given
            CreateCommentCommand command = new CreateCommentCommand(
                    new CardId("card-1"),
                    new UserId("user-1"),
                    "");

            // when
            ValidationResult<CreateCommentCommand> result = commentValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("content");
        }

        @Test
        @DisplayName("댓글 내용이 공백만 있으면 검증에 실패해야 한다")
        void validateCreate_WithBlankContent_ShouldBeInvalid() {
            // given
            CreateCommentCommand command = new CreateCommentCommand(
                    new CardId("card-1"),
                    new UserId("user-1"),
                    "   ");

            // when
            ValidationResult<CreateCommentCommand> result = commentValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("content");
        }

        @Test
        @DisplayName("댓글 내용이 1000자를 초과하면 검증에 실패해야 한다")
        void validateCreate_WithContentExceedingMaxLength_ShouldBeInvalid() {
            // given
            String longContent = "a".repeat(1001);
            CreateCommentCommand command = new CreateCommentCommand(
                    new CardId("card-1"),
                    new UserId("user-1"),
                    longContent);

            // when
            ValidationResult<CreateCommentCommand> result = commentValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("content");
        }

        @Test
        @DisplayName("댓글 내용이 정확히 1000자이면 검증을 통과해야 한다")
        void validateCreate_WithContentExactlyMaxLength_ShouldBeValid() {
            // given
            String exactLengthContent = "a".repeat(1000);
            CreateCommentCommand command = new CreateCommentCommand(
                    new CardId("card-1"),
                    new UserId("user-1"),
                    exactLengthContent);

            // when
            ValidationResult<CreateCommentCommand> result = commentValidator.validateCreate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("UpdateCommentCommand 검증 테스트")
    class UpdateCommentCommandValidationTest {

        @Test
        @DisplayName("유효한 댓글 수정 커맨드는 검증을 통과해야 한다")
        void validateUpdate_WithValidCommand_ShouldBeValid() {
            // given
            UpdateCommentCommand command = new UpdateCommentCommand(
                    new CommentId("comment-1"),
                    new UserId("user-1"),
                    "수정된 댓글 내용");

            // when
            ValidationResult<UpdateCommentCommand> result = commentValidator.validateUpdate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("댓글 ID가 null이면 검증에 실패해야 한다")
        void validateUpdate_WithNullCommentId_ShouldBeInvalid() {
            // given
            UpdateCommentCommand command = new UpdateCommentCommand(
                    null,
                    new UserId("user-1"),
                    "수정된 댓글 내용");

            // when
            ValidationResult<UpdateCommentCommand> result = commentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("commentId");
        }

        @Test
        @DisplayName("요청자 ID가 null이면 검증에 실패해야 한다")
        void validateUpdate_WithNullRequesterId_ShouldBeInvalid() {
            // given
            UpdateCommentCommand command = new UpdateCommentCommand(
                    new CommentId("comment-1"),
                    null,
                    "수정된 댓글 내용");

            // when
            ValidationResult<UpdateCommentCommand> result = commentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("requesterId");
        }

        @Test
        @DisplayName("댓글 내용이 null이면 검증에 실패해야 한다")
        void validateUpdate_WithNullContent_ShouldBeInvalid() {
            // given
            UpdateCommentCommand command = new UpdateCommentCommand(
                    new CommentId("comment-1"),
                    new UserId("user-1"),
                    null);

            // when
            ValidationResult<UpdateCommentCommand> result = commentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("content");
        }

        @Test
        @DisplayName("댓글 내용이 1000자를 초과하면 검증에 실패해야 한다")
        void validateUpdate_WithContentExceedingMaxLength_ShouldBeInvalid() {
            // given
            String longContent = "a".repeat(1001);
            UpdateCommentCommand command = new UpdateCommentCommand(
                    new CommentId("comment-1"),
                    new UserId("user-1"),
                    longContent);

            // when
            ValidationResult<UpdateCommentCommand> result = commentValidator.validateUpdate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("content");
        }
    }

    @Nested
    @DisplayName("DeleteCommentCommand 검증 테스트")
    class DeleteCommentCommandValidationTest {

        @Test
        @DisplayName("유효한 댓글 삭제 커맨드는 검증을 통과해야 한다")
        void validateDelete_WithValidCommand_ShouldBeValid() {
            // given
            DeleteCommentCommand command = new DeleteCommentCommand(
                    new CommentId("comment-1"),
                    new UserId("user-1"));

            // when
            ValidationResult<DeleteCommentCommand> result = commentValidator.validateDelete(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("댓글 ID가 null이면 검증에 실패해야 한다")
        void validateDelete_WithNullCommentId_ShouldBeInvalid() {
            // given
            DeleteCommentCommand command = new DeleteCommentCommand(
                    null,
                    new UserId("user-1"));

            // when
            ValidationResult<DeleteCommentCommand> result = commentValidator.validateDelete(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("commentId");
        }

        @Test
        @DisplayName("요청자 ID가 null이면 검증에 실패해야 한다")
        void validateDelete_WithNullRequesterId_ShouldBeInvalid() {
            // given
            DeleteCommentCommand command = new DeleteCommentCommand(
                    new CommentId("comment-1"),
                    null);

            // when
            ValidationResult<DeleteCommentCommand> result = commentValidator.validateDelete(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).field()).isEqualTo("requesterId");
        }
    }
}