package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.domain.Board;
import com.boardly.shared.common.value.BoardId;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.shared.common.value.ListId;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.card.application.command.DeleteCardCommand;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.Card;
import com.boardly.shared.common.value.CardId;
import com.boardly.features.card.domain.port.CardRepository;
import com.boardly.features.comment.domain.port.CommentRepository;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCardService - 삭제 실패 테스트")
class DeleteCardServiceDeletionFailureTest {

    @Mock
    private CardValidator cardValidator;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MessageResolver validationMessageResolver;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ActivityHelper activityHelper;

    private DeleteCardService deleteCardService;

    @BeforeEach
    void setUp() {
        deleteCardService = new DeleteCardService(
                cardValidator,
                cardRepository,
                boardListRepository,
                boardRepository,
                commentRepository,
                validationMessageResolver,
                activityHelper);
    }

    @Test
    @DisplayName("카드 삭제 실패 시 실패를 반환한다")
    void shouldReturnFailureWhenCardDeletionFails() {
        // given
        UserId userId = new UserId("user-123");
        CardId cardId = new CardId("card-123");
        ListId listId = new ListId("list-123");
        BoardId boardId = new BoardId("board-123");
        DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

        Card cardToDelete = Card.builder()
                .cardId(cardId)
                .title("테스트 카드")
                .listId(listId)
                .position(1)
                .build();

        BoardList boardList = BoardList.builder()
                .listId(listId)
                .title("테스트 리스트")
                .boardId(boardId)
                .position(1)
                .build();

        Board board = Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .ownerId(userId)
                .isArchived(false)
                .build();

        Failure deletionFailure = Failure.ofInternalError("카드 삭제 중 오류가 발생했습니다.", "INTERNAL_ERROR", null);

        when(cardValidator.validateDelete(command))
                .thenReturn(ValidationResult.valid(command));
        when(cardRepository.findById(cardId))
                .thenReturn(Optional.of(cardToDelete));
        when(boardListRepository.findById(listId))
                .thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                .thenReturn(Optional.of(board));
        when(commentRepository.deleteByCardId(cardId))
                .thenReturn(Either.right(null));
        when(cardRepository.delete(cardId))
                .thenReturn(Either.left(deletionFailure));

        // when
        Either<Failure, Void> result = deleteCardService.deleteCard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(deletionFailure);

        verify(cardValidator).validateDelete(command);
        verify(cardRepository).findById(cardId);
        verify(boardListRepository).findById(listId);
        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
        verify(commentRepository).deleteByCardId(cardId);
        verify(cardRepository).delete(cardId);
        verifyNoInteractions(activityHelper);
    }

    @Test
    @DisplayName("댓글 삭제 실패 시 실패를 반환한다")
    void shouldReturnFailureWhenCommentDeletionFails() {
        // given
        UserId userId = new UserId("user-123");
        CardId cardId = new CardId("card-123");
        ListId listId = new ListId("list-123");
        BoardId boardId = new BoardId("board-123");
        DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

        Card cardToDelete = Card.builder()
                .cardId(cardId)
                .title("테스트 카드")
                .listId(listId)
                .position(1)
                .build();

        BoardList boardList = BoardList.builder()
                .listId(listId)
                .title("테스트 리스트")
                .boardId(boardId)
                .position(1)
                .build();

        Board board = Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .ownerId(userId)
                .isArchived(false)
                .build();

        Failure commentDeletionFailure = Failure.ofInternalError("댓글 삭제 중 오류가 발생했습니다.", "INTERNAL_ERROR", null);

        when(cardValidator.validateDelete(command))
                .thenReturn(ValidationResult.valid(command));
        when(cardRepository.findById(cardId))
                .thenReturn(Optional.of(cardToDelete));
        when(boardListRepository.findById(listId))
                .thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                .thenReturn(Optional.of(board));
        when(commentRepository.deleteByCardId(cardId))
                .thenReturn(Either.left(commentDeletionFailure));

        // when
        Either<Failure, Void> result = deleteCardService.deleteCard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(commentDeletionFailure);

        verify(cardValidator).validateDelete(command);
        verify(cardRepository).findById(cardId);
        verify(boardListRepository).findById(listId);
        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
        verify(commentRepository).deleteByCardId(cardId);
        verifyNoInteractions(activityHelper);
    }
}