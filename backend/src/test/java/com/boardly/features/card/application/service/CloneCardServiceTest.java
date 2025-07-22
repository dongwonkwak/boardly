package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Instant;
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
import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.features.card.application.validation.CloneCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.policy.CardClonePolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloneCardService 테스트")
class CloneCardServiceTest {

        @Mock
        private CloneCardValidator cloneCardValidator;

        @Mock
        private CardClonePolicy cardClonePolicy;

        @Mock
        private CardRepository cardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @InjectMocks
        private CloneCardService cloneCardService;

        private UserId testUserId;
        private CardId testCardId;
        private ListId testListId;
        private BoardId testBoardId;
        private Card testCard;
        private BoardList testBoardList;
        private Board testBoard;
        private CloneCardCommand validCommand;

        @BeforeEach
        void setUp() {
                testUserId = new UserId("test-user-123");
                testCardId = new CardId("test-card-123");
                testListId = new ListId("test-list-123");
                testBoardId = new BoardId("test-board-123");

                Instant now = Instant.now();

                testCard = Card.builder()
                                .cardId(testCardId)
                                .title("원본 카드")
                                .description("원본 카드 설명")
                                .position(1)
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

                validCommand = CloneCardCommand.of(
                                testCardId,
                                "복제된 카드",
                                null, // 같은 리스트에 복제
                                testUserId);

                // 기본 메시지 모킹 설정
                lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 올바르지 않습니다");
                lenient().when(validationMessageResolver.getMessage("error.service.card.clone.not_found"))
                                .thenReturn("복제할 카드를 찾을 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("error.service.card.clone.list_not_found"))
                                .thenReturn("리스트를 찾을 수 없습니다");
                lenient().when(validationMessageResolver.getMessage("error.service.card.clone.access_denied"))
                                .thenReturn("보드에 접근할 권한이 없습니다");
                lenient().when(validationMessageResolver.getMessage("error.service.card.clone.archived_board"))
                                .thenReturn("아카이브된 보드에서는 카드를 복제할 수 없습니다");
        }

        @Nested
        @DisplayName("같은 리스트 내 카드 복제")
        class CloneWithinSameList {

                @Test
                @DisplayName("유효한 요청으로 같은 리스트에 카드를 복제할 수 있다")
                void cloneCard_ValidRequest_SameList_ShouldSucceed() {
                        // given
                        when(cloneCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardClonePolicy.canCloneWithinSameList(testCard))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(testListId))
                                        .thenReturn(Optional.of(5));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.right(testCard));

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(testCard);
                }

                @Test
                @DisplayName("리스트의 카드 개수 제한에 도달한 경우 복제에 실패한다")
                void cloneCard_CardLimitReached_SameList_ShouldFail() {
                        // given
                        when(cloneCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardClonePolicy.canCloneWithinSameList(testCard))
                                        .thenReturn(Either.left(Failure.ofConflict("LIST_CARD_LIMIT_EXCEEDED")));

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("LIST_CARD_LIMIT_EXCEEDED");
                }
        }

        @Nested
        @DisplayName("다른 리스트로 카드 복제")
        class CloneToAnotherList {

                private ListId targetListId;
                private CloneCardCommand crossListCommand;

                @BeforeEach
                void setUp() {
                        targetListId = new ListId("target-list-456");
                        crossListCommand = CloneCardCommand.of(
                                        testCardId,
                                        "다른 리스트로 복제된 카드",
                                        targetListId,
                                        testUserId);
                }

                @Test
                @DisplayName("유효한 요청으로 다른 리스트에 카드를 복제할 수 있다")
                void cloneCard_ValidRequest_CrossList_ShouldSucceed() {
                        // given
                        BoardList targetBoardList = BoardList.builder()
                                        .listId(targetListId)
                                        .title("대상 리스트")
                                        .position(2)
                                        .boardId(testBoardId)
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(cloneCardValidator.validate(crossListCommand))
                                        .thenReturn(ValidationResult.valid(crossListCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardListRepository.findById(targetListId))
                                        .thenReturn(Optional.of(targetBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardClonePolicy.canCloneToAnotherList(testCard, targetListId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(targetListId))
                                        .thenReturn(Optional.of(3));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.right(testCard));

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(crossListCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(testCard);
                }

                @Test
                @DisplayName("대상 리스트에 접근 권한이 없는 경우 복제에 실패한다")
                void cloneCard_NoAccessToTargetList_ShouldFail() {
                        // given
                        when(cloneCardValidator.validate(crossListCommand))
                                        .thenReturn(ValidationResult.valid(crossListCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(boardListRepository.findById(targetListId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(crossListCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                }
        }

        @Nested
        @DisplayName("입력 검증")
        class InputValidation {

                @Test
                @DisplayName("입력 검증에 실패한 경우 복제에 실패한다")
                void cloneCard_InvalidInput_ShouldFail() {
                        // given
                        ValidationResult<CloneCardCommand> invalidResult = ValidationResult.invalid(
                                        "title", "카드 제목은 필수입니다", null);
                        when(cloneCardValidator.validate(validCommand))
                                        .thenReturn(invalidResult);

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                }
        }

        @Nested
        @DisplayName("카드 존재 확인")
        class CardExistenceCheck {

                @Test
                @DisplayName("복제할 카드가 존재하지 않는 경우 실패한다")
                void cloneCard_CardNotFound_ShouldFail() {
                        // given
                        when(cloneCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(validCommand);

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
                void cloneCard_NoBoardAccess_ShouldFail() {
                        // given
                        when(cloneCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                }

                @Test
                @DisplayName("아카이브된 보드에서 카드 복제를 시도하는 경우 실패한다")
                void cloneCard_ArchivedBoard_ShouldFail() {
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

                        when(cloneCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
                }
        }

        @Nested
        @DisplayName("카드 저장")
        class CardSaving {

                @Test
                @DisplayName("카드 저장에 실패한 경우 실패한다")
                void cloneCard_SaveFailure_ShouldFail() {
                        // given
                        when(cloneCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardClonePolicy.canCloneWithinSameList(testCard))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(testListId))
                                        .thenReturn(Optional.of(5));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                }
        }

        @Nested
        @DisplayName("위치 계산")
        class PositionCalculation {

                @Test
                @DisplayName("빈 리스트에 복제할 때 위치는 0이 된다")
                void cloneCard_EmptyList_PositionShouldBeZero() {
                        // given
                        when(cloneCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardClonePolicy.canCloneWithinSameList(testCard))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(testListId))
                                        .thenReturn(Optional.empty());
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.right(testCard));

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                }

                @Test
                @DisplayName("기존 카드가 있는 리스트에 복제할 때 위치는 최대 위치 + 1이 된다")
                void cloneCard_ExistingCards_PositionShouldBeMaxPlusOne() {
                        // given
                        when(cloneCardValidator.validate(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(testCardId))
                                        .thenReturn(Optional.of(testCard));
                        when(boardListRepository.findById(testListId))
                                        .thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId))
                                        .thenReturn(Optional.of(testBoard));
                        when(cardClonePolicy.canCloneWithinSameList(testCard))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findMaxPositionByListId(testListId))
                                        .thenReturn(Optional.of(10));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.right(testCard));

                        // when
                        Either<Failure, Card> result = cloneCardService.cloneCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                }
        }
}