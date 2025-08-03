package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import com.boardly.features.card.domain.model.CardPriority;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UpdateCardService updateCardPriority 메서드 테스트")
class UpdateCardServiceUpdateCardPriorityTest {

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
                null, // CardMovePolicy는 updateCardPriority 테스트에서 사용하지 않음
                cardRepository,
                boardListRepository,
                boardRepository,
                validationMessageResolver,
                activityHelper);
    }

    @Nested
    @DisplayName("updateCardPriority 메서드 테스트")
    class UpdateCardPriorityTest {

        private Card existingCard;
        private BoardList boardList;
        private Board board;
        private CardId cardId;
        private ListId listId;
        private BoardId boardId;
        private String priority;

        @BeforeEach
        void setUp() {
            cardId = new CardId("card-123");
            listId = new ListId("list-123");
            boardId = new BoardId("board-123");
            priority = "high";

            existingCard = mock(Card.class);
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(listId);
            when(existingCard.getTitle()).thenReturn("테스트 카드");

            boardList = mock(BoardList.class);
            when(boardList.getListId()).thenReturn(listId);
            when(boardList.getBoardId()).thenReturn(boardId);
            when(boardList.getTitle()).thenReturn("테스트 리스트");

            board = mock(Board.class);
            when(board.getBoardId()).thenReturn(boardId);
            when(board.getTitle()).thenReturn("테스트 보드");
            when(board.isArchived()).thenReturn(false);
        }

        @Test
        @DisplayName("유효한 카드 ID로 우선순위를 설정할 때 성공한다")
        void shouldUpdateCardPrioritySuccessfully() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.error"))
                    .thenReturn("카드 수정 중 오류가 발생했습니다.");
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), priority);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).setPriority(any(CardPriority.class));
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("다양한 우선순위 값으로 설정할 때 성공한다")
        void shouldUpdateCardPriorityWithDifferentValuesSuccessfully() {
            // given
            String[] priorities = { "low", "medium", "high", "urgent" };
            when(validationMessageResolver.getMessage("error.service.card.update.error"))
                    .thenReturn("카드 수정 중 오류가 발생했습니다.");
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            for (String testPriority : priorities) {
                // when
                Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), testPriority);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).isEqualTo(existingCard);
            }
        }

        @Test
        @DisplayName("카드 ID가 null일 때 실패를 반환한다")
        void shouldReturnFailureWhenCardIdIsNull() {
            // given
            when(validationMessageResolver.getMessage("validation.card.id.invalid"))
                    .thenReturn("카드 ID가 유효하지 않습니다.");

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(null, priority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 ID가 유효하지 않습니다.");
            verifyNoInteractions(cardRepository);
        }

        @Test
        @DisplayName("카드 ID가 빈 문자열일 때 실패를 반환한다")
        void shouldReturnFailureWhenCardIdIsEmpty() {
            // given
            when(validationMessageResolver.getMessage("validation.card.id.invalid"))
                    .thenReturn("카드 ID가 유효하지 않습니다.");

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority("", priority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 ID가 유효하지 않습니다.");
            verifyNoInteractions(cardRepository);
        }

        @Test
        @DisplayName("카드 ID가 공백 문자열일 때 실패를 반환한다")
        void shouldReturnFailureWhenCardIdIsBlank() {
            // given
            when(validationMessageResolver.getMessage("validation.card.id.invalid"))
                    .thenReturn("카드 ID가 유효하지 않습니다.");

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority("   ", priority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 ID가 유효하지 않습니다.");
            verifyNoInteractions(cardRepository);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.not_found"))
                    .thenReturn("카드를 찾을 수 없습니다.");
            when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), priority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드를 찾을 수 없습니다.");
            verifyNoInteractions(boardListRepository);
        }

        @Test
        @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenListNotFound() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.list_not_found"))
                    .thenReturn("리스트를 찾을 수 없습니다.");
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), priority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");
            verifyNoInteractions(boardRepository);
        }

        @Test
        @DisplayName("보드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardNotFound() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.board_not_found"))
                    .thenReturn("보드를 찾을 수 없습니다.");
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), priority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다.");
            // existingCard는 findCardById에서 getListId()가 호출되므로 상호작용이 있음
        }

        @Test
        @DisplayName("아카이브된 보드의 카드 우선순위 변경 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenBoardIsArchived() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.archived_board"))
                    .thenReturn("아카이브된 보드의 카드는 수정할 수 없습니다.");
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(board.isArchived()).thenReturn(true);

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), priority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("아카이브된 보드의 카드는 수정할 수 없습니다.");
            // existingCard는 findCardById에서 getListId()가 호출되므로 상호작용이 있음
        }

        @Test
        @DisplayName("카드 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCardSaveFails() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.error"))
                    .thenReturn("카드 수정 중 오류가 발생했습니다.");
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(existingCard)).thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), priority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("저장 실패");
            verify(existingCard).setPriority(any(CardPriority.class));
        }

        @Test
        @DisplayName("카드 우선순위 설정 중 예외 발생 시 실패를 반환한다")
        void shouldReturnFailureWhenExceptionOccursDuringPrioritySetting() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.error"))
                    .thenReturn("카드 수정 중 오류가 발생했습니다.");
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            doThrow(new RuntimeException("우선순위 설정 실패")).when(existingCard).setPriority(any(CardPriority.class));

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), priority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 수정 중 오류가 발생했습니다.");
            // cardRepository는 findCardById에서 호출되므로 상호작용이 있음
        }

        @Test
        @DisplayName("유효하지 않은 우선순위 값으로 설정 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenInvalidPriorityValue() {
            // given
            String invalidPriority = "invalid_priority";
            when(validationMessageResolver.getMessage("error.service.card.update.error"))
                    .thenReturn("카드 수정 중 오류가 발생했습니다.");
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            doThrow(new IllegalArgumentException("유효하지 않은 우선순위 값")).when(existingCard)
                    .setPriority(any(CardPriority.class));

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), invalidPriority);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("카드 수정 중 오류가 발생했습니다.");
            // cardRepository는 findCardById에서 호출되므로 상호작용이 있음
        }

        @Test
        @DisplayName("이미 우선순위가 설정된 카드에 새로운 우선순위를 설정할 때 성공한다")
        void shouldSucceedWhenSettingNewPriorityForCardWithExistingPriority() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.error"))
                    .thenReturn("카드 수정 중 오류가 발생했습니다.");
            when(existingCard.getPriority()).thenReturn(CardPriority.MEDIUM);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), priority);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).setPriority(any(CardPriority.class));
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("우선순위가 없는 카드에 우선순위를 설정할 때 성공한다")
        void shouldSucceedWhenSettingPriorityForCardWithoutPriority() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.error"))
                    .thenReturn("카드 수정 중 오류가 발생했습니다.");
            when(existingCard.getPriority()).thenReturn(null);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), priority);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).setPriority(any(CardPriority.class));
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("같은 우선순위로 다시 설정해도 성공한다")
        void shouldSucceedWhenSettingSamePriority() {
            // given
            when(validationMessageResolver.getMessage("error.service.card.update.error"))
                    .thenReturn("카드 수정 중 오류가 발생했습니다.");
            when(existingCard.getPriority()).thenReturn(CardPriority.HIGH);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCardPriority(cardId.getId(), "high");

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).setPriority(any(CardPriority.class));
            verify(cardRepository).save(existingCard);
        }
    }
}