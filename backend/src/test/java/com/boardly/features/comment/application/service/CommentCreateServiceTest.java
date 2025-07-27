package com.boardly.features.comment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.comment.application.port.input.CreateCommentCommand;
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
@DisplayName("CommentCreateService 테스트")
class CommentCreateServiceTest {

    @Mock
    private CommentValidator commentValidator;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ActivityHelper activityHelper;

    private CommentCreateService commentCreateService;

    @BeforeEach
    void setUp() {
        commentCreateService = new CommentCreateService(
                commentValidator,
                validationMessageResolver,
                commentRepository,
                cardRepository,
                boardListRepository,
                boardRepository,
                activityHelper);

        // 공통으로 사용되는 메시지 설정
        lenient().when(validationMessageResolver.getMessage("error.service.comment.create.validation"))
                .thenReturn("댓글 생성 검증에 실패했습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.comment.create.card.not.found"))
                .thenReturn("카드를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.comment.create.boardlist.not.found"))
                .thenReturn("보드 리스트를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.comment.create.card.fetch.failed"))
                .thenReturn("카드 조회에 실패했습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.comment.create.boardlist.fetch.failed"))
                .thenReturn("보드 리스트 조회에 실패했습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.comment.create.save.failed"))
                .thenReturn("댓글 저장에 실패했습니다.");
    }

    @Nested
    @DisplayName("createComment 메서드 테스트")
    class CreateCommentTest {

        private CreateCommentCommand validCommand;
        private Card card;
        private BoardList boardList;
        private Comment createdComment;
        private UserId authorId;
        private CardId cardId;
        private ListId listId;
        private BoardId boardId;

        @BeforeEach
        void setUp() {
            authorId = new UserId("user-1");
            cardId = new CardId("card-1");
            listId = new ListId("list-1");
            boardId = new BoardId("board-1");

            validCommand = new CreateCommentCommand(
                    cardId,
                    authorId,
                    "테스트 댓글 내용");

            card = Card.restore(
                    cardId,
                    "테스트 카드",
                    "카드 설명",
                    0,
                    null,
                    false,
                    listId,
                    null,
                    0,
                    0,
                    0,
                    null,
                    null);

            boardList = BoardList.create(
                    "테스트 리스트",
                    0,
                    boardId);

            createdComment = Comment.create(cardId, authorId, "테스트 댓글 내용");
        }

        @Test
        @DisplayName("유효한 커맨드로 댓글 생성 시 성공한다")
        void shouldCreateCommentSuccessfully() {
            // given
            ValidationResult<CreateCommentCommand> validValidationResult = ValidationResult.valid(validCommand);
            when(commentValidator.validateCreate(validCommand)).thenReturn(validValidationResult);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(commentRepository.save(any(Comment.class))).thenReturn(Either.right(createdComment));

            // when
            Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(createdComment);

            // 검증
            verify(commentValidator).validateCreate(validCommand);
            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(listId);
            verify(commentRepository).save(any(Comment.class));
            verify(activityHelper).logCardActivity(
                    eq(ActivityType.CARD_ADD_COMMENT),
                    eq(authorId),
                    any(Map.class),
                    any(String.class), // boardName
                    eq(boardId),
                    eq(listId),
                    eq(cardId));
        }

        @Test
        @DisplayName("입력 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenValidationFails() {
            // given
            var fieldViolation = Failure.FieldViolation.builder()
                    .field("content")
                    .message("댓글 내용은 필수입니다.")
                    .rejectedValue(null)
                    .build();
            ValidationResult<CreateCommentCommand> invalidValidationResult = ValidationResult
                    .invalid(fieldViolation);
            when(commentValidator.validateCreate(validCommand)).thenReturn(invalidValidationResult);

            // when
            Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("댓글 생성 검증에 실패했습니다.");

            // 다른 의존성들이 호출되지 않았는지 확인
            verifyNoInteractions(cardRepository, boardListRepository, commentRepository, activityHelper);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            ValidationResult<CreateCommentCommand> validValidationResult = ValidationResult.valid(validCommand);
            when(commentValidator.validateCreate(validCommand)).thenReturn(validValidationResult);
            when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("카드를 찾을 수 없습니다.");

            // 다른 의존성들이 호출되지 않았는지 확인
            verifyNoInteractions(boardListRepository, commentRepository, activityHelper);
        }

        @Test
        @DisplayName("카드 조회 시 예외 발생 시 실패를 반환한다")
        void shouldReturnFailureWhenCardFetchThrowsException() {
            // given
            ValidationResult<CreateCommentCommand> validValidationResult = ValidationResult.valid(validCommand);
            when(commentValidator.validateCreate(validCommand)).thenReturn(validValidationResult);
            when(cardRepository.findById(cardId)).thenThrow(new RuntimeException("Database error"));

            // when
            Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 조회에 실패했습니다.");

            // 다른 의존성들이 호출되지 않았는지 확인
            verifyNoInteractions(boardListRepository, commentRepository, activityHelper);
        }

        @Test
        @DisplayName("보드 리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardListNotFound() {
            // given
            ValidationResult<CreateCommentCommand> validValidationResult = ValidationResult.valid(validCommand);
            when(commentValidator.validateCreate(validCommand)).thenReturn(validValidationResult);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("보드 리스트를 찾을 수 없습니다.");

            // 다른 의존성들이 호출되지 않았는지 확인
            verifyNoInteractions(commentRepository, activityHelper);
        }

        @Test
        @DisplayName("보드 리스트 조회 시 예외 발생 시 실패를 반환한다")
        void shouldReturnFailureWhenBoardListFetchThrowsException() {
            // given
            ValidationResult<CreateCommentCommand> validValidationResult = ValidationResult.valid(validCommand);
            when(commentValidator.validateCreate(validCommand)).thenReturn(validValidationResult);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(boardListRepository.findById(listId)).thenThrow(new RuntimeException("Database error"));

            // when
            Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("보드 리스트 조회에 실패했습니다.");

            // 다른 의존성들이 호출되지 않았는지 확인
            verifyNoInteractions(commentRepository, activityHelper);
        }

        @Test
        @DisplayName("댓글 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCommentSaveFails() {
            // given
            ValidationResult<CreateCommentCommand> validValidationResult = ValidationResult.valid(validCommand);
            when(commentValidator.validateCreate(validCommand)).thenReturn(validValidationResult);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(commentRepository.save(any(Comment.class)))
                    .thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

            // when
            Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("댓글 저장에 실패했습니다.");

            // 활동 로그가 호출되지 않았는지 확인
            verifyNoInteractions(activityHelper);
        }

        @Test
        @DisplayName("활동 로그에 올바른 페이로드가 전달된다")
        void shouldPassCorrectPayloadToActivityLog() {
            // given
            ValidationResult<CreateCommentCommand> validValidationResult = ValidationResult.valid(validCommand);
            when(commentValidator.validateCreate(validCommand)).thenReturn(validValidationResult);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(commentRepository.save(any(Comment.class))).thenReturn(Either.right(createdComment));

            // when
            commentCreateService.createComment(validCommand);

            // then
            verify(activityHelper).logCardActivity(
                    eq(ActivityType.CARD_ADD_COMMENT),
                    eq(authorId),
                    argThat(payload -> {
                        assertThat(payload).containsKey("commentId");
                        assertThat(payload).containsKey("content");
                        assertThat(payload).containsKey("cardTitle");
                        assertThat(payload).containsKey("cardId");
                        assertThat(payload.get("content")).isEqualTo("테스트 댓글 내용");
                        assertThat(payload.get("cardTitle")).isEqualTo("테스트 카드");
                        assertThat(payload.get("cardId")).isEqualTo("card-1");
                        return true;
                    }),
                    any(String.class), // boardName
                    eq(boardId),
                    eq(listId),
                    eq(cardId));
        }

        @Test
        @DisplayName("댓글 생성 시 Comment.create 팩토리 메서드가 올바른 파라미터로 호출된다")
        void shouldCreateCommentWithCorrectParameters() {
            // given
            ValidationResult<CreateCommentCommand> validValidationResult = ValidationResult.valid(validCommand);
            when(commentValidator.validateCreate(validCommand)).thenReturn(validValidationResult);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(commentRepository.save(any(Comment.class))).thenReturn(Either.right(createdComment));

            // when
            commentCreateService.createComment(validCommand);

            // then
            verify(commentRepository).save(argThat(comment -> {
                assertThat(comment.getCardId()).isEqualTo(cardId);
                assertThat(comment.getAuthorId()).isEqualTo(authorId);
                assertThat(comment.getContent()).isEqualTo("테스트 댓글 내용");
                assertThat(comment.isEdited()).isFalse();
                return true;
            }));
        }
    }
}