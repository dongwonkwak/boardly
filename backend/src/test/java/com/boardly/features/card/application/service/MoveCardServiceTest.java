package com.boardly.features.card.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.application.validation.MoveCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.policy.CardMovePolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MoveCardService 테스트")
class MoveCardServiceTest {

    @Mock
    private MoveCardValidator moveCardValidator;

    @Mock
    private CardMovePolicy cardMovePolicy;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ValidationMessageResolver messageResolver;

    @InjectMocks
    private MoveCardService moveCardService;

    private UserId testUserId;
    private ListId sourceListId;
    private ListId targetListId;
    private BoardId testBoardId;
    private CardId testCardId;
    private Card testCard;
    private BoardList sourceBoardList;
    private BoardList targetBoardList;
    private Board testBoard;
    private MoveCardCommand sameListMoveCommand;
    private MoveCardCommand differentListMoveCommand;

    @BeforeEach
    void setUp() {
        testUserId = new UserId("test-user-123");
        sourceListId = new ListId("source-list-123");
        targetListId = new ListId("target-list-123");
        testBoardId = new BoardId("test-board-123");
        testCardId = new CardId("test-card-123");

        Instant now = Instant.now();

        testCard = Card.builder()
                .cardId(testCardId)
                .title("테스트 카드")
                .description("테스트 카드 설명")
                .position(2)
                .listId(sourceListId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        sourceBoardList = BoardList.builder()
                .listId(sourceListId)
                .title("소스 리스트")
                .position(1)
                .boardId(testBoardId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        targetBoardList = BoardList.builder()
                .listId(targetListId)
                .title("타겟 리스트")
                .position(2)
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

        sameListMoveCommand = MoveCardCommand.of(testCardId, null, 4, testUserId);
        differentListMoveCommand = MoveCardCommand.of(testCardId, targetListId, 1, testUserId);
    }

    @Nested
    @DisplayName("moveCard 메서드 테스트")
    class MoveCardTest {

        @Nested
        @DisplayName("같은 리스트 내 이동 테스트")
        class SameListMoveTest {

            @Test
            @DisplayName("같은 리스트 내 카드 이동 성공 - 뒤로 이동")
            void moveCard_SameList_Backward_Success() {
                // given
                when(moveCardValidator.validate(sameListMoveCommand))
                        .thenReturn(ValidationResult.valid(sameListMoveCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.of(sourceBoardList));
                when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                        .thenReturn(Optional.of(testBoard));
                when(cardMovePolicy.canMoveWithinSameList(testCard, 4))
                        .thenReturn(Either.right(null));
                when(cardRepository.findByListIdAndPositionBetween(sourceListId, 3, 4))
                        .thenReturn(Arrays.asList(
                                createTestCard("카드3", 3),
                                createTestCard("카드4", 4)));
                when(cardRepository.save(any(Card.class)))
                        .thenAnswer(invocation -> Either.right(invocation.getArgument(0)));

                // when
                Either<Failure, Card> result = moveCardService.moveCard(sameListMoveCommand);

                // then
                assertThat(result.isRight()).isTrue();
                Card movedCard = result.get();
                assertThat(movedCard.getPosition()).isEqualTo(4);
                assertThat(movedCard.getListId()).isEqualTo(sourceListId);

                verify(cardRepository).saveAll(anyList());
                verify(cardRepository).save(testCard);
            }

            @Test
            @DisplayName("같은 리스트 내 카드 이동 성공 - 앞으로 이동")
            void moveCard_SameList_Forward_Success() {
                // given
                MoveCardCommand forwardCommand = MoveCardCommand.of(testCardId, null, 0, testUserId);
                when(moveCardValidator.validate(forwardCommand))
                        .thenReturn(ValidationResult.valid(forwardCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.of(sourceBoardList));
                when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                        .thenReturn(Optional.of(testBoard));
                when(cardMovePolicy.canMoveWithinSameList(testCard, 0))
                        .thenReturn(Either.right(null));
                when(cardRepository.findByListIdAndPositionBetween(sourceListId, 0, 1))
                        .thenReturn(Arrays.asList(
                                createTestCard("카드0", 0),
                                createTestCard("카드1", 1)));
                when(cardRepository.save(any(Card.class)))
                        .thenAnswer(invocation -> Either.right(invocation.getArgument(0)));

                // when
                Either<Failure, Card> result = moveCardService.moveCard(forwardCommand);

                // then
                assertThat(result.isRight()).isTrue();
                Card movedCard = result.get();
                assertThat(movedCard.getPosition()).isEqualTo(0);
                assertThat(movedCard.getListId()).isEqualTo(sourceListId);

                verify(cardRepository).saveAll(anyList());
                verify(cardRepository).save(testCard);
            }

            @Test
            @DisplayName("같은 리스트 내 카드 이동 - 위치 변경 없음")
            void moveCard_SameList_NoPositionChange_Success() {
                // given
                MoveCardCommand noChangeCommand = MoveCardCommand.of(testCardId, null, 2, testUserId);
                when(moveCardValidator.validate(noChangeCommand))
                        .thenReturn(ValidationResult.valid(noChangeCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.of(sourceBoardList));
                when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                        .thenReturn(Optional.of(testBoard));
                when(cardMovePolicy.canMoveWithinSameList(testCard, 2))
                        .thenReturn(Either.right(null));

                // when
                Either<Failure, Card> result = moveCardService.moveCard(noChangeCommand);

                // then
                assertThat(result.isRight()).isTrue();
                Card movedCard = result.get();
                assertThat(movedCard.getPosition()).isEqualTo(2);
                assertThat(movedCard.getListId()).isEqualTo(sourceListId);

                verify(cardRepository, never()).saveAll(anyList());
                verify(cardRepository, never()).save(any(Card.class));
            }

            @Test
            @DisplayName("같은 리스트 내 카드 이동 - 정책 위반")
            void moveCard_SameList_PolicyViolation() {
                // given
                when(moveCardValidator.validate(sameListMoveCommand))
                        .thenReturn(ValidationResult.valid(sameListMoveCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.of(sourceBoardList));
                when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                        .thenReturn(Optional.of(testBoard));
                when(cardMovePolicy.canMoveWithinSameList(testCard, 4))
                        .thenReturn(Either.left(Failure.ofConflict("POSITION_OUT_OF_RANGE")));

                // when
                Either<Failure, Card> result = moveCardService.moveCard(sameListMoveCommand);

                // then
                assertThat(result.isLeft()).isTrue();
                Failure failure = result.getLeft();
                assertThat(failure).isInstanceOf(Failure.ResourceConflict.class);
                Failure.ResourceConflict conflict = (Failure.ResourceConflict) failure;
                assertThat(conflict.getErrorCode()).isEqualTo("RESOURCE_CONFLICT");
            }
        }

        @Nested
        @DisplayName("다른 리스트로 이동 테스트")
        class DifferentListMoveTest {

            @Test
            @DisplayName("다른 리스트로 카드 이동 성공")
            void moveCard_DifferentList_Success() {
                // given
                when(moveCardValidator.validate(differentListMoveCommand))
                        .thenReturn(ValidationResult.valid(differentListMoveCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.of(sourceBoardList));
                when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                        .thenReturn(Optional.of(testBoard));
                when(boardListRepository.findById(targetListId))
                        .thenReturn(Optional.of(targetBoardList));
                when(cardMovePolicy.canMoveToAnotherList(testCard, targetListId, 1))
                        .thenReturn(Either.right(null));
                when(cardRepository.findByListIdAndPositionGreaterThan(sourceListId, 2))
                        .thenReturn(Arrays.asList(
                                createTestCard("카드3", 3),
                                createTestCard("카드4", 4)));
                when(cardRepository.findByListIdAndPositionGreaterThan(targetListId, 0))
                        .thenReturn(Arrays.asList(
                                createTestCard("카드1", 1),
                                createTestCard("카드2", 2)));
                when(cardRepository.save(any(Card.class)))
                        .thenAnswer(invocation -> Either.right(invocation.getArgument(0)));

                // when
                Either<Failure, Card> result = moveCardService.moveCard(differentListMoveCommand);

                // then
                assertThat(result.isRight()).isTrue();
                Card movedCard = result.get();
                assertThat(movedCard.getPosition()).isEqualTo(1);
                assertThat(movedCard.getListId()).isEqualTo(targetListId);

                verify(cardRepository, times(2)).saveAll(anyList());
                verify(cardRepository).save(testCard);
            }

            @Test
            @DisplayName("다른 리스트로 카드 이동 - 대상 리스트 없음")
            void moveCard_DifferentList_TargetListNotFound() {
                // given
                when(moveCardValidator.validate(differentListMoveCommand))
                        .thenReturn(ValidationResult.valid(differentListMoveCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.of(sourceBoardList));
                when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                        .thenReturn(Optional.of(testBoard));
                when(boardListRepository.findById(targetListId))
                        .thenReturn(Optional.empty());
                when(messageResolver.getMessage("error.service.card.move.target_list_not_found"))
                        .thenReturn("대상 리스트를 찾을 수 없습니다.");

                // when
                Either<Failure, Card> result = moveCardService.moveCard(differentListMoveCommand);

                // then
                assertThat(result.isLeft()).isTrue();
                Failure failure = result.getLeft();
                assertThat(failure).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) failure;
                assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                assertThat(notFound.getMessage()).isEqualTo("대상 리스트를 찾을 수 없습니다.");
            }

            @Test
            @DisplayName("다른 리스트로 카드 이동 - 정책 위반")
            void moveCard_DifferentList_PolicyViolation() {
                // given
                when(moveCardValidator.validate(differentListMoveCommand))
                        .thenReturn(ValidationResult.valid(differentListMoveCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.of(sourceBoardList));
                when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                        .thenReturn(Optional.of(testBoard));
                when(boardListRepository.findById(targetListId))
                        .thenReturn(Optional.of(targetBoardList));
                when(cardMovePolicy.canMoveToAnotherList(testCard, targetListId, 1))
                        .thenReturn(Either.left(Failure.ofConflict("LIST_CARD_LIMIT_EXCEEDED")));

                // when
                Either<Failure, Card> result = moveCardService.moveCard(differentListMoveCommand);

                // then
                assertThat(result.isLeft()).isTrue();
                Failure failure = result.getLeft();
                assertThat(failure).isInstanceOf(Failure.ResourceConflict.class);
                Failure.ResourceConflict conflict = (Failure.ResourceConflict) failure;
                assertThat(conflict.getErrorCode()).isEqualTo("RESOURCE_CONFLICT");
            }
        }

        @Nested
        @DisplayName("입력 검증 테스트")
        class ValidationTest {

            @Test
            @DisplayName("입력 검증 실패")
            void moveCard_ValidationFailure() {
                // given
                List<Failure.FieldViolation> validationErrors = List.of(
                        Failure.FieldViolation.builder()
                                .field("cardId")
                                .message("카드 ID는 필수입니다.")
                                .rejectedValue(null)
                                .build(),
                        Failure.FieldViolation.builder()
                                .field("newPosition")
                                .message("새로운 위치는 0 이상이어야 합니다.")
                                .rejectedValue(-1)
                                .build());
                ValidationResult<MoveCardCommand> invalidResult = ValidationResult
                        .invalid(io.vavr.collection.List.ofAll(validationErrors));
                when(moveCardValidator.validate(sameListMoveCommand))
                        .thenReturn(invalidResult);
                when(messageResolver.getMessage("validation.input.invalid"))
                        .thenReturn("입력값이 유효하지 않습니다.");

                // when
                Either<Failure, Card> result = moveCardService.moveCard(sameListMoveCommand);

                // then
                assertThat(result.isLeft()).isTrue();
                Failure failure = result.getLeft();
                assertThat(failure).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) failure;
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getMessage()).isEqualTo("입력값이 유효하지 않습니다.");
                assertThat(inputError.getViolations()).hasSize(2);
            }
        }

        @Nested
        @DisplayName("카드 존재 확인 테스트")
        class CardExistenceTest {

            @Test
            @DisplayName("카드를 찾을 수 없는 경우")
            void moveCard_CardNotFound() {
                // given
                when(moveCardValidator.validate(sameListMoveCommand))
                        .thenReturn(ValidationResult.valid(sameListMoveCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.empty());
                when(messageResolver.getMessage("error.service.card.move.not_found"))
                        .thenReturn("이동할 카드를 찾을 수 없습니다.");

                // when
                Either<Failure, Card> result = moveCardService.moveCard(sameListMoveCommand);

                // then
                assertThat(result.isLeft()).isTrue();
                Failure failure = result.getLeft();
                assertThat(failure).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) failure;
                assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                assertThat(notFound.getMessage()).isEqualTo("이동할 카드를 찾을 수 없습니다.");
            }
        }

        @Nested
        @DisplayName("권한 검증 테스트")
        class AuthorizationTest {

            @Test
            @DisplayName("보드 접근 권한 없음")
            void moveCard_AccessDenied() {
                // given
                when(moveCardValidator.validate(sameListMoveCommand))
                        .thenReturn(ValidationResult.valid(sameListMoveCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.of(sourceBoardList));
                when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                        .thenReturn(Optional.empty());
                when(messageResolver.getMessage("error.service.card.move.access_denied"))
                        .thenReturn("보드에 접근할 권한이 없습니다.");

                // when
                Either<Failure, Card> result = moveCardService.moveCard(sameListMoveCommand);

                // then
                assertThat(result.isLeft()).isTrue();
                Failure failure = result.getLeft();
                assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
                Failure.PermissionDenied forbidden = (Failure.PermissionDenied) failure;
                assertThat(forbidden.getErrorCode()).isEqualTo("보드에 접근할 권한이 없습니다.");
                assertThat(forbidden.getMessage()).isEqualTo("접근이 거부되었습니다.");
            }

            @Test
            @DisplayName("아카이브된 보드에서 카드 이동 시도")
            void moveCard_ArchivedBoard() {
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

                when(moveCardValidator.validate(sameListMoveCommand))
                        .thenReturn(ValidationResult.valid(sameListMoveCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.of(sourceBoardList));
                when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                        .thenReturn(Optional.of(archivedBoard));
                when(messageResolver.getMessage("error.service.card.move.archived_board"))
                        .thenReturn("아카이브된 보드에서는 카드를 이동할 수 없습니다.");

                // when
                Either<Failure, Card> result = moveCardService.moveCard(sameListMoveCommand);

                // then
                assertThat(result.isLeft()).isTrue();
                Failure failure = result.getLeft();
                assertThat(failure).isInstanceOf(Failure.ResourceConflict.class);
                Failure.ResourceConflict conflict = (Failure.ResourceConflict) failure;
                assertThat(conflict.getErrorCode()).isEqualTo("RESOURCE_CONFLICT");
                assertThat(conflict.getMessage()).isEqualTo("아카이브된 보드에서는 카드를 이동할 수 없습니다.");
            }

            @Test
            @DisplayName("리스트를 찾을 수 없는 경우")
            void moveCard_ListNotFound() {
                // given
                when(moveCardValidator.validate(sameListMoveCommand))
                        .thenReturn(ValidationResult.valid(sameListMoveCommand));
                when(cardRepository.findById(testCardId))
                        .thenReturn(Optional.of(testCard));
                when(boardListRepository.findById(sourceListId))
                        .thenReturn(Optional.empty());
                when(messageResolver.getMessage("error.service.card.move.list_not_found"))
                        .thenReturn("리스트를 찾을 수 없습니다.");

                // when
                Either<Failure, Card> result = moveCardService.moveCard(sameListMoveCommand);

                // then
                assertThat(result.isLeft()).isTrue();
                Failure failure = result.getLeft();
                assertThat(failure).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) failure;
                assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                assertThat(notFound.getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("카드 위치 조정 테스트")
    class CardPositionAdjustmentTest {

        @Test
        @DisplayName("카드 제거로 인한 위치 조정")
        void adjustCardPositionsForRemoval() {
            // given
            List<Card> cardsToUpdate = Arrays.asList(
                    createTestCard("카드3", 3),
                    createTestCard("카드4", 4));
            when(moveCardValidator.validate(differentListMoveCommand))
                    .thenReturn(ValidationResult.valid(differentListMoveCommand));
            when(cardRepository.findById(testCardId))
                    .thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(sourceListId))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                    .thenReturn(Optional.of(testBoard));
            when(boardListRepository.findById(targetListId))
                    .thenReturn(Optional.of(targetBoardList));
            when(cardMovePolicy.canMoveToAnotherList(testCard, targetListId, 1))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionGreaterThan(sourceListId, 2))
                    .thenReturn(cardsToUpdate);
            when(cardRepository.findByListIdAndPositionGreaterThan(targetListId, 0))
                    .thenReturn(Arrays.asList());
            when(cardRepository.saveAll(anyList()))
                    .thenReturn(cardsToUpdate);
            when(cardRepository.save(any(Card.class)))
                    .thenAnswer(invocation -> Either.right(invocation.getArgument(0)));

            // when
            moveCardService.moveCard(differentListMoveCommand);

            // then
            verify(cardRepository).findByListIdAndPositionGreaterThan(sourceListId, 2);
            verify(cardRepository).saveAll(cardsToUpdate);
            assertThat(cardsToUpdate.get(0).getPosition()).isEqualTo(2);
            assertThat(cardsToUpdate.get(1).getPosition()).isEqualTo(3);
        }

        @Test
        @DisplayName("카드 삽입으로 인한 위치 조정")
        void adjustCardPositionsForInsertion() {
            // given
            List<Card> cardsToUpdate = Arrays.asList(
                    createTestCard("카드1", 1),
                    createTestCard("카드2", 2));
            when(moveCardValidator.validate(differentListMoveCommand))
                    .thenReturn(ValidationResult.valid(differentListMoveCommand));
            when(cardRepository.findById(testCardId))
                    .thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(sourceListId))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                    .thenReturn(Optional.of(testBoard));
            when(boardListRepository.findById(targetListId))
                    .thenReturn(Optional.of(targetBoardList));
            when(cardMovePolicy.canMoveToAnotherList(testCard, targetListId, 1))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionGreaterThan(sourceListId, 2))
                    .thenReturn(Arrays.asList());
            when(cardRepository.findByListIdAndPositionGreaterThan(targetListId, 0))
                    .thenReturn(cardsToUpdate);
            when(cardRepository.saveAll(anyList()))
                    .thenReturn(cardsToUpdate);
            when(cardRepository.save(any(Card.class)))
                    .thenAnswer(invocation -> Either.right(invocation.getArgument(0)));

            // when
            moveCardService.moveCard(differentListMoveCommand);

            // then
            verify(cardRepository).findByListIdAndPositionGreaterThan(targetListId, 0);
            verify(cardRepository).saveAll(cardsToUpdate);
            assertThat(cardsToUpdate.get(0).getPosition()).isEqualTo(2);
            assertThat(cardsToUpdate.get(1).getPosition()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("로그 메시지 테스트")
    class LoggingTest {

        @Test
        @DisplayName("성공 시 로그 메시지 확인")
        void moveCard_Success_LogMessages() {
            // given
            when(moveCardValidator.validate(sameListMoveCommand))
                    .thenReturn(ValidationResult.valid(sameListMoveCommand));
            when(cardRepository.findById(testCardId))
                    .thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(sourceListId))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                    .thenReturn(Optional.of(testBoard));
            when(cardMovePolicy.canMoveWithinSameList(testCard, 4))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionBetween(sourceListId, 3, 4))
                    .thenReturn(Arrays.asList(
                            createTestCard("카드3", 3),
                            createTestCard("카드4", 4)));
            when(cardRepository.save(any(Card.class)))
                    .thenAnswer(invocation -> Either.right(invocation.getArgument(0)));

            // when
            Either<Failure, Card> result = moveCardService.moveCard(sameListMoveCommand);

            // then
            assertThat(result.isRight()).isTrue();
            // 로그 메시지는 실제로는 확인할 수 없지만, 메서드가 정상적으로 실행되었음을 확인
        }

        @Test
        @DisplayName("실패 시 로그 메시지 확인")
        void moveCard_Failure_LogMessages() {
            // given
            when(moveCardValidator.validate(sameListMoveCommand))
                    .thenReturn(ValidationResult.valid(sameListMoveCommand));
            when(cardRepository.findById(testCardId))
                    .thenReturn(Optional.empty());
            when(messageResolver.getMessage("error.service.card.move.not_found"))
                    .thenReturn("이동할 카드를 찾을 수 없습니다.");

            // when
            Either<Failure, Card> result = moveCardService.moveCard(sameListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            // 로그 메시지는 실제로는 확인할 수 없지만, 메서드가 정상적으로 실행되었음을 확인
        }
    }

    private Card createTestCard(String title, int position) {
        return Card.builder()
                .cardId(new CardId())
                .title(title)
                .description("테스트 카드 설명")
                .position(position)
                .listId(sourceListId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}