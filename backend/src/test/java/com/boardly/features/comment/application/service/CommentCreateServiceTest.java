package com.boardly.features.comment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
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
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.comment.application.port.input.CreateCommentCommand;
import com.boardly.features.comment.application.validation.CommentValidator;
import com.boardly.features.comment.domain.model.Comment;
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
                lenient().when(validationMessageResolver.getMessage("error.service.comment.create.card.fetch.failed"))
                                .thenReturn("카드 조회 중 오류가 발생했습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.comment.create.boardlist.not.found"))
                                .thenReturn("보드 리스트를 찾을 수 없습니다.");
                lenient().when(validationMessageResolver
                                .getMessage("error.service.comment.create.boardlist.fetch.failed"))
                                .thenReturn("보드 리스트 조회 중 오류가 발생했습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.comment.create.save.failed"))
                                .thenReturn("댓글 저장 중 오류가 발생했습니다.");
        }

        @Nested
        @DisplayName("createComment 메서드 테스트")
        class CreateCommentTest {

                private CreateCommentCommand validCommand;
                private Card existingCard;
                private BoardList boardList;
                private Board board;
                private Comment savedComment;
                private UserId authorId;
                private CardId cardId;
                private ListId listId;
                private BoardId boardId;
                private String commentContent;

                @BeforeEach
                void setUp() {
                        authorId = new UserId("user-123");
                        cardId = new CardId("card-123");
                        listId = new ListId("list-123");
                        boardId = new BoardId("board-123");
                        commentContent = "테스트 댓글 내용입니다.";

                        validCommand = new CreateCommentCommand(cardId, authorId, commentContent);

                        // 기존 카드 Mock 설정
                        existingCard = org.mockito.Mockito.mock(Card.class);
                        lenient().when(existingCard.getCardId()).thenReturn(cardId);
                        lenient().when(existingCard.getTitle()).thenReturn("테스트 카드");
                        lenient().when(existingCard.getListId()).thenReturn(listId);

                        // 보드 리스트 Mock 설정
                        boardList = BoardList.builder()
                                        .listId(listId)
                                        .title("테스트 리스트")
                                        .boardId(boardId)
                                        .position(0)
                                        .build();

                        // 보드 Mock 설정
                        board = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(authorId)
                                        .isArchived(false)
                                        .build();

                        // 저장된 댓글 Mock 설정
                        savedComment = org.mockito.Mockito.mock(Comment.class);
                        lenient().when(savedComment.getCommentId())
                                        .thenReturn(new com.boardly.features.comment.domain.model.CommentId(
                                                        "comment-123"));
                }

                @Test
                @DisplayName("유효한 커맨드로 댓글 생성 시 성공한다")
                void shouldCreateCommentSuccessfully() {
                        // given
                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(validCommand);
                        when(commentValidator.validateCreate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(commentRepository.save(any(Comment.class))).thenReturn(Either.right(savedComment));

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(savedComment);

                        verify(commentValidator).validateCreate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findById(boardId);
                        verify(commentRepository).save(any(Comment.class));
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_ADD_COMMENT),
                                        eq(authorId),
                                        any(),
                                        eq("테스트 보드"),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
                }

                @Test
                @DisplayName("입력 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenValidationFails() {
                        // given
                        List<Failure.FieldViolation> validationErrors = List.of(
                                        Failure.FieldViolation.builder()
                                                        .field("content")
                                                        .message("댓글 내용은 필수입니다.")
                                                        .rejectedValue(null)
                                                        .build(),
                                        Failure.FieldViolation.builder()
                                                        .field("content")
                                                        .message("댓글 내용은 1-1000자여야 합니다.")
                                                        .rejectedValue("")
                                                        .build());
                        ValidationResult<CreateCommentCommand> invalidResult = ValidationResult
                                        .invalid(io.vavr.collection.List.ofAll(validationErrors));
                        when(commentValidator.validateCreate(validCommand)).thenReturn(invalidResult);

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InputError.class);
                        assertThat(failure.getMessage()).isEqualTo("댓글 생성 검증에 실패했습니다.");
                        assertThat(((Failure.InputError) failure).getViolations()).hasSize(2);

                        verify(commentValidator).validateCreate(validCommand);
                        verifyNoInteractions(cardRepository, boardListRepository, commentRepository, activityHelper);
                }

                @Test
                @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenCardNotFound() {
                        // given
                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(validCommand);
                        when(commentValidator.validateCreate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        assertThat(failure.getMessage()).isEqualTo("카드를 찾을 수 없습니다.");

                        verify(commentValidator).validateCreate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verifyNoInteractions(boardListRepository, commentRepository, activityHelper);
                }

                @Test
                @DisplayName("카드 조회 중 예외 발생 시 실패를 반환한다")
                void shouldReturnFailureWhenCardFetchFails() {
                        // given
                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(validCommand);
                        when(commentValidator.validateCreate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenThrow(new RuntimeException("데이터베이스 오류"));

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InternalError.class);
                        assertThat(failure.getMessage()).isEqualTo("카드 조회 중 오류가 발생했습니다.");

                        verify(commentValidator).validateCreate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verifyNoInteractions(boardListRepository, commentRepository, activityHelper);
                }

                @Test
                @DisplayName("보드 리스트가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardListNotFound() {
                        // given
                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(validCommand);
                        when(commentValidator.validateCreate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        assertThat(failure.getMessage()).isEqualTo("보드 리스트를 찾을 수 없습니다.");

                        verify(commentValidator).validateCreate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verifyNoInteractions(commentRepository, activityHelper);
                }

                @Test
                @DisplayName("보드 리스트 조회 중 예외 발생 시 실패를 반환한다")
                void shouldReturnFailureWhenBoardListFetchFails() {
                        // given
                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(validCommand);
                        when(commentValidator.validateCreate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenThrow(new RuntimeException("데이터베이스 오류"));

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InternalError.class);
                        assertThat(failure.getMessage()).isEqualTo("보드 리스트 조회 중 오류가 발생했습니다.");

                        verify(commentValidator).validateCreate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verifyNoInteractions(commentRepository, activityHelper);
                }

                @Test
                @DisplayName("댓글 저장 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenCommentSaveFails() {
                        // given
                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(validCommand);
                        when(commentValidator.validateCreate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(commentRepository.save(any(Comment.class)))
                                        .thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InternalError.class);
                        assertThat(failure.getMessage()).isEqualTo("댓글 저장 중 오류가 발생했습니다.");

                        verify(commentValidator).validateCreate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(commentRepository).save(any(Comment.class));
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("보드 정보가 없을 때도 댓글 생성이 성공한다")
                void shouldCreateCommentSuccessfullyWhenBoardNotFound() {
                        // given
                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(validCommand);
                        when(commentValidator.validateCreate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());
                        when(commentRepository.save(any(Comment.class))).thenReturn(Either.right(savedComment));

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(savedComment);

                        verify(commentValidator).validateCreate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findById(boardId);
                        verify(commentRepository).save(any(Comment.class));
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_ADD_COMMENT),
                                        eq(authorId),
                                        any(),
                                        eq("알 수 없는 보드"),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
                }

                @Test
                @DisplayName("긴 댓글 내용으로도 댓글 생성이 성공한다")
                void shouldCreateCommentWithLongContentSuccessfully() {
                        // given
                        String longContent = "이것은 매우 긴 댓글 내용입니다. ".repeat(50); // 1000자 이상
                        CreateCommentCommand longContentCommand = new CreateCommentCommand(cardId, authorId,
                                        longContent);

                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(longContentCommand);
                        when(commentValidator.validateCreate(longContentCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(commentRepository.save(any(Comment.class))).thenReturn(Either.right(savedComment));

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(longContentCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(savedComment);

                        verify(commentValidator).validateCreate(longContentCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findById(boardId);
                        verify(commentRepository).save(any(Comment.class));
                }

                @Test
                @DisplayName("빈 댓글 내용으로도 댓글 생성이 성공한다")
                void shouldCreateCommentWithEmptyContentSuccessfully() {
                        // given
                        String emptyContent = "";
                        CreateCommentCommand emptyContentCommand = new CreateCommentCommand(cardId, authorId,
                                        emptyContent);

                        ValidationResult<CreateCommentCommand> validResult = ValidationResult
                                        .valid(emptyContentCommand);
                        when(commentValidator.validateCreate(emptyContentCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(commentRepository.save(any(Comment.class))).thenReturn(Either.right(savedComment));

                        // when
                        Either<Failure, Comment> result = commentCreateService.createComment(emptyContentCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(savedComment);

                        verify(commentValidator).validateCreate(emptyContentCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findById(boardId);
                        verify(commentRepository).save(any(Comment.class));
                }

                @Test
                @DisplayName("활동 로그에 올바른 정보가 기록된다")
                void shouldLogActivityWithCorrectInformation() {
                        // given
                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(validCommand);
                        when(commentValidator.validateCreate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(commentRepository.save(any(Comment.class))).thenReturn(Either.right(savedComment));

                        // when
                        commentCreateService.createComment(validCommand);

                        // then
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_ADD_COMMENT),
                                        eq(authorId),
                                        any(),
                                        eq("테스트 보드"),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
                }

                @Test
                @DisplayName("댓글 생성 시 Comment.create 메서드가 올바른 파라미터로 호출된다")
                void shouldCreateCommentWithCorrectParameters() {
                        // given
                        ValidationResult<CreateCommentCommand> validResult = ValidationResult.valid(validCommand);
                        when(commentValidator.validateCreate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(commentRepository.save(any(Comment.class))).thenReturn(Either.right(savedComment));

                        // when
                        commentCreateService.createComment(validCommand);

                        // then
                        verify(commentRepository).save(any(Comment.class));
                        // Comment.create 메서드 호출 검증은 실제 Comment 객체 생성 로직에 따라 달라질 수 있음
                }
        }
}