package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCardService - 카드 재정렬 테스트")
class DeleteCardServiceReorderTest {

    @Mock
    private CardValidator cardValidator;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

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
    @DisplayName("카드 삭제 후 나머지 카드들의 위치를 재정렬한다")
    void shouldReorderRemainingCardsAfterDeletion() {
        // given
        UserId userId = new UserId("user-123");
        CardId cardId = new CardId("card-123");
        ListId listId = new ListId("list-123");
        BoardId boardId = new BoardId("board-123");
        DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

        Card cardToDelete = Card.builder()
                .cardId(cardId)
                .title("삭제될 카드")
                .listId(listId)
                .position(2)
                .build();

        Card remainingCard1 = Card.builder()
                .cardId(new CardId("card-456"))
                .title("남은 카드 1")
                .listId(listId)
                .position(3)
                .build();

        Card remainingCard2 = Card.builder()
                .cardId(new CardId("card-789"))
                .title("남은 카드 2")
                .listId(listId)
                .position(4)
                .build();

        List<Card> cardsToReorder = List.of(remainingCard1, remainingCard2);

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
                .thenReturn(Either.right(null));
        when(cardRepository.findByListIdAndPositionGreaterThan(listId, 2))
                .thenReturn(cardsToReorder);
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        // when
        Either<Failure, Void> result = deleteCardService.deleteCard(command);

        // then
        assertThat(result.isRight()).isTrue();

        verify(cardRepository).findByListIdAndPositionGreaterThan(listId, 2);
        verify(cardRepository).saveAll(cardsToReorder);

        // 위치가 재정렬되었는지 확인
        assertThat(remainingCard1.getPosition()).isEqualTo(2);
        assertThat(remainingCard2.getPosition()).isEqualTo(3);
    }

    @Test
    @DisplayName("재정렬할 카드가 없을 때 아무것도 하지 않는다")
    void shouldDoNothingWhenNoCardsToReorder() {
        // given
        UserId userId = new UserId("user-123");
        CardId cardId = new CardId("card-123");
        ListId listId = new ListId("list-123");
        BoardId boardId = new BoardId("board-123");
        DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

        Card cardToDelete = Card.builder()
                .cardId(cardId)
                .title("삭제될 카드")
                .listId(listId)
                .position(2)
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
                .thenReturn(Either.right(null));
        when(cardRepository.findByListIdAndPositionGreaterThan(listId, 2))
                .thenReturn(List.of());
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        // when
        Either<Failure, Void> result = deleteCardService.deleteCard(command);

        // then
        assertThat(result.isRight()).isTrue();
        verify(cardRepository).findByListIdAndPositionGreaterThan(listId, 2);
        verify(cardRepository, times(0)).saveAll(any());
    }

    @Test
    @DisplayName("재정렬 중 예외가 발생해도 삭제는 성공한다")
    void shouldSucceedEvenWhenReorderThrowsException() {
        // given
        UserId userId = new UserId("user-123");
        CardId cardId = new CardId("card-123");
        ListId listId = new ListId("list-123");
        BoardId boardId = new BoardId("board-123");
        DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

        Card cardToDelete = Card.builder()
                .cardId(cardId)
                .title("삭제될 카드")
                .listId(listId)
                .position(2)
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
                .thenReturn(Either.right(null));
        lenient().when(cardRepository.findByListIdAndPositionGreaterThan(listId, 2))
                .thenThrow(new RuntimeException("재정렬 중 오류"));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        // when
        Either<Failure, Void> result = deleteCardService.deleteCard(command);

        // then
        assertThat(result.isRight()).isTrue();
    }
}