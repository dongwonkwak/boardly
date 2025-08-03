package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UpdateCardService updateCardDueDate 메서드 테스트")
class UpdateCardServiceUpdateCardDueDateTest {

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
    private ActivityHelper activityHelper;

    private UpdateCardService updateCardService;

    @BeforeEach
    void setUp() {
        updateCardService = new UpdateCardService(
                cardValidator,
                null, // CardMovePolicy는 updateCardDueDate 테스트에서 사용하지 않음
                cardRepository,
                boardListRepository,
                boardRepository,
                validationMessageResolver,
                activityHelper);

        // 공통으로 사용되는 메시지 설정
        lenient().when(validationMessageResolver.getMessage("validation.card.id.invalid"))
                .thenReturn("카드 ID가 유효하지 않습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.not_found"))
                .thenReturn("카드를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.list_not_found"))
                .thenReturn("리스트를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.board_not_found"))
                .thenReturn("보드를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.access_denied"))
                .thenReturn("보드 접근 권한이 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.archived_board"))
                .thenReturn("아카이브된 보드의 카드는 수정할 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.error"))
                .thenReturn("카드 수정 중 오류가 발생했습니다.");
    }

    @Nested
    @DisplayName("updateCardDueDate 메서드 테스트")
    class UpdateCardDueDateTest {

        private Card existingCard;
        private BoardList boardList;
        private Board board;
        private CardId cardId;
        private ListId listId;
        private BoardId boardId;
        private Instant dueDate;

        @BeforeEach
        void setUp() {
            cardId = new CardId("card-123");
            listId = new ListId("list-123");
            boardId = new BoardId("board-123");
            dueDate = Instant.now().plusSeconds(86400); // 24시간 후

            existingCard = mock(Card.class);
            boardList = mock(BoardList.class);
            board = mock(Board.class);
        }

        @Test
        @DisplayName("유효한 카드 ID로 마감일을 설정할 때 성공한다")
        void shouldUpdateCardDueDateSuccessfully() {
            // given
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(board.getBoardId()).thenReturn(boardId);
            when(board.isArchived()).thenReturn(false);
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), dueDate);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).setDueDate(dueDate);
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("마감일을 null로 설정하여 마감일을 제거할 때 성공한다")
        void shouldRemoveCardDueDateSuccessfully() {
            // given
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(board.getBoardId()).thenReturn(boardId);
            when(board.isArchived()).thenReturn(false);
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), null);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).removeDueDate();
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("카드 ID가 null일 때 실패를 반환한다")
        void shouldReturnFailureWhenCardIdIsNull() {
            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(null, dueDate);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 ID가 유효하지 않습니다.");
            verifyNoInteractions(cardRepository);
        }

        @Test
        @DisplayName("카드 ID가 빈 문자열일 때 실패를 반환한다")
        void shouldReturnFailureWhenCardIdIsEmpty() {
            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate("", dueDate);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 ID가 유효하지 않습니다.");
            verifyNoInteractions(cardRepository);
        }

        @Test
        @DisplayName("카드 ID가 공백 문자열일 때 실패를 반환한다")
        void shouldReturnFailureWhenCardIdIsBlank() {
            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate("   ", dueDate);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 ID가 유효하지 않습니다.");
            verifyNoInteractions(cardRepository);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), dueDate);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드를 찾을 수 없습니다.");
            verifyNoInteractions(boardListRepository);
        }

        @Test
        @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenListNotFound() {
            // given
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), dueDate);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");
            verifyNoInteractions(boardRepository);
        }

        @Test
        @DisplayName("보드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardNotFound() {
            // given
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), dueDate);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("아카이브된 보드의 카드 마감일 변경 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenBoardIsArchived() {
            // given
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(board.getBoardId()).thenReturn(boardId);
            when(board.isArchived()).thenReturn(true);

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), dueDate);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("아카이브된 보드의 카드는 수정할 수 없습니다.");
        }

        @Test
        @DisplayName("카드 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCardSaveFails() {
            // given
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(board.getBoardId()).thenReturn(boardId);
            when(board.isArchived()).thenReturn(false);
            when(cardRepository.save(existingCard)).thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), dueDate);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("저장 실패");
            verify(existingCard).setDueDate(dueDate);
        }

        @Test
        @DisplayName("카드 마감일 설정 중 예외 발생 시 실패를 반환한다")
        void shouldReturnFailureWhenExceptionOccursDuringDueDateSetting() {
            // given
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(board.getBoardId()).thenReturn(boardId);
            when(board.isArchived()).thenReturn(false);
            doThrow(new RuntimeException("마감일 설정 실패")).when(existingCard).setDueDate(dueDate);

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), dueDate);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 수정 중 오류가 발생했습니다.");
        }

        @Test
        @DisplayName("카드 마감일 제거 중 예외 발생 시 실패를 반환한다")
        void shouldReturnFailureWhenExceptionOccursDuringDueDateRemoval() {
            // given
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(board.getBoardId()).thenReturn(boardId);
            when(board.isArchived()).thenReturn(false);
            doThrow(new RuntimeException("마감일 제거 실패")).when(existingCard).removeDueDate();

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), null);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 수정 중 오류가 발생했습니다.");
        }

        @Test
        @DisplayName("이미 마감일이 설정된 카드에 새로운 마감일을 설정할 때 성공한다")
        void shouldSucceedWhenSettingNewDueDateForCardWithExistingDueDate() {
            // given
            Instant existingDueDate = Instant.now().plusSeconds(3600); // 1시간 후
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(existingCard.getDueDate()).thenReturn(existingDueDate);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(board.getBoardId()).thenReturn(boardId);
            when(board.isArchived()).thenReturn(false);
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), dueDate);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).setDueDate(dueDate);
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("마감일이 없는 카드에 마감일을 설정할 때 성공한다")
        void shouldSucceedWhenSettingDueDateForCardWithoutDueDate() {
            // given
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(existingCard.getDueDate()).thenReturn(null);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(board.getBoardId()).thenReturn(boardId);
            when(board.isArchived()).thenReturn(false);
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardDueDate(cardId.getId(), dueDate);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).setDueDate(dueDate);
            verify(cardRepository).save(existingCard);
        }
    }
}