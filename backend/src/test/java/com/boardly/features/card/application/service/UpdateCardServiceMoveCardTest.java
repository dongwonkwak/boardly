package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.policy.CardMovePolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UpdateCardService moveCard 메서드 테스트")
class UpdateCardServiceMoveCardTest {

    @Mock
    private CardValidator cardValidator;

    @Mock
    private CardMovePolicy cardMovePolicy;

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
                cardMovePolicy,
                cardRepository,
                boardListRepository,
                boardRepository,
                validationMessageResolver,
                activityHelper);

        // 공통으로 사용되는 메시지 설정
        lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.move.not_found"))
                .thenReturn("이동할 카드를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.move.access_denied"))
                .thenReturn("보드 접근 권한이 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.move.archived_board"))
                .thenReturn("아카이브된 보드의 카드는 이동할 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.move.target_list_not_found"))
                .thenReturn("대상 리스트를 찾을 수 없습니다.");
    }

    @Nested
    @DisplayName("moveCard 메서드 테스트")
    class MoveCardTest {

        private Card existingCard;
        private BoardList sourceList;
        private BoardList targetList;
        private Board board;
        private UserId userId;
        private CardId cardId;
        private ListId sourceListId;
        private ListId targetListId;
        private BoardId boardId;
        private MoveCardCommand command;

        @BeforeEach
        void setUp() {
            cardId = new CardId("test-card-id");
            sourceListId = new ListId("test-source-list-id");
            targetListId = new ListId("test-target-list-id");
            boardId = new BoardId("test-board-id");
            userId = new UserId("test-user-id");

            existingCard = mock(Card.class);
            when(existingCard.getCardId()).thenReturn(cardId);
            when(existingCard.getListId()).thenReturn(sourceListId);
            when(existingCard.getPosition()).thenReturn(2);

            sourceList = mock(BoardList.class);
            when(sourceList.getListId()).thenReturn(sourceListId);
            when(sourceList.getBoardId()).thenReturn(boardId);
            when(sourceList.getTitle()).thenReturn("소스 리스트");

            targetList = mock(BoardList.class);
            when(targetList.getListId()).thenReturn(targetListId);
            when(targetList.getBoardId()).thenReturn(boardId);
            when(targetList.getTitle()).thenReturn("대상 리스트");

            board = mock(Board.class);
            when(board.getBoardId()).thenReturn(boardId);
            when(board.isArchived()).thenReturn(false);
            when(board.getTitle()).thenReturn("테스트 보드");

            command = MoveCardCommand.of(cardId, targetListId, 3, userId);
        }

        @Test
        @DisplayName("다른 리스트로 카드 이동이 성공한다")
        void shouldMoveCardToAnotherListSuccessfully() {
            // given
            when(cardValidator.validateMove(command)).thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
            when(boardListRepository.findById(targetListId)).thenReturn(Optional.of(targetList));
            when(cardMovePolicy.canMoveToAnotherList(existingCard, targetListId, 3))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionGreaterThan(sourceListId, 2))
                    .thenReturn(List.of());
            when(cardRepository.findByListIdAndPositionGreaterThan(targetListId, 2))
                    .thenReturn(List.of());
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(command);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).moveToList(targetListId, 3);
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("같은 리스트 내에서 카드 이동이 성공한다")
        void shouldMoveCardWithinSameListSuccessfully() {
            // given
            MoveCardCommand sameListCommand = MoveCardCommand.of(cardId, null, 5, userId);
            when(cardValidator.validateMove(sameListCommand)).thenReturn(ValidationResult.valid(sameListCommand));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
            when(cardMovePolicy.canMoveWithinSameList(existingCard, 5))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionBetween(sourceListId, 3, 4))
                    .thenReturn(List.of());
            when(cardRepository.saveAll(anyList())).thenReturn(List.of());
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(sameListCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(existingCard).updatePosition(5);
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("입력 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenInputValidationFails() {
            // given
            ValidationResult<MoveCardCommand> invalidResult = ValidationResult.invalid("command", "입력 오류", command);
            when(cardValidator.validateMove(command)).thenReturn(invalidResult);

            // when
            Either<Failure, Card> result = updateCardService.moveCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다.");
            verifyNoInteractions(cardRepository);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            when(cardValidator.validateMove(command)).thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.moveCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("이동할 카드를 찾을 수 없습니다.");
            verify(cardRepository).findById(cardId);
            verifyNoInteractions(boardRepository);
        }

        @Test
        @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardAccessDenied() {
            // given
            when(cardValidator.validateMove(command)).thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.moveCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("보드 접근 권한이 없습니다.");
            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(sourceListId);
            verify(boardRepository).findByIdAndOwnerId(boardId, userId);
            verifyNoInteractions(cardMovePolicy);
        }

        @Test
        @DisplayName("아카이브된 보드의 카드 이동 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenBoardIsArchived() {
            // given
            when(cardValidator.validateMove(command)).thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
            when(board.isArchived()).thenReturn(true);

            // when
            Either<Failure, Card> result = updateCardService.moveCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("아카이브된 보드의 카드는 이동할 수 없습니다.");
            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(sourceListId);
            verify(boardRepository).findByIdAndOwnerId(boardId, userId);
            verifyNoInteractions(cardMovePolicy);
        }

        @Test
        @DisplayName("대상 리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenTargetListNotFound() {
            // given
            when(cardValidator.validateMove(command)).thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
            when(boardListRepository.findById(targetListId)).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.moveCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("대상 리스트를 찾을 수 없습니다.");
            verify(cardRepository).findById(cardId);
            verify(boardListRepository).findById(sourceListId);
            verify(boardRepository).findByIdAndOwnerId(boardId, userId);
            verify(boardListRepository).findById(targetListId);
            verifyNoInteractions(cardMovePolicy);
        }

        @Test
        @DisplayName("카드 이동 정책 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenMovePolicyValidationFails() {
            // given
            when(cardValidator.validateMove(command)).thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
            when(boardListRepository.findById(targetListId)).thenReturn(Optional.of(targetList));
            when(cardMovePolicy.canMoveToAnotherList(existingCard, targetListId, 3))
                    .thenReturn(Either.left(Failure.ofConflict("LIST_CARD_LIMIT_EXCEEDED")));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");
            verify(cardMovePolicy).canMoveToAnotherList(existingCard, targetListId, 3);
            // moveToList는 호출되지 않아야 함
            verify(existingCard, org.mockito.Mockito.never()).moveToList(targetListId, 3);
        }

        @Test
        @DisplayName("카드 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCardSaveFails() {
            // given
            when(cardValidator.validateMove(command)).thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
            when(boardListRepository.findById(targetListId)).thenReturn(Optional.of(targetList));
            when(cardMovePolicy.canMoveToAnotherList(existingCard, targetListId, 3))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionGreaterThan(sourceListId, 2))
                    .thenReturn(List.of());
            when(cardRepository.findByListIdAndPositionGreaterThan(targetListId, 2))
                    .thenReturn(List.of());
            when(cardRepository.save(existingCard)).thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("저장 실패");
            verify(existingCard).moveToList(targetListId, 3);
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("같은 리스트 내에서 위치 변경 없이 이동할 때 성공한다")
        void shouldSucceedWhenMovingToSamePositionWithinSameList() {
            // given
            MoveCardCommand samePositionCommand = MoveCardCommand.of(cardId, null, 2, userId);
            when(cardValidator.validateMove(samePositionCommand))
                    .thenReturn(ValidationResult.valid(samePositionCommand));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
            when(cardMovePolicy.canMoveWithinSameList(existingCard, 2))
                    .thenReturn(Either.right(null));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(samePositionCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            // 위치가 같으므로 updatePosition은 호출되지 않지만, 다른 메서드들은 호출될 수 있음
            verify(existingCard, org.mockito.Mockito.never()).updatePosition(2);
            verify(cardRepository, org.mockito.Mockito.never()).save(existingCard);
        }

        @Test
        @DisplayName("카드 이동 중 예외 발생 시 실패를 반환한다")
        void shouldReturnFailureWhenExceptionOccursDuringMove() {
            // given
            when(cardValidator.validateMove(command)).thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
            when(boardListRepository.findById(targetListId)).thenReturn(Optional.of(targetList));
            when(cardMovePolicy.canMoveToAnotherList(existingCard, targetListId, 3))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionGreaterThan(sourceListId, 2))
                    .thenReturn(List.of());
            when(cardRepository.findByListIdAndPositionGreaterThan(targetListId, 2))
                    .thenReturn(List.of());
            doThrow(new RuntimeException("이동 실패")).when(existingCard).moveToList(targetListId, 3);

            // when & then
            // 예외가 발생하면 RuntimeException이 그대로 전파됨
            assertThatThrownBy(() -> updateCardService.moveCard(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("이동 실패");
            verify(existingCard).moveToList(targetListId, 3);
            verify(cardRepository, org.mockito.Mockito.never()).save(existingCard);
        }

        @Test
        @DisplayName("위치 조정이 필요한 경우 성공적으로 처리한다")
        void shouldHandlePositionAdjustmentSuccessfully() {
            // given
            Card cardToAdjust = mock(Card.class);
            when(cardToAdjust.getPosition()).thenReturn(3);
            when(cardValidator.validateMove(command)).thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(sourceList));
            when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
            when(boardListRepository.findById(targetListId)).thenReturn(Optional.of(targetList));
            when(cardMovePolicy.canMoveToAnotherList(existingCard, targetListId, 3))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionGreaterThan(sourceListId, 2))
                    .thenReturn(List.of(cardToAdjust));
            when(cardRepository.findByListIdAndPositionGreaterThan(targetListId, 2))
                    .thenReturn(List.of());
            when(cardRepository.saveAll(anyList())).thenReturn(List.of(cardToAdjust));
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(command);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(cardToAdjust).updatePosition(2); // 위치 조정
            verify(cardRepository).saveAll(List.of(cardToAdjust));
            verify(existingCard).moveToList(targetListId, 3);
            verify(cardRepository).save(existingCard);
        }
    }
}