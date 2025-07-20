package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardQueryService 테스트")
class CardQueryServiceTest {

  @Mock
  private CardRepository cardRepository;

  @Mock
  private BoardListRepository boardListRepository;

  @Mock
  private BoardRepository boardRepository;

  @Mock
  private MessageSource messageSource;

  private CardQueryService cardQueryService;

  private CardId cardId;
  private ListId listId;
  private UserId userId;
  private BoardId boardId;

  @BeforeEach
  void setUp() {
    LocaleContextHolder.setLocale(Locale.KOREAN);

    // MessageSource Mock 설정
    lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
        .thenAnswer(invocation -> {
          String key = invocation.getArgument(0);
          return switch (key) {
            case "validation.cardId.required" -> "카드 ID는 필수 항목입니다";
            case "validation.userId.required" -> "사용자 ID는 필수 항목입니다";
            case "validation.listId.required" -> "리스트 ID는 필수 항목입니다";
            default -> key;
          };
        });

    ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    cardQueryService = new CardQueryService(cardRepository, boardListRepository, boardRepository,
        commonValidationRules);

    // 테스트 데이터 초기화
    cardId = new CardId("card-123");
    listId = new ListId("list-123");
    userId = new UserId("user-123");
    boardId = new BoardId("board-123");
  }

  // ==================== getCard 테스트 ====================

  @Test
  @DisplayName("유효한 카드 조회 요청은 성공해야 한다")
  void getCard_ValidRequest_ShouldSucceed() {
    // given
    Card card = createTestCard();
    BoardList boardList = createTestBoardList();
    Board board = createTestBoard();

    when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
    when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));

    // when
    Either<Failure, Card> result = cardQueryService.getCard(cardId, userId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isEqualTo(card);
  }

  @Test
  @DisplayName("null cardId로 카드 조회 시 실패해야 한다")
  void getCard_NullCardId_ShouldFail() {
    // when
    Either<Failure, Card> result = cardQueryService.getCard(null, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
  }

  @Test
  @DisplayName("null userId로 카드 조회 시 실패해야 한다")
  void getCard_NullUserId_ShouldFail() {
    // when
    Either<Failure, Card> result = cardQueryService.getCard(cardId, null);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
  }

  @Test
  @DisplayName("존재하지 않는 카드 조회 시 실패해야 한다")
  void getCard_NonExistentCard_ShouldFail() {
    // given
    when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

    // when
    Either<Failure, Card> result = cardQueryService.getCard(cardId, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("NOT_FOUND_CARD");
  }

  @Test
  @DisplayName("존재하지 않는 리스트의 카드 조회 시 실패해야 한다")
  void getCard_NonExistentList_ShouldFail() {
    // given
    Card card = createTestCard();
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
    when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

    // when
    Either<Failure, Card> result = cardQueryService.getCard(cardId, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("NOT_FOUND_LIST");
  }

  @Test
  @DisplayName("보드 접근 권한이 없는 경우 실패해야 한다")
  void getCard_NoBoardAccess_ShouldFail() {
    // given
    Card card = createTestCard();
    BoardList boardList = createTestBoardList();

    when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
    when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());

    // when
    Either<Failure, Card> result = cardQueryService.getCard(cardId, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("FORBIDDEN_BOARD");
  }

  // ==================== getCardsByListId 테스트 ====================

  @Test
  @DisplayName("유효한 리스트별 카드 조회 요청은 성공해야 한다")
  void getCardsByListId_ValidRequest_ShouldSucceed() {
    // given
    List<Card> cards = Arrays.asList(createTestCard(), createTestCard2());
    BoardList boardList = createTestBoardList();
    Board board = createTestBoard();

    when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
    when(cardRepository.findByListIdOrderByPosition(listId)).thenReturn(cards);

    // when
    Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(listId, userId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).hasSize(2);
    assertThat(result.get()).isEqualTo(cards);
  }

  @Test
  @DisplayName("null listId로 리스트별 카드 조회 시 실패해야 한다")
  void getCardsByListId_NullListId_ShouldFail() {
    // when
    Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(null, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
  }

  @Test
  @DisplayName("존재하지 않는 리스트 조회 시 실패해야 한다")
  void getCardsByListId_NonExistentList_ShouldFail() {
    // given
    when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

    // when
    Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(listId, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("NOT_FOUND_LIST");
  }

  @Test
  @DisplayName("보드 접근 권한이 없는 경우 리스트별 카드 조회 시 실패해야 한다")
  void getCardsByListId_NoBoardAccess_ShouldFail() {
    // given
    BoardList boardList = createTestBoardList();

    when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());

    // when
    Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(listId, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("FORBIDDEN_BOARD");
  }

  // ==================== searchCards 테스트 ====================

  @Test
  @DisplayName("유효한 카드 검색 요청은 성공해야 한다")
  void searchCards_ValidRequest_ShouldSucceed() {
    // given
    String searchTerm = "테스트";
    List<Card> cards = Arrays.asList(createTestCard());
    BoardList boardList = createTestBoardList();
    Board board = createTestBoard();

    when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
    when(cardRepository.findByListIdAndTitleContaining(listId, searchTerm)).thenReturn(cards);

    // when
    Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).hasSize(1);
    assertThat(result.get()).isEqualTo(cards);
  }

  @Test
  @DisplayName("null 검색어로 검색 시 실패해야 한다")
  void searchCards_NullSearchTerm_ShouldFail() {
    // when
    Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, null, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("SEARCH_TERM_EMPTY");
  }

  @Test
  @DisplayName("빈 검색어로 검색 시 실패해야 한다")
  void searchCards_EmptySearchTerm_ShouldFail() {
    // when
    Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, "", userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("SEARCH_TERM_EMPTY");
  }

  @Test
  @DisplayName("공백만 있는 검색어로 검색 시 실패해야 한다")
  void searchCards_BlankSearchTerm_ShouldFail() {
    // when
    Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, "   ", userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("SEARCH_TERM_EMPTY");
  }

  @Test
  @DisplayName("존재하지 않는 리스트에서 검색 시 실패해야 한다")
  void searchCards_NonExistentList_ShouldFail() {
    // given
    String searchTerm = "테스트";
    when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

    // when
    Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("NOT_FOUND_LIST");
  }

  @Test
  @DisplayName("보드 접근 권한이 없는 경우 검색 시 실패해야 한다")
  void searchCards_NoBoardAccess_ShouldFail() {
    // given
    String searchTerm = "테스트";
    BoardList boardList = createTestBoardList();

    when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
    when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());

    // when
    Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().message()).isEqualTo("FORBIDDEN_BOARD");
  }

  // ==================== 헬퍼 메서드 ====================

  private Card createTestCard() {
    return Card.builder()
        .cardId(cardId)
        .title("테스트 카드")
        .description("테스트 설명")
        .position(1)
        .listId(listId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  private Card createTestCard2() {
    return Card.builder()
        .cardId(new CardId("card-456"))
        .title("테스트 카드 2")
        .description("테스트 설명 2")
        .position(2)
        .listId(listId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  private BoardList createTestBoardList() {
    return BoardList.builder()
        .listId(listId)
        .title("테스트 리스트")
        .position(1)
        .boardId(boardId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  private Board createTestBoard() {
    return Board.builder()
        .boardId(boardId)
        .title("테스트 보드")
        .description("테스트 보드 설명")
        .ownerId(userId)
        .isArchived(false)
        .isStarred(false)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }
}