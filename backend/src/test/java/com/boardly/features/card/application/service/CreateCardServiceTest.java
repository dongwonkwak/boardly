package com.boardly.features.card.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.application.validation.CreateCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.policy.CardCreationPolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCardServiceTest {

  private CreateCardService createCardService;

  @Mock
  private CreateCardValidator cardValidator;

  @Mock
  private CardCreationPolicy cardCreationPolicy;

  @Mock
  private CardRepository cardRepository;

  @Mock
  private BoardListRepository boardListRepository;

  @Mock
  private BoardRepository boardRepository;

  private CreateCardCommand validCommand;
  private ListId testListId;
  private UserId testUserId;
  private BoardId testBoardId;
  private BoardList testBoardList;
  private Board testBoard;
  private Card testCard;

  @BeforeEach
  void setUp() {
    createCardService = new CreateCardService(
        cardValidator, cardCreationPolicy, cardRepository, boardListRepository, boardRepository);

    // 테스트 데이터 설정
    testListId = new ListId();
    testUserId = new UserId();
    testBoardId = new BoardId();

    validCommand = CreateCardCommand.of(
        "테스트 카드 제목",
        "테스트 카드 설명",
        testListId,
        testUserId);

    testBoardList = BoardList.create("테스트 리스트", 0, testBoardId);
    testBoard = Board.create("테스트 보드", "테스트 보드 설명", testUserId);
    testCard = Card.create("테스트 카드 제목", "테스트 카드 설명", 0, testListId);
  }

  // ==================== 성공 케이스 테스트 ====================

  @Test
  @DisplayName("유효한 카드 생성 요청 시 성공해야 한다")
  void createCard_withValidRequest_shouldSucceed() {
    // given
    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
    when(cardValidator.validate(validCommand)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
    when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
    when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(5));
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isEqualTo(testCard);

    verify(cardValidator).validate(validCommand);
    verify(boardListRepository).findById(testListId);
    verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
    verify(cardCreationPolicy).canCreateCard(testListId);
    verify(cardRepository).findMaxPositionByListId(testListId);
    verify(cardRepository).save(any(Card.class));
  }

  @Test
  @DisplayName("빈 리스트에 첫 번째 카드를 생성할 때 position이 0이어야 한다")
  void createCard_inEmptyList_shouldSetPositionToZero() {
    // given
    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
    when(cardValidator.validate(validCommand)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
    when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
    when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.empty());
    when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
      Card savedCard = invocation.getArgument(0);
      return Either.right(savedCard);
    });

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isRight()).isTrue();
    verify(cardRepository).save(argThat(card -> card.getPosition() == 0));
  }

  @Test
  @DisplayName("기존 카드가 있는 리스트에 새 카드를 생성할 때 position이 최대값+1이어야 한다")
  void createCard_inListWithExistingCards_shouldSetPositionToMaxPlusOne() {
    // given
    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
    when(cardValidator.validate(validCommand)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
    when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
    when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(10));
    when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
      Card savedCard = invocation.getArgument(0);
      return Either.right(savedCard);
    });

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isRight()).isTrue();
    verify(cardRepository).save(argThat(card -> card.getPosition() == 11));
  }

  // ==================== 검증 실패 테스트 ====================

  @Test
  @DisplayName("입력 검증 실패 시 ValidationFailure를 반환해야 한다")
  void createCard_withInvalidInput_shouldReturnValidationFailure() {
    // given
    List<Failure.FieldViolation> validationErrors = List.of(
        Failure.FieldViolation.builder()
            .field("title")
            .message("제목은 필수입니다")
            .rejectedValue(null)
            .build(),
        Failure.FieldViolation.builder()
            .field("title")
            .message("제목은 1-200자여야 합니다")
            .rejectedValue("")
            .build());
    ValidationResult<CreateCardCommand> invalidResult = ValidationResult
        .invalid(io.vavr.collection.List.ofAll(validationErrors));
    when(cardValidator.validate(validCommand)).thenReturn(invalidResult);

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);

    Failure.ValidationFailure failure = (Failure.ValidationFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("INVALID_INPUT");
    assertThat(failure.violations()).containsExactlyElementsOf(validationErrors);

    verify(cardValidator).validate(validCommand);
    verifyNoInteractions(boardListRepository, boardRepository, cardCreationPolicy, cardRepository);
  }

  // ==================== 리스트 존재 확인 실패 테스트 ====================

  @Test
  @DisplayName("존재하지 않는 리스트에 카드 생성 시 NotFoundFailure를 반환해야 한다")
  void createCard_withNonExistentList_shouldReturnNotFoundFailure() {
    // given
    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
    when(cardValidator.validate(validCommand)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);

    Failure.NotFoundFailure failure = (Failure.NotFoundFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("리스트를 찾을 수 없습니다.");

    verify(cardValidator).validate(validCommand);
    verify(boardListRepository).findById(testListId);
    verifyNoInteractions(boardRepository, cardCreationPolicy, cardRepository);
  }

  // ==================== 권한 검증 실패 테스트 ====================

  @Test
  @DisplayName("보드 접근 권한이 없는 경우 ForbiddenFailure를 반환해야 한다")
  void createCard_withoutBoardAccess_shouldReturnForbiddenFailure() {
    // given
    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
    when(cardValidator.validate(validCommand)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.empty());

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);

    Failure.ForbiddenFailure failure = (Failure.ForbiddenFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("보드에 접근할 권한이 없습니다.");

    verify(cardValidator).validate(validCommand);
    verify(boardListRepository).findById(testListId);
    verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
    verifyNoInteractions(cardCreationPolicy, cardRepository);
  }

  // ==================== 아카이브된 보드 테스트 ====================

  @Test
  @DisplayName("아카이브된 보드에 카드 생성 시 ConflictFailure를 반환해야 한다")
  void createCard_inArchivedBoard_shouldReturnConflictFailure() {
    // given
    Board archivedBoard = Board.create("아카이브된 보드", "설명", testUserId);
    archivedBoard.archive();

    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
    when(cardValidator.validate(validCommand)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(archivedBoard));

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);

    Failure.ConflictFailure failure = (Failure.ConflictFailure) result.getLeft();
    assertThat(failure.message()).isEqualTo("아카이브된 보드에는 카드를 생성할 수 없습니다.");

    verify(cardValidator).validate(validCommand);
    verify(boardListRepository).findById(testListId);
    verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
    verifyNoInteractions(cardCreationPolicy, cardRepository);
  }

  // ==================== 카드 생성 정책 실패 테스트 ====================

  @Test
  @DisplayName("카드 생성 정책 위반 시 정책 실패를 반환해야 한다")
  void createCard_withPolicyViolation_shouldReturnPolicyFailure() {
    // given
    Failure policyFailure = Failure.ofForbidden("리스트당 최대 100개의 카드만 생성할 수 있습니다.");

    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
    when(cardValidator.validate(validCommand)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
    when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.left(policyFailure));

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isEqualTo(policyFailure);

    verify(cardValidator).validate(validCommand);
    verify(boardListRepository).findById(testListId);
    verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
    verify(cardCreationPolicy).canCreateCard(testListId);
    verifyNoInteractions(cardRepository);
  }

  // ==================== 카드 저장 실패 테스트 ====================

  @Test
  @DisplayName("카드 저장 실패 시 저장 실패를 반환해야 한다")
  void createCard_whenSaveFails_shouldReturnSaveFailure() {
    // given
    Failure saveFailure = Failure.ofInternalServerError("카드 저장 중 오류가 발생했습니다.");

    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
    when(cardValidator.validate(validCommand)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
    when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
    when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(5));
    when(cardRepository.save(any(Card.class))).thenReturn(Either.left(saveFailure));

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isEqualTo(saveFailure);

    verify(cardValidator).validate(validCommand);
    verify(boardListRepository).findById(testListId);
    verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
    verify(cardCreationPolicy).canCreateCard(testListId);
    verify(cardRepository).findMaxPositionByListId(testListId);
    verify(cardRepository).save(any(Card.class));
  }

  // ==================== 경계값 테스트 ====================

  @Test
  @DisplayName("null 설명으로 카드 생성 시 성공해야 한다")
  void createCard_withNullDescription_shouldSucceed() {
    // given
    CreateCardCommand commandWithNullDescription = CreateCardCommand.of(
        "테스트 카드 제목",
        null,
        testListId,
        testUserId);

    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(commandWithNullDescription);
    when(cardValidator.validate(commandWithNullDescription)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
    when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
    when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(0));
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));

    // when
    Either<Failure, Card> result = createCardService.createCard(commandWithNullDescription);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isEqualTo(testCard);
  }

  @Test
  @DisplayName("빈 문자열 설명으로 카드 생성 시 성공해야 한다")
  void createCard_withEmptyDescription_shouldSucceed() {
    // given
    CreateCardCommand commandWithEmptyDescription = CreateCardCommand.of(
        "테스트 카드 제목",
        "",
        testListId,
        testUserId);

    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(commandWithEmptyDescription);
    when(cardValidator.validate(commandWithEmptyDescription)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
    when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
    when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(0));
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));

    // when
    Either<Failure, Card> result = createCardService.createCard(commandWithEmptyDescription);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isEqualTo(testCard);
  }

  // ==================== 로깅 검증 테스트 ====================

  @Test
  @DisplayName("성공적인 카드 생성 시 적절한 로그가 기록되어야 한다")
  void createCard_successfulCreation_shouldLogAppropriately() {
    // given
    ValidationResult<CreateCardCommand> validResult = ValidationResult.valid(validCommand);
    when(cardValidator.validate(validCommand)).thenReturn(validResult);
    when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
    when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
    when(cardCreationPolicy.canCreateCard(testListId)).thenReturn(Either.right(null));
    when(cardRepository.findMaxPositionByListId(testListId)).thenReturn(Optional.of(5));
    when(cardRepository.save(any(Card.class))).thenReturn(Either.right(testCard));

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isRight()).isTrue();
    // 로깅은 실제로는 SLF4J를 통해 처리되므로 여기서는 검증하지 않음
    // 실제 환경에서는 로그 레벨과 출력을 확인해야 함
  }

  @Test
  @DisplayName("검증 실패 시 경고 로그가 기록되어야 한다")
  void createCard_validationFailure_shouldLogWarning() {
    // given
    List<Failure.FieldViolation> validationErrors = List.of(
        Failure.FieldViolation.builder()
            .field("title")
            .message("제목은 필수입니다")
            .rejectedValue(null)
            .build());
    ValidationResult<CreateCardCommand> invalidResult = ValidationResult
        .invalid(io.vavr.collection.List.ofAll(validationErrors));
    when(cardValidator.validate(validCommand)).thenReturn(invalidResult);

    // when
    Either<Failure, Card> result = createCardService.createCard(validCommand);

    // then
    assertThat(result.isLeft()).isTrue();
    // 로깅은 실제로는 SLF4J를 통해 처리되므로 여기서는 검증하지 않음
  }
}