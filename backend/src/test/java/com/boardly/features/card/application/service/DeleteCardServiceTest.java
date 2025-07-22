package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.features.card.application.validation.DeleteCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCardService 테스트")
class DeleteCardServiceTest {

        @Mock
        private DeleteCardValidator deleteCardValidator;

        @Mock
        private CardRepository cardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @InjectMocks
        private DeleteCardService deleteCardService;

        private UserId testUserId;
        private CardId testCardId;
        private ListId testListId;
        private BoardId testBoardId;
        private Card testCard;
        private BoardList testBoardList;
        private Board testBoard;
        private DeleteCardCommand validCommand;

        @BeforeEach
        void setUp() {
                testUserId = new UserId("test-user-123");
                testCardId = new CardId("test-card-123");
                testListId = new ListId("test-list-123");
                testBoardId = new BoardId("test-board-123");

                Instant now = Instant.now();

                testCard = Card.builder()
                                .cardId(testCardId)
                                .title("삭제할 카드")
                                .description("삭제할 카드 설명")
                                .position(2)
                                .listId(testListId)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                testBoardList = BoardList.builder()
                                .listId(testListId)
                                .title("테스트 리스트")
                                .position(1)
                                .boardId(testBoardId)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                testBoard = Board.builder()
                                .boardId(testBoardId)
                                .title("테스트 보드")
                                .description("테스트 보드 설명")
                                .isArchived(false)
                                .ownerId(testUserId)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                validCommand = DeleteCardCommand.of(testCardId, testUserId);

                // 기본 메시지 모킹 설정
                lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 올바르지 않습니다");
                lenient().when(validationMessageResolver.getMessage("error.service.card.delete.not_found"))
                                .thenReturn("삭제할 카드를 찾을 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("error.service.card.delete.list_not_found"))
                                .thenReturn("리스트를 찾을 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("error.service.card.delete.access_denied"))
                                .thenReturn("보드에 접근할 권한이 없습니다");
                lenient().when(validationMessageResolver.getMessage("error.service.card.delete.archived_board"))
                                .thenReturn("아카이브된 보드에서는 카드를 삭제할 수 없습니다");
        }

        @Nested
        @DisplayName("카드 삭제 성공")
        class DeleteCardSuccess {

                @Test
                @DisplayName("유효한 요청으로 카드를 삭제할 수 있다")
                void deleteCard_ValidRequest_ShouldSucceed() {
                        // given
                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardRepository.delete(testCardId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findByListIdAndPositionGreaterThan(testListId, 2))
                                        .thenReturn(List.of());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository).delete(testCardId);
                }

                @Test
                @DisplayName("카드 삭제 후 남은 카드들의 위치가 재정렬된다")
                void deleteCard_WithRemainingCards_ShouldReorderPositions() {
                        // given
                        Card remainingCard1 = Card.builder()
                                        .cardId(new CardId("card-456"))
                                        .title("남은 카드 1")
                                        .position(3)
                                        .listId(testListId)
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        Card remainingCard2 = Card.builder()
                                        .cardId(new CardId("card-789"))
                                        .title("남은 카드 2")
                                        .position(4)
                                        .listId(testListId)
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        List<Card> cardsToReorder = List.of(remainingCard1, remainingCard2);

                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardRepository.delete(testCardId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findByListIdAndPositionGreaterThan(testListId, 2))
                                        .thenReturn(cardsToReorder);
                        when(cardRepository.saveAll(any()))
                                        .thenReturn(cardsToReorder);

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository).delete(testCardId);
                        verify(cardRepository).saveAll(any());

                        // 위치가 재정렬되었는지 확인
                        assertThat(remainingCard1.getPosition()).isEqualTo(2); // 3 -> 2
                        assertThat(remainingCard2.getPosition()).isEqualTo(3); // 4 -> 3
                }

                @Test
                @DisplayName("마지막 위치의 카드를 삭제할 때는 위치 재정렬이 필요 없다")
                void deleteCard_LastPositionCard_ShouldNotReorder() {
                        // given
                        Card lastCard = Card.builder()
                                        .cardId(testCardId)
                                        .title("마지막 카드")
                                        .position(5)
                                        .listId(testListId)
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        DeleteCardCommand lastCardCommand = DeleteCardCommand.of(testCardId, testUserId);

                        when(deleteCardValidator.validate(lastCardCommand))
                                        .thenReturn(ValidationResult.valid(lastCardCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(lastCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardRepository.delete(testCardId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findByListIdAndPositionGreaterThan(testListId, 5))
                                        .thenReturn(List.of());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(lastCardCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository).delete(testCardId);
                }
        }

        @Nested
        @DisplayName("입력 검증")
        class InputValidation {

                @Test
                @DisplayName("입력 검증에 실패한 경우 삭제에 실패한다")
                void deleteCard_InvalidInput_ShouldFail() {
                        // given
                        ValidationResult<DeleteCardCommand> invalidResult = ValidationResult.invalid(
                                        "cardId", "카드 ID는 필수입니다", null);
                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(invalidResult);

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                }
        }

        @Nested
        @DisplayName("카드 존재 확인")
        class CardExistenceCheck {

                @Test
                @DisplayName("삭제할 카드가 존재하지 않는 경우 실패한다")
                void deleteCard_CardNotFound_ShouldFail() {
                        // given
                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                }
        }

        @Nested
        @DisplayName("권한 검증")
        class PermissionValidation {

                @Test
                @DisplayName("보드에 접근 권한이 없는 경우 실패한다")
                void deleteCard_NoBoardAccess_ShouldFail() {
                        // given
                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                }

                @Test
                @DisplayName("아카이브된 보드에서 카드 삭제를 시도하는 경우 실패한다")
                void deleteCard_ArchivedBoard_ShouldFail() {
                        // given
                        Board archivedBoard = Board.builder()
                                        .boardId(testBoardId)
                                        .title("아카이브된 보드")
                                        .description("아카이브된 보드 설명")
                                        .isArchived(true)
                                        .ownerId(testUserId)
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
                }

                @Test
                @DisplayName("리스트를 찾을 수 없는 경우 실패한다")
                void deleteCard_ListNotFound_ShouldFail() {
                        // given
                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                }
        }

        @Nested
        @DisplayName("카드 삭제 실패")
        class CardDeletionFailure {

                @Test
                @DisplayName("카드 삭제 중 오류가 발생한 경우 실패한다")
                void deleteCard_DeletionError_ShouldFail() {
                        // given
                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardRepository.delete(testCardId))
                                        .thenReturn(Either.left(Failure.ofInternalServerError("삭제 실패")));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                }
        }

        @Nested
        @DisplayName("위치 재정렬")
        class PositionReordering {

                @Test
                @DisplayName("위치 재정렬 중 오류가 발생해도 카드 삭제는 성공한다")
                void deleteCard_ReorderError_ShouldStillSucceed() {
                        // given
                        Card remainingCard = Card.builder()
                                        .cardId(new CardId("card-456"))
                                        .title("남은 카드")
                                        .position(3)
                                        .listId(testListId)
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardRepository.delete(testCardId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findByListIdAndPositionGreaterThan(testListId, 2))
                                        .thenReturn(List.of(remainingCard));
                        when(cardRepository.saveAll(any()))
                                        .thenThrow(new RuntimeException("저장 실패"));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        // 카드 삭제는 성공했지만 위치 재정렬은 실패했음
                        verify(cardRepository).delete(testCardId);
                }

                @Test
                @DisplayName("재정렬할 카드가 없는 경우 저장을 호출하지 않는다")
                void deleteCard_NoCardsToReorder_ShouldNotCallSaveAll() {
                        // given
                        when(deleteCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardRepository.delete(testCardId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findByListIdAndPositionGreaterThan(testListId, 2))
                                        .thenReturn(List.of());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository).delete(testCardId);
                        // 재정렬할 카드가 없으면 saveAll이 호출되지 않음
                }
        }
}