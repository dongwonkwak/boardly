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
@DisplayName("DeleteCardService - 아카이브된 보드 테스트")
class DeleteCardServiceArchivedBoardTest {

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

        when(validationMessageResolver.getMessage("error.service.card.delete.archived_board"))
                .thenReturn("아카이브된 보드에서는 카드를 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("아카이브된 보드에서 카드 삭제 시도 시 실패를 반환한다")
    void shouldReturnFailureWhenBoardIsArchived() {
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

        Board archivedBoard = Board.builder()
                .boardId(boardId)
                .title("아카이브된 보드")
                .ownerId(userId)
                .isArchived(true)
                .build();

        when(cardValidator.validateDelete(command))
                .thenReturn(ValidationResult.valid(command));
        when(cardRepository.findById(cardId))
                .thenReturn(Optional.of(cardToDelete));
        when(boardListRepository.findById(listId))
                .thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                .thenReturn(Optional.of(archivedBoard));

        // when
        Either<Failure, Void> result = deleteCardService.deleteCard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        Failure failure = result.getLeft();
        assertThat(failure).isInstanceOf(Failure.BusinessRuleViolation.class);
        Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) failure;
        assertThat(businessRuleViolation.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
        assertThat(businessRuleViolation.getMessage()).isEqualTo("아카이브된 보드에서는 카드를 삭제할 수 없습니다.");

        verify(cardValidator).validateDelete(command);
        verify(cardRepository).findById(cardId);
        verify(boardListRepository).findById(listId);
        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
        verifyNoInteractions(activityHelper);
    }
}