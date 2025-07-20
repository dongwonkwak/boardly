package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.GetCardCommand;
import com.boardly.features.card.application.validation.GetCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCardService 테스트")
class GetCardServiceTest {

  @Mock
  private GetCardValidator cardValidator;

  @Mock
  private CardRepository cardRepository;

  @Mock
  private BoardListRepository boardListRepository;

  @Mock
  private BoardRepository boardRepository;

  private GetCardService getCardService;

  @BeforeEach
  void setUp() {
    getCardService = new GetCardService(
        cardValidator,
        cardRepository,
        boardListRepository,
        boardRepository);
  }

  @Test
  @DisplayName("유효한 카드 조회 요청은 성공해야 한다")
  void shouldGetCardSuccessfully() {
    // given
    CardId cardId = new CardId("card-123");
    UserId userId = new UserId("user-123");
    ListId listId = new ListId("list-123");
    BoardId boardId = new BoardId("board-123");

    GetCardCommand command = new GetCardCommand(cardId, userId);

    Card card = Card.builder()
        .cardId(cardId)
        .title("테스트 카드")
        .description("테스트 설명")
        .position(1)
        .listId(listId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    BoardList boardList = BoardList.builder()
        .listId(listId)
        .title("테스트 리스트")
        .position(1)
        .boardId(boardId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    Board board = Board.builder()
        .boardId(boardId)
        .title("테스트 보드")
        .description("테스트 보드 설명")
        .ownerId(userId)
        .isArchived(false)
        .isStarred(false)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    // when
    when(cardValidator.validate(command)).thenReturn(ValidationResult.valid(command));
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
    when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));

    Either<Failure, Card> result = getCardService.getCard(command);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isEqualTo(card);
  }

  @Test
  @DisplayName("검증 실패 시 실패를 반환해야 한다")
  void shouldReturnFailureWhenValidationFails() {
    // given
    CardId cardId = new CardId("card-123");
    UserId userId = new UserId("user-123");
    GetCardCommand command = new GetCardCommand(cardId, userId);

    Failure.FieldViolation violation = Failure.FieldViolation.builder()
        .field("cardId")
        .message("카드 ID는 필수 항목입니다")
        .rejectedValue(null)
        .build();
    ValidationResult<GetCardCommand> invalidResult = ValidationResult.invalid(violation);

    // when
    when(cardValidator.validate(command)).thenReturn(invalidResult);

    Either<Failure, Card> result = getCardService.getCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
  }

  @Test
  @DisplayName("카드가 존재하지 않으면 실패를 반환해야 한다")
  void shouldReturnFailureWhenCardNotFound() {
    // given
    CardId cardId = new CardId("card-123");
    UserId userId = new UserId("user-123");
    GetCardCommand command = new GetCardCommand(cardId, userId);

    // when
    when(cardValidator.validate(command)).thenReturn(ValidationResult.valid(command));
    when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

    Either<Failure, Card> result = getCardService.getCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("카드를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("카드가 속한 리스트가 존재하지 않으면 실패를 반환해야 한다")
  void shouldReturnFailureWhenBoardListNotFound() {
    // given
    CardId cardId = new CardId("card-123");
    UserId userId = new UserId("user-123");
    ListId listId = new ListId("list-123");

    GetCardCommand command = new GetCardCommand(cardId, userId);

    Card card = Card.builder()
        .cardId(cardId)
        .title("테스트 카드")
        .description("테스트 설명")
        .position(1)
        .listId(listId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    // when
    when(cardValidator.validate(command)).thenReturn(ValidationResult.valid(command));
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
    when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

    Either<Failure, Card> result = getCardService.getCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("카드가 속한 리스트를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("보드 접근 권한이 없으면 실패를 반환해야 한다")
  void shouldReturnFailureWhenNoBoardAccess() {
    // given
    CardId cardId = new CardId("card-123");
    UserId userId = new UserId("user-123");
    ListId listId = new ListId("list-123");
    BoardId boardId = new BoardId("board-123");

    GetCardCommand command = new GetCardCommand(cardId, userId);

    Card card = Card.builder()
        .cardId(cardId)
        .title("테스트 카드")
        .description("테스트 설명")
        .position(1)
        .listId(listId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    BoardList boardList = BoardList.builder()
        .listId(listId)
        .title("테스트 리스트")
        .position(1)
        .boardId(boardId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    // when
    when(cardValidator.validate(command)).thenReturn(ValidationResult.valid(command));
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
    when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());

    Either<Failure, Card> result = getCardService.getCard(command);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("보드에 접근할 권한이 없습니다.");
  }
}