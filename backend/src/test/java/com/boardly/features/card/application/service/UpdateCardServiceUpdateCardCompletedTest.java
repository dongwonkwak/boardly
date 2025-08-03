package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCardService updateCardCompleted 메서드 테스트")
class UpdateCardServiceUpdateCardCompletedTest {

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
                null, // CardMovePolicy는 updateCardCompleted 테스트에서 사용하지 않음
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
    @DisplayName("updateCardCompleted 메서드 테스트")
    class UpdateCardCompletedTest {

        private Card existingCard;
        private BoardList boardList;
        private Board board;
        private UserId userId;
        private CardId cardId;
        private ListId listId;
        private BoardId boardId;
        private String cardIdString;

        @BeforeEach
        void setUp() {
            userId = new UserId("user-123");
            cardId = new CardId("card-123");
            cardIdString = "card-123";
            listId = new ListId("list-123");
            boardId = new BoardId("board-123");

            existingCard = mock(Card.class);
            lenient().when(existingCard.getCardId()).thenReturn(cardId);
            lenient().when(existingCard.getListId()).thenReturn(listId);
            lenient().when(existingCard.isCompleted()).thenReturn(false);

            boardList = BoardList.builder()
                    .listId(listId)
                    .title("테스트 리스트")
                    .boardId(boardId)
                    .position(0)
                    .build();

            board = Board.builder()
                    .boardId(boardId)
                    .title("테스트 보드")
                    .ownerId(userId)
                    .isArchived(false)
                    .build();
        }

        @Test
        @DisplayName("유효한 카드 ID로 완료 상태를 true로 변경 시 성공한다")
        void shouldUpdateCardCompletedToTrueSuccessfully() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(any(Card.class))).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(cardIdString, true);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);

            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(listId);
            verify(boardRepository).findById(boardId);
            verify(cardRepository).save(existingCard);
            verify(existingCard).complete();
        }

        @Test
        @DisplayName("유효한 카드 ID로 완료 상태를 false로 변경 시 성공한다")
        void shouldUpdateCardCompletedToFalseSuccessfully() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(any(Card.class))).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(cardIdString, false);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);

            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(listId);
            verify(boardRepository).findById(boardId);
            verify(cardRepository).save(existingCard);
            verify(existingCard).uncomplete();
        }

        @Test
        @DisplayName("카드 ID가 null일 때 실패를 반환한다")
        void shouldReturnFailureWhenCardIdIsNull() {
            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(null, true);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);
            assertThat(failure.getMessage()).isEqualTo("카드 ID가 유효하지 않습니다.");

            verifyNoInteractions(cardRepository, boardListRepository, boardRepository, activityHelper);
        }

        @Test
        @DisplayName("카드 ID가 빈 문자열일 때 실패를 반환한다")
        void shouldReturnFailureWhenCardIdIsEmpty() {
            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted("", true);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);
            assertThat(failure.getMessage()).isEqualTo("카드 ID가 유효하지 않습니다.");

            verifyNoInteractions(cardRepository, boardListRepository, boardRepository, activityHelper);
        }

        @Test
        @DisplayName("카드 ID가 공백 문자열일 때 실패를 반환한다")
        void shouldReturnFailureWhenCardIdIsBlank() {
            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted("   ", true);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);
            assertThat(failure.getMessage()).isEqualTo("카드 ID가 유효하지 않습니다.");

            verifyNoInteractions(cardRepository, boardListRepository, boardRepository, activityHelper);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(cardIdString, true);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);
            assertThat(failure.getMessage()).isEqualTo("카드를 찾을 수 없습니다.");

            verify(cardRepository).findById(cardId);
            verifyNoInteractions(boardListRepository, boardRepository, activityHelper);
        }

        @Test
        @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenListNotFound() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(cardIdString, true);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);
            assertThat(failure.getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");

            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(listId);
            verifyNoInteractions(boardRepository, activityHelper);
        }

        @Test
        @DisplayName("보드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardNotFound() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(cardIdString, true);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);
            assertThat(failure.getMessage()).isEqualTo("보드를 찾을 수 없습니다.");

            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(listId);
            verify(boardRepository).findById(boardId);
            verifyNoInteractions(activityHelper);
        }

        @Test
        @DisplayName("아카이브된 보드의 카드 완료 상태 변경 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenBoardIsArchived() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(boardId)
                    .title("아카이브된 보드")
                    .ownerId(userId)
                    .isArchived(true)
                    .build();

            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(archivedBoard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(cardIdString, true);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.ResourceConflict.class);
            assertThat(failure.getMessage()).isEqualTo("아카이브된 보드의 카드는 수정할 수 없습니다.");

            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(listId);
            verify(boardRepository).findById(boardId);
            verifyNoInteractions(activityHelper);
        }

        @Test
        @DisplayName("카드 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCardSaveFails() {
            // given
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(any(Card.class))).thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(cardIdString, true);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InternalError.class);
            assertThat(failure.getMessage()).isEqualTo("저장 실패");

            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(listId);
            verify(boardRepository).findById(boardId);
            verify(cardRepository).save(existingCard);
            verifyNoInteractions(activityHelper);
        }

        @Test
        @DisplayName("이미 완료된 카드를 다시 완료로 설정해도 성공한다")
        void shouldSucceedWhenSettingAlreadyCompletedCardToCompleted() {
            // given
            Card completedCard = mock(Card.class);
            lenient().when(completedCard.getCardId()).thenReturn(cardId);
            lenient().when(completedCard.getListId()).thenReturn(listId);
            lenient().when(completedCard.isCompleted()).thenReturn(true);

            when(cardRepository.findById(cardId)).thenReturn(Optional.of(completedCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(any(Card.class))).thenReturn(Either.right(completedCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(cardIdString, true);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(completedCard);

            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(listId);
            verify(boardRepository).findById(boardId);
            verify(cardRepository).save(completedCard);
            verify(completedCard).complete();
        }

        @Test
        @DisplayName("완료되지 않은 카드를 미완료로 설정해도 성공한다")
        void shouldSucceedWhenSettingIncompleteCardToIncomplete() {
            // given
            Card incompleteCard = mock(Card.class);
            lenient().when(incompleteCard.getCardId()).thenReturn(cardId);
            lenient().when(incompleteCard.getListId()).thenReturn(listId);
            lenient().when(incompleteCard.isCompleted()).thenReturn(false);

            when(cardRepository.findById(cardId)).thenReturn(Optional.of(incompleteCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(any(Card.class))).thenReturn(Either.right(incompleteCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardCompleted(cardIdString, false);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(incompleteCard);

            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(listId);
            verify(boardRepository).findById(boardId);
            verify(cardRepository).save(incompleteCard);
            verify(incompleteCard).uncomplete();
        }
    }
}