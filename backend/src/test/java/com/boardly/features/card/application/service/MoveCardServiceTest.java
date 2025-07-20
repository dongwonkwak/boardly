package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

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
import org.springframework.context.i18n.LocaleContextHolder;

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
  private MessageSource messageSource;

  private MoveCardService moveCardService;

  private CardId cardId;
  private ListId sourceListId;
  private ListId targetListId;
  private UserId userId;
  private BoardId boardId;
  private Card testCard;
  private BoardList testBoardList;
  private Board testBoard;

  @BeforeEach
  void setUp() {
    LocaleContextHolder.setLocale(Locale.KOREAN);

    // MessageSource Mock 설정
    lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
        .thenAnswer(invocation -> {
          String key = invocation.getArgument(0);
          return switch (key) {
            case "error.service.card.move.not_found" -> "이동할 카드를 찾을 수 없습니다";
            case "error.service.card.move.target_list_not_found" -> "대상 리스트를 찾을 수 없습니다";
            case "error.service.card.move.list_not_found" -> "리스트를 찾을 수 없습니다";
            case "error.service.card.move.access_denied" -> "보드에 접근할 권한이 없습니다";
            case "error.service.card.move.archived_board" -> "아카이브된 보드에서는 카드를 이동할 수 없습니다";
            default -> key;
          };
        });

    ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
    moveCardService = new MoveCardService(
        moveCardValidator, cardMovePolicy, cardRepository, boardListRepository, boardRepository, messageResolver);

    // 테스트 데이터 초기화
    cardId = new CardId("card-123");
    sourceListId = new ListId("list-123");
    targetListId = new ListId("list-456");
    userId = new UserId("user-123");
    boardId = new BoardId("board-123");

    testCard = Card.builder()
        .cardId(cardId)
        .title("테스트 카드")
        .description("테스트 설명")
        .position(2)
        .listId(sourceListId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    testBoardList = BoardList.create("테스트 리스트", 0, boardId);
    testBoard = Board.create("테스트 보드", "테스트 보드 설명", userId);
  }

  // ==================== 파라미터화 테스트 데이터 제공 메서드들 ====================

  private static Stream<Arguments> moveWithinSameListTestData() {
    return Stream.of(
        Arguments.of(0, "첫 번째 위치로 이동"),
        Arguments.of(1, "두 번째 위치로 이동"),
        Arguments.of(5, "중간 위치로 이동"),
        Arguments.of(10, "마지막 위치로 이동"));
  }

  private static Stream<Arguments> moveToAnotherListTestData() {
    return Stream.of(
        Arguments.of(0, "첫 번째 위치로 이동"),
        Arguments.of(1, "두 번째 위치로 이동"),
        Arguments.of(5, "중간 위치로 이동"),
        Arguments.of(10, "마지막 위치로 이동"));
  }

  // ==================== 성공 케이스 테스트 ====================

  @Test
  @DisplayName("같은 리스트 내에서 카드 이동이 성공해야 한다")
  void moveCard_withinSameList_shouldSucceed() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, 5, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(cardMovePolicy.canMoveWithinSameList(testCard, 5)).thenReturn(Either.right(null));
    when(cardRepository.findByListIdAndPositionBetween(sourceListId, 3, 5))
        .thenReturn(Arrays.asList(createTestCard("card-1", 3), createTestCard("card-2", 4)));
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));
    when(cardRepository.saveAll(any())).thenReturn(Arrays.asList());

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get().getPosition()).isEqualTo(5);
    verify(cardRepository).saveAll(any());
    verify(cardRepository).save(testCard);
  }

  @Test
  @DisplayName("다른 리스트로 카드 이동이 성공해야 한다")
  void moveCard_toAnotherList_shouldSucceed() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, targetListId, 3, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardListRepository.findById(targetListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(cardMovePolicy.canMoveToAnotherList(testCard, targetListId, 3)).thenReturn(Either.right(null));
    when(cardRepository.findByListIdAndPositionGreaterThan(sourceListId, 2))
        .thenReturn(Arrays.asList(createTestCard("card-3", 3), createTestCard("card-4", 4)));
    when(cardRepository.findByListIdAndPositionGreaterThan(targetListId, 2))
        .thenReturn(Arrays.asList(createTestCard("card-5", 3), createTestCard("card-6", 4)));
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));
    when(cardRepository.saveAll(any())).thenReturn(Arrays.asList());

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get().getListId()).isEqualTo(targetListId);
    assertThat(result.get().getPosition()).isEqualTo(3);
    verify(cardRepository, org.mockito.Mockito.times(2)).saveAll(any()); // 원본 리스트와 대상 리스트 각각 한 번씩
    verify(cardRepository).save(testCard);
  }

  @Test
  @DisplayName("같은 위치로 이동할 때는 위치 조정 없이 성공해야 한다")
  void moveCard_samePosition_shouldSucceedWithoutAdjustment() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, 2, userId); // 현재 위치와 동일
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(cardMovePolicy.canMoveWithinSameList(testCard, 2)).thenReturn(Either.right(null));

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get().getPosition()).isEqualTo(2);
    verify(cardRepository, org.mockito.Mockito.never()).saveAll(any());
  }

  @ParameterizedTest
  @DisplayName("같은 리스트 내에서 다양한 위치로 이동이 성공해야 한다")
  @MethodSource("moveWithinSameListTestData")
  void moveCard_withinSameList_variousPositions_shouldSucceed(int newPosition, String description) {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, newPosition, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(cardMovePolicy.canMoveWithinSameList(testCard, newPosition)).thenReturn(Either.right(null));
    when(cardRepository.findByListIdAndPositionBetween(any(), anyInt(), anyInt()))
        .thenReturn(Arrays.asList());
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get().getPosition()).isEqualTo(newPosition);
  }

  @ParameterizedTest
  @DisplayName("다른 리스트로 다양한 위치로 이동이 성공해야 한다")
  @MethodSource("moveToAnotherListTestData")
  void moveCard_toAnotherList_variousPositions_shouldSucceed(int newPosition, String description) {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, targetListId, newPosition, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardListRepository.findById(targetListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(cardMovePolicy.canMoveToAnotherList(testCard, targetListId, newPosition)).thenReturn(Either.right(null));
    when(cardRepository.findByListIdAndPositionGreaterThan(any(), anyInt()))
        .thenReturn(Arrays.asList());
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get().getListId()).isEqualTo(targetListId);
    assertThat(result.get().getPosition()).isEqualTo(newPosition);
  }

  // ==================== 실패 케이스 테스트 ====================

  @Test
  @DisplayName("입력 검증 실패 시 에러를 반환해야 한다")
  void moveCard_validationFailure_shouldReturnError() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, -1, userId);
    ValidationResult<MoveCardCommand> invalidResult = ValidationResult.invalid(
        io.vavr.collection.List.of(Failure.FieldViolation.builder()
            .field("newPosition")
            .message("위치는 0 이상이어야 합니다")
            .rejectedValue(-1)
            .build()));

    when(moveCardValidator.validate(command)).thenReturn(invalidResult);

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
    assertThat(result.getLeft().message()).isEqualTo("INVALID_INPUT");
  }

  @Test
  @DisplayName("카드가 존재하지 않으면 에러를 반환해야 한다")
  void moveCard_cardNotFound_shouldReturnError() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, 5, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
    assertThat(result.getLeft().message()).isEqualTo("이동할 카드를 찾을 수 없습니다");
  }

  @Test
  @DisplayName("리스트가 존재하지 않으면 에러를 반환해야 한다")
  void moveCard_listNotFound_shouldReturnError() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, 5, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.empty());

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
    assertThat(result.getLeft().message()).isEqualTo("리스트를 찾을 수 없습니다");
  }

  @Test
  @DisplayName("보드 접근 권한이 없으면 에러를 반환해야 한다")
  void moveCard_accessDenied_shouldReturnError() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, 5, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
    assertThat(result.getLeft().message()).isEqualTo("보드에 접근할 권한이 없습니다");
  }

  @Test
  @DisplayName("아카이브된 보드에서는 카드 이동이 실패해야 한다")
  void moveCard_archivedBoard_shouldReturnError() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, 5, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);
    Board archivedBoard = Board.builder()
        .boardId(boardId)
        .title("아카이브된 보드")
        .description("아카이브된 보드")
        .ownerId(userId)
        .isArchived(true)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(archivedBoard));

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
    assertThat(result.getLeft().message()).isEqualTo("아카이브된 보드에서는 카드를 이동할 수 없습니다");
  }

  @Test
  @DisplayName("대상 리스트가 존재하지 않으면 에러를 반환해야 한다")
  void moveCard_targetListNotFound_shouldReturnError() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, targetListId, 5, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(boardListRepository.findById(targetListId)).thenReturn(Optional.empty());

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
    assertThat(result.getLeft().message()).isEqualTo("대상 리스트를 찾을 수 없습니다");
  }

  @Test
  @DisplayName("같은 리스트 내 이동 정책 검증 실패 시 에러를 반환해야 한다")
  void moveCard_sameListPolicyFailure_shouldReturnError() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, 5, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);
    Failure policyFailure = Failure.ofConflict("POSITION_OUT_OF_RANGE");

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(cardMovePolicy.canMoveWithinSameList(testCard, 5)).thenReturn(Either.left(policyFailure));

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isEqualTo(policyFailure);
  }

  @Test
  @DisplayName("다른 리스트로 이동 정책 검증 실패 시 에러를 반환해야 한다")
  void moveCard_anotherListPolicyFailure_shouldReturnError() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, targetListId, 5, userId);
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);
    Failure policyFailure = Failure.ofConflict("LIST_CARD_LIMIT_EXCEEDED");

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardListRepository.findById(targetListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(cardMovePolicy.canMoveToAnotherList(testCard, targetListId, 5)).thenReturn(Either.left(policyFailure));

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isEqualTo(policyFailure);
  }

  // ==================== 위치 조정 테스트 ====================

  @Test
  @DisplayName("뒤로 이동할 때 다른 카드들의 위치가 올바르게 조정되어야 한다")
  void moveCard_backwardMove_shouldAdjustPositionsCorrectly() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, 5, userId); // 2 -> 5 (뒤로 이동)
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    List<Card> cardsToAdjust = Arrays.asList(
        createTestCard("card-1", 3),
        createTestCard("card-2", 4));

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(cardMovePolicy.canMoveWithinSameList(testCard, 5)).thenReturn(Either.right(null));
    when(cardRepository.findByListIdAndPositionBetween(sourceListId, 3, 5)).thenReturn(cardsToAdjust);
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));
    when(cardRepository.saveAll(any())).thenReturn(cardsToAdjust);

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isRight()).isTrue();
    verify(cardRepository).saveAll(cardsToAdjust);
    assertThat(cardsToAdjust.get(0).getPosition()).isEqualTo(2); // 3 -> 2
    assertThat(cardsToAdjust.get(1).getPosition()).isEqualTo(3); // 4 -> 3
  }

  @Test
  @DisplayName("앞으로 이동할 때 다른 카드들의 위치가 올바르게 조정되어야 한다")
  void moveCard_forwardMove_shouldAdjustPositionsCorrectly() {
    // given
    MoveCardCommand command = new MoveCardCommand(cardId, null, 0, userId); // 2 -> 0 (앞으로 이동)
    ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(command);

    List<Card> cardsToAdjust = Arrays.asList(
        createTestCard("card-1", 0),
        createTestCard("card-2", 1));

    when(moveCardValidator.validate(command)).thenReturn(validResult);
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(boardListRepository.findById(sourceListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(testBoard));
    when(cardMovePolicy.canMoveWithinSameList(testCard, 0)).thenReturn(Either.right(null));
    when(cardRepository.findByListIdAndPositionBetween(sourceListId, 0, 1)).thenReturn(cardsToAdjust);
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));
    when(cardRepository.saveAll(any())).thenReturn(cardsToAdjust);

    // when
    Either<Failure, Card> result = moveCardService.moveCard(command);

    // then
    assertThat(result.isRight()).isTrue();
    verify(cardRepository).saveAll(cardsToAdjust);
    assertThat(cardsToAdjust.get(0).getPosition()).isEqualTo(1); // 0 -> 1
    assertThat(cardsToAdjust.get(1).getPosition()).isEqualTo(2); // 1 -> 2
  }

  // ==================== 헬퍼 메서드 ====================

  private Card createTestCard(String cardIdStr, int position) {
    return Card.builder()
        .cardId(new CardId(cardIdStr))
        .title("테스트 카드 " + cardIdStr)
        .description("테스트 설명")
        .position(position)
        .listId(sourceListId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }
}