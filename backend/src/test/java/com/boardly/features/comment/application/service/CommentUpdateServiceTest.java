package com.boardly.features.comment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.comment.application.port.input.UpdateCommentCommand;
import com.boardly.features.comment.application.validation.CommentValidator;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentUpdateService 테스트")
class CommentUpdateServiceTest {

        @Mock
        private CommentValidator commentValidator;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @Mock
        private CommentRepository commentRepository;

        private CommentUpdateService commentUpdateService;

        @BeforeEach
        void setUp() {
                commentUpdateService = new CommentUpdateService(
                                commentValidator,
                                validationMessageResolver,
                                commentRepository);

                // 공통으로 사용되는 메시지 설정
                lenient().when(validationMessageResolver.getMessage("validation.comment.update.error"))
                                .thenReturn("댓글 수정 중 오류가 발생했습니다.");
                lenient().when(validationMessageResolver.getMessage("validation.comment.not.found"))
                                .thenReturn("댓글을 찾을 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("validation.comment.update.access.denied"))
                                .thenReturn("댓글 수정 권한이 없습니다.");
        }

        @Nested
        @DisplayName("updateComment 메서드 테스트")
        class UpdateCommentTest {

                private UpdateCommentCommand validCommand;
                private Comment existingComment;
                private CommentId commentId;
                private UserId requesterId;
                private UserId authorId;
                private CardId cardId;
                private String originalContent;
                private String newContent;

                @BeforeEach
                void setUp() {
                        commentId = new CommentId();
                        requesterId = new UserId();
                        authorId = requesterId; // 같은 사용자로 설정 (권한 확인 테스트에서 별도로 변경)
                        cardId = new CardId();
                        originalContent = "기존 댓글 내용";
                        newContent = "새로운 댓글 내용";

                        validCommand = new UpdateCommentCommand(commentId, requesterId, newContent);

                        existingComment = Comment.restore(
                                        commentId,
                                        cardId,
                                        authorId,
                                        originalContent,
                                        false,
                                        Instant.now().minusSeconds(3600),
                                        Instant.now().minusSeconds(3600));
                }

                @Test
                @DisplayName("유효한 커맨드로 댓글 수정 시 성공한다")
                void shouldUpdateCommentSuccessfully() {
                        // given
                        when(commentValidator.validateUpdate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(commentRepository.findById(commentId))
                                        .thenReturn(Optional.of(existingComment));
                        when(commentRepository.save(any(Comment.class)))
                                        .thenReturn(Either.right(existingComment));

                        // when
                        Either<Failure, Comment> result = commentUpdateService.updateComment(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(existingComment);
                        assertThat(existingComment.getContent()).isEqualTo(newContent);
                        assertThat(existingComment.isEdited()).isTrue();

                        verify(commentValidator).validateUpdate(validCommand);
                        verify(commentRepository).findById(commentId);
                        verify(commentRepository).save(existingComment);
                }

                @Test
                @DisplayName("입력 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenValidationFails() {
                        // given
                        var validationError = Failure.FieldViolation.builder()
                                        .field("content")
                                        .message("댓글 내용은 필수입니다")
                                        .rejectedValue("")
                                        .build();
                        ValidationResult<UpdateCommentCommand> validationResult = ValidationResult
                                        .invalid(validationError);

                        when(commentValidator.validateUpdate(validCommand))
                                        .thenReturn(validationResult);

                        // when
                        Either<Failure, Comment> result = commentUpdateService.updateComment(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("댓글 수정 중 오류가 발생했습니다.");

                        verify(commentValidator).validateUpdate(validCommand);
                        verifyNoInteractions(commentRepository);
                }

                @Test
                @DisplayName("댓글이 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenCommentNotFound() {
                        // given
                        when(commentValidator.validateUpdate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(commentRepository.findById(commentId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Comment> result = commentUpdateService.updateComment(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("댓글을 찾을 수 없습니다.");

                        verify(commentValidator).validateUpdate(validCommand);
                        verify(commentRepository).findById(commentId);
                        verify(commentRepository, times(0)).save(any());
                }

                @Test
                @DisplayName("댓글 작성자가 아닌 사용자가 수정 시도 시 실패를 반환한다")
                void shouldReturnFailureWhenNotAuthor() {
                        // given
                        UserId differentUserId = new UserId();
                        var commandWithDifferentUser = new UpdateCommentCommand(commentId, differentUserId, newContent);

                        when(commentValidator.validateUpdate(commandWithDifferentUser))
                                        .thenReturn(ValidationResult.valid(commandWithDifferentUser));
                        when(commentRepository.findById(commentId))
                                        .thenReturn(Optional.of(existingComment));

                        // when
                        Either<Failure, Comment> result = commentUpdateService.updateComment(commandWithDifferentUser);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("댓글 수정 권한이 없습니다.");

                        verify(commentValidator).validateUpdate(commandWithDifferentUser);
                        verify(commentRepository).findById(commentId);
                        verify(commentRepository, times(0)).save(any());
                }

                @Test
                @DisplayName("댓글 저장 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenCommentSaveFails() {
                        // given
                        var saveFailure = Failure.ofInternalServerError("저장 중 오류가 발생했습니다.");

                        when(commentValidator.validateUpdate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(commentRepository.findById(commentId))
                                        .thenReturn(Optional.of(existingComment));
                        when(commentRepository.save(any(Comment.class)))
                                        .thenReturn(Either.left(saveFailure));

                        // when
                        Either<Failure, Comment> result = commentUpdateService.updateComment(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isEqualTo(saveFailure);

                        verify(commentValidator).validateUpdate(validCommand);
                        verify(commentRepository).findById(commentId);
                        verify(commentRepository).save(existingComment);
                }

                @Test
                @DisplayName("댓글 내용이 공백으로만 구성되어 있을 때 정상 처리된다")
                void shouldHandleWhitespaceOnlyContent() {
                        // given
                        String whitespaceContent = "   ";
                        var commandWithWhitespace = new UpdateCommentCommand(commentId, requesterId, whitespaceContent);

                        when(commentValidator.validateUpdate(commandWithWhitespace))
                                        .thenReturn(ValidationResult.valid(commandWithWhitespace));
                        when(commentRepository.findById(commentId))
                                        .thenReturn(Optional.of(existingComment));
                        when(commentRepository.save(any(Comment.class)))
                                        .thenReturn(Either.right(existingComment));

                        // when
                        Either<Failure, Comment> result = commentUpdateService.updateComment(commandWithWhitespace);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(existingComment.getContent()).isEqualTo(""); // trim된 결과
                        assertThat(existingComment.isEdited()).isTrue();

                        verify(commentValidator).validateUpdate(commandWithWhitespace);
                        verify(commentRepository).findById(commentId);
                        verify(commentRepository).save(existingComment);
                }

                @Test
                @DisplayName("댓글 내용이 빈 문자열일 때 정상 처리된다")
                void shouldHandleEmptyContent() {
                        // given
                        var commandWithEmptyContent = new UpdateCommentCommand(commentId, requesterId, "");

                        when(commentValidator.validateUpdate(commandWithEmptyContent))
                                        .thenReturn(ValidationResult.valid(commandWithEmptyContent));
                        when(commentRepository.findById(commentId))
                                        .thenReturn(Optional.of(existingComment));
                        when(commentRepository.save(any(Comment.class)))
                                        .thenReturn(Either.right(existingComment));

                        // when
                        Either<Failure, Comment> result = commentUpdateService.updateComment(commandWithEmptyContent);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(existingComment.getContent()).isEqualTo(""); // 빈 문자열로 처리됨
                        assertThat(existingComment.isEdited()).isTrue();

                        verify(commentValidator).validateUpdate(commandWithEmptyContent);
                        verify(commentRepository).findById(commentId);
                        verify(commentRepository).save(existingComment);
                }

                @Test
                @DisplayName("이미 수정된 댓글을 다시 수정할 때 edited 플래그가 true로 유지된다")
                void shouldMaintainEditedFlagWhenUpdatingAlreadyEditedComment() {
                        // given
                        Comment editedComment = Comment.restore(
                                        commentId,
                                        cardId,
                                        authorId,
                                        originalContent,
                                        true, // 이미 수정됨
                                        Instant.now().minusSeconds(3600),
                                        Instant.now().minusSeconds(1800));

                        when(commentValidator.validateUpdate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(commentRepository.findById(commentId))
                                        .thenReturn(Optional.of(editedComment));
                        when(commentRepository.save(any(Comment.class)))
                                        .thenReturn(Either.right(editedComment));

                        // when
                        Either<Failure, Comment> result = commentUpdateService.updateComment(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(editedComment.getContent()).isEqualTo(newContent);
                        assertThat(editedComment.isEdited()).isTrue(); // 여전히 true

                        verify(commentValidator).validateUpdate(validCommand);
                        verify(commentRepository).findById(commentId);
                        verify(commentRepository).save(editedComment);
                }
        }
}