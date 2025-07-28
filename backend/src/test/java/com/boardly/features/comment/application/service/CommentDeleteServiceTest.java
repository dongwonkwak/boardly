package com.boardly.features.comment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.comment.application.port.input.DeleteCommentCommand;
import com.boardly.features.comment.application.validation.CommentValidator;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentDeleteService 테스트")
class CommentDeleteServiceTest {

    @Mock
    private CommentValidator commentValidator;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentDeleteService commentDeleteService;

    private CommentId commentId;
    private UserId requesterId;
    private UserId authorId;
    private CardId cardId;
    private DeleteCommentCommand command;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentId = new CommentId("comment-123");
        requesterId = new UserId("user-123");
        authorId = new UserId("user-123"); // 같은 사용자로 설정
        cardId = new CardId("card-123");

        command = new DeleteCommentCommand(commentId, requesterId);

        comment = Comment.restore(
                commentId,
                cardId,
                authorId,
                "테스트 댓글 내용",
                false,
                Instant.now(),
                Instant.now());
    }

    @Nested
    @DisplayName("성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("댓글 삭제가 성공적으로 완료되어야 한다")
        void deleteComment_WithValidInput_ShouldSucceed() {
            // given
            ValidationResult<DeleteCommentCommand> validResult = ValidationResult.valid(command);

            when(commentValidator.validateDelete(command)).thenReturn(validResult);
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(commentRepository.delete(commentId)).thenReturn(Either.right(null));

            // when
            Either<Failure, Void> result = commentDeleteService.deleteComment(command);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isNull();

            // verify
            verify(commentValidator).validateDelete(command);
            verify(commentRepository).findById(commentId);
            verify(commentRepository).delete(commentId);
        }
    }

    @Nested
    @DisplayName("검증 실패 케이스")
    class ValidationFailureCases {

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void deleteComment_WithInvalidInput_ShouldReturnInputError() {
            // given
            String errorMessage = "댓글 삭제 입력 검증에 실패했습니다";
            ValidationResult<DeleteCommentCommand> invalidResult = ValidationResult.invalid(
                    "commentId", "validation.comment.id.required", commentId);

            when(commentValidator.validateDelete(command)).thenReturn(invalidResult);
            when(validationMessageResolver.getMessage("error.service.comment.delete.validation"))
                    .thenReturn(errorMessage);

            // when
            Either<Failure, Void> result = commentDeleteService.deleteComment(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);

            // verify
            verify(commentValidator).validateDelete(command);
            verify(commentRepository, never()).findById(any());
            verify(commentRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("댓글 없음 케이스")
    class CommentNotFoundCases {

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시도 시 NotFound를 반환해야 한다")
        void deleteComment_WithNonExistentComment_ShouldReturnNotFound() {
            // given
            ValidationResult<DeleteCommentCommand> validResult = ValidationResult.valid(command);
            String errorMessage = "삭제할 댓글을 찾을 수 없습니다";

            when(commentValidator.validateDelete(command)).thenReturn(validResult);
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.comment.delete.not.found"))
                    .thenReturn(errorMessage);

            // when
            Either<Failure, Void> result = commentDeleteService.deleteComment(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);

            // verify
            verify(commentValidator).validateDelete(command);
            verify(commentRepository).findById(commentId);
            verify(commentRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("권한 거부 케이스")
    class PermissionDeniedCases {

        @Test
        @DisplayName("댓글 작성자가 아닌 사용자가 삭제 시도 시 PermissionDenied를 반환해야 한다")
        void deleteComment_WithUnauthorizedUser_ShouldReturnPermissionDenied() {
            // given
            UserId differentUserId = new UserId("user-456");
            DeleteCommentCommand unauthorizedCommand = new DeleteCommentCommand(commentId, differentUserId);
            ValidationResult<DeleteCommentCommand> validResult = ValidationResult.valid(unauthorizedCommand);
            String errorMessage = "댓글 삭제 권한이 없습니다";

            when(commentValidator.validateDelete(unauthorizedCommand)).thenReturn(validResult);
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(validationMessageResolver.getMessage("error.service.comment.delete.unauthorized"))
                    .thenReturn(errorMessage);
            when(validationMessageResolver.getMessage("error.service.comment.delete.not.found"))
                    .thenReturn("삭제할 댓글을 찾을 수 없습니다");

            // when
            Either<Failure, Void> result = commentDeleteService.deleteComment(unauthorizedCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);

            // verify
            verify(commentValidator).validateDelete(unauthorizedCommand);
            verify(commentRepository).findById(commentId);
            verify(commentRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("삭제 실패 케이스")
    class DeleteFailureCases {

        @Test
        @DisplayName("댓글 삭제 중 오류 발생 시 해당 오류를 반환해야 한다")
        void deleteComment_WithDeleteFailure_ShouldReturnDeleteError() {
            // given
            ValidationResult<DeleteCommentCommand> validResult = ValidationResult.valid(command);
            String deleteErrorMessage = "데이터베이스 오류가 발생했습니다";
            Failure deleteFailure = Failure.ofInternalServerError(deleteErrorMessage);

            when(commentValidator.validateDelete(command)).thenReturn(validResult);
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(commentRepository.delete(commentId)).thenReturn(Either.left(deleteFailure));

            // when
            Either<Failure, Void> result = commentDeleteService.deleteComment(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(deleteFailure);

            // verify
            verify(commentValidator).validateDelete(command);
            verify(commentRepository).findById(commentId);
            verify(commentRepository).delete(commentId);
        }
    }

    @Nested
    @DisplayName("메서드 체이닝 테스트")
    class MethodChainingTests {

        @Test
        @DisplayName("각 단계가 순서대로 실행되어야 한다")
        void deleteComment_ShouldExecuteStepsInOrder() {
            // given
            ValidationResult<DeleteCommentCommand> validResult = ValidationResult.valid(command);

            when(commentValidator.validateDelete(command)).thenReturn(validResult);
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(commentRepository.delete(commentId)).thenReturn(Either.right(null));

            // when
            Either<Failure, Void> result = commentDeleteService.deleteComment(command);

            // then
            assertThat(result.isRight()).isTrue();

            // verify 순서 확인
            verify(commentValidator).validateDelete(command);
            verify(commentRepository).findById(commentId);
            verify(commentRepository).delete(commentId);
        }

        @Test
        @DisplayName("검증 실패 시 후속 단계가 실행되지 않아야 한다")
        void deleteComment_WithValidationFailure_ShouldNotExecuteSubsequentSteps() {
            // given
            ValidationResult<DeleteCommentCommand> invalidResult = ValidationResult.invalid(
                    "commentId", "validation.comment.id.required", commentId);
            String errorMessage = "댓글 삭제 입력 검증에 실패했습니다";

            when(commentValidator.validateDelete(command)).thenReturn(invalidResult);
            when(validationMessageResolver.getMessage("error.service.comment.delete.validation"))
                    .thenReturn(errorMessage);

            // when
            Either<Failure, Void> result = commentDeleteService.deleteComment(command);

            // then
            assertThat(result.isLeft()).isTrue();

            // verify 후속 단계가 실행되지 않음
            verify(commentValidator).validateDelete(command);
            verify(commentRepository, never()).findById(any());
            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("댓글 없음 시 삭제 단계가 실행되지 않아야 한다")
        void deleteComment_WithCommentNotFound_ShouldNotExecuteDeleteStep() {
            // given
            ValidationResult<DeleteCommentCommand> validResult = ValidationResult.valid(command);
            String errorMessage = "삭제할 댓글을 찾을 수 없습니다";

            when(commentValidator.validateDelete(command)).thenReturn(validResult);
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.comment.delete.not.found"))
                    .thenReturn(errorMessage);

            // when
            Either<Failure, Void> result = commentDeleteService.deleteComment(command);

            // then
            assertThat(result.isLeft()).isTrue();

            // verify 삭제 단계가 실행되지 않음
            verify(commentValidator).validateDelete(command);
            verify(commentRepository).findById(commentId);
            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("권한 거부 시 삭제 단계가 실행되지 않아야 한다")
        void deleteComment_WithPermissionDenied_ShouldNotExecuteDeleteStep() {
            // given
            UserId differentUserId = new UserId("user-456");
            DeleteCommentCommand unauthorizedCommand = new DeleteCommentCommand(commentId, differentUserId);
            ValidationResult<DeleteCommentCommand> validResult = ValidationResult.valid(unauthorizedCommand);
            String errorMessage = "댓글 삭제 권한이 없습니다";

            when(commentValidator.validateDelete(unauthorizedCommand)).thenReturn(validResult);
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(validationMessageResolver.getMessage("error.service.comment.delete.unauthorized"))
                    .thenReturn(errorMessage);
            when(validationMessageResolver.getMessage("error.service.comment.delete.not.found"))
                    .thenReturn("삭제할 댓글을 찾을 수 없습니다");

            // when
            Either<Failure, Void> result = commentDeleteService.deleteComment(unauthorizedCommand);

            // then
            assertThat(result.isLeft()).isTrue();

            // verify 삭제 단계가 실행되지 않음
            verify(commentValidator).validateDelete(unauthorizedCommand);
            verify(commentRepository).findById(commentId);
            verify(commentRepository, never()).delete(any());
        }
    }
}