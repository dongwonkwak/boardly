package com.boardly.features.card.application.service;

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
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CardQueryServiceTest {

    private CardQueryService cardQueryService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CommonValidationRules commonValidationRules;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @BeforeEach
    void setUp() {
        cardQueryService = new CardQueryService(
                cardRepository,
                boardListRepository,
                boardRepository,
                commonValidationRules,
                validationMessageResolver);
    }

    // ==================== HELPER METHODS ====================

    private CardId createCardId() {
        return new CardId();
    }

    private ListId createListId() {
        return new ListId();
    }

    private BoardId createBoardId() {
        return new BoardId();
    }

    private UserId createUserId() {
        return new UserId();
    }

    private Card createValidCard(CardId cardId, ListId listId) {
        return Card.builder()
                .cardId(cardId)
                .title("테스트 카드")
                .description("테스트 카드 설명")
                .position(1)
                .listId(listId)
                .createdBy(new UserId("test-user"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private BoardList createValidBoardList(ListId listId, BoardId boardId) {
        return BoardList.builder()
                .listId(listId)
                .title("테스트 리스트")
                .description("테스트 리스트 설명")
                .position(1)
                .boardId(boardId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Board createValidBoard(BoardId boardId, UserId ownerId) {
        return Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .isArchived(false)
                .ownerId(ownerId)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private ValidationResult<Object> createValidValidationResult() {
        return ValidationResult.valid(new Object());
    }

    private ValidationResult<Object> createInvalidValidationResult() {
        return ValidationResult.invalid("field", "입력 검증 실패", null);
    }

    // ==================== getCard TESTS ====================

    @Test
    @DisplayName("유효한 정보로 카드 조회가 성공해야 한다")
    void getCard_withValidData_shouldReturnCard() {
        // given
        CardId cardId = createCardId();
        ListId listId = createListId();
        BoardId boardId = createBoardId();
        UserId userId = createUserId();

        Card card = createValidCard(cardId, listId);
        BoardList boardList = createValidBoardList(listId, boardId);
        Board board = createValidBoard(boardId, userId);

        when(commonValidationRules.cardIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));

        // when
        Either<Failure, Card> result = cardQueryService.getCard(cardId, userId);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(card);

        verify(cardRepository).findById(cardId);
        verify(boardListRepository).findById(listId);
        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
    }

    @Test
    @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
    void getCard_withInvalidData_shouldReturnInputError() {
        // given
        CardId cardId = createCardId();
        UserId userId = createUserId();

        when(commonValidationRules.cardIdRequired(any())).thenReturn(cmd -> createInvalidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(validationMessageResolver.getMessage("validation.input.invalid")).thenReturn("입력이 유효하지 않습니다");

        // when
        Either<Failure, Card> result = cardQueryService.getCard(cardId, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("INVALID_INPUT");
        assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다");

        verify(cardRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 카드 조회 시 NotFound 오류를 반환해야 한다")
    void getCard_withNonExistentCard_shouldReturnNotFoundFailure() {
        // given
        CardId cardId = createCardId();
        UserId userId = createUserId();

        when(commonValidationRules.cardIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("error.service.card.read.not_found")).thenReturn("카드를 찾을 수 없습니다");

        // when
        Either<Failure, Card> result = cardQueryService.getCard(cardId, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
        assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(result.getLeft().getMessage()).isEqualTo("카드를 찾을 수 없습니다");

        verify(cardRepository).findById(cardId);
        verify(boardListRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 리스트 조회 시 NotFound 오류를 반환해야 한다")
    void getCard_withNonExistentList_shouldReturnNotFoundFailure() {
        // given
        CardId cardId = createCardId();
        ListId listId = createListId();
        UserId userId = createUserId();

        Card card = createValidCard(cardId, listId);

        when(commonValidationRules.cardIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardListRepository.findById(listId)).thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("error.service.card.read.list_not_found"))
                .thenReturn("리스트를 찾을 수 없습니다");

        // when
        Either<Failure, Card> result = cardQueryService.getCard(cardId, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
        assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(result.getLeft().getMessage()).isEqualTo("리스트를 찾을 수 없습니다");

        verify(cardRepository).findById(cardId);
        verify(boardListRepository).findById(listId);
        verify(boardRepository, never()).findByIdAndOwnerId(any(), any());
    }

    @Test
    @DisplayName("권한이 없는 사용자가 카드 조회 시 PermissionDenied 오류를 반환해야 한다")
    void getCard_withoutPermission_shouldReturnPermissionDeniedFailure() {
        // given
        CardId cardId = createCardId();
        ListId listId = createListId();
        BoardId boardId = createBoardId();
        UserId userId = createUserId();

        Card card = createValidCard(cardId, listId);
        BoardList boardList = createValidBoardList(listId, boardId);

        when(commonValidationRules.cardIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("error.service.card.read.access_denied")).thenReturn("접근 권한이 없습니다");

        // when
        Either<Failure, Card> result = cardQueryService.getCard(cardId, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
        assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode()).isEqualTo("PERMISSION_DENIED");
        assertThat(result.getLeft().getMessage()).isEqualTo("접근 권한이 없습니다");

        verify(cardRepository).findById(cardId);
        verify(boardListRepository).findById(listId);
        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
    }

    // ==================== getCardsByListId TESTS ====================

    @Test
    @DisplayName("유효한 정보로 리스트별 카드 조회가 성공해야 한다")
    void getCardsByListId_withValidData_shouldReturnCards() {
        // given
        ListId listId = createListId();
        BoardId boardId = createBoardId();
        UserId userId = createUserId();

        BoardList boardList = createValidBoardList(listId, boardId);
        Board board = createValidBoard(boardId, userId);
        List<Card> cards = Arrays.asList(
                createValidCard(createCardId(), listId),
                createValidCard(createCardId(), listId));

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
        when(cardRepository.findByListIdOrderByPosition(listId)).thenReturn(cards);

        // when
        Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(listId, userId);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(cards);

        verify(boardListRepository).findById(listId);
        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
        verify(cardRepository).findByListIdOrderByPosition(listId);
    }

    @Test
    @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
    void getCardsByListId_withInvalidData_shouldReturnInputError() {
        // given
        ListId listId = createListId();
        UserId userId = createUserId();

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createInvalidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(validationMessageResolver.getMessage("validation.input.invalid")).thenReturn("입력이 유효하지 않습니다");

        // when
        Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(listId, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("INVALID_INPUT");
        assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다");

        verify(boardListRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 리스트 조회 시 NotFound 오류를 반환해야 한다")
    void getCardsByListId_withNonExistentList_shouldReturnNotFoundFailure() {
        // given
        ListId listId = createListId();
        UserId userId = createUserId();

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(boardListRepository.findById(listId)).thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("error.service.card.read.list_not_found"))
                .thenReturn("리스트를 찾을 수 없습니다");

        // when
        Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(listId, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
        assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(result.getLeft().getMessage()).isEqualTo("리스트를 찾을 수 없습니다");

        verify(boardListRepository).findById(listId);
        verify(boardRepository, never()).findByIdAndOwnerId(any(), any());
    }

    @Test
    @DisplayName("권한이 없는 사용자가 리스트별 카드 조회 시 PermissionDenied 오류를 반환해야 한다")
    void getCardsByListId_withoutPermission_shouldReturnPermissionDeniedFailure() {
        // given
        ListId listId = createListId();
        BoardId boardId = createBoardId();
        UserId userId = createUserId();

        BoardList boardList = createValidBoardList(listId, boardId);

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("error.service.card.read.access_denied")).thenReturn("접근 권한이 없습니다");

        // when
        Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(listId, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
        assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode()).isEqualTo("PERMISSION_DENIED");
        assertThat(result.getLeft().getMessage()).isEqualTo("접근 권한이 없습니다");

        verify(boardListRepository).findById(listId);
        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
        verify(cardRepository, never()).findByListIdOrderByPosition(any());
    }

    // ==================== searchCards TESTS ====================

    @Test
    @DisplayName("유효한 정보로 카드 검색이 성공해야 한다")
    void searchCards_withValidData_shouldReturnCards() {
        // given
        ListId listId = createListId();
        BoardId boardId = createBoardId();
        UserId userId = createUserId();
        String searchTerm = "테스트";

        BoardList boardList = createValidBoardList(listId, boardId);
        Board board = createValidBoard(boardId, userId);
        List<Card> cards = Arrays.asList(
                createValidCard(createCardId(), listId));

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
        when(cardRepository.findByListIdAndTitleContaining(listId, searchTerm)).thenReturn(cards);

        // when
        Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(cards);

        verify(boardListRepository).findById(listId);
        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
        verify(cardRepository).findByListIdAndTitleContaining(listId, searchTerm);
    }

    @Test
    @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
    void searchCards_withInvalidData_shouldReturnInputError() {
        // given
        ListId listId = createListId();
        UserId userId = createUserId();
        String searchTerm = "테스트";

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createInvalidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(validationMessageResolver.getMessage("validation.input.invalid")).thenReturn("입력이 유효하지 않습니다");

        // when
        Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("INVALID_INPUT");
        assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다");

        verify(boardListRepository, never()).findById(any());
    }

    @Test
    @DisplayName("빈 검색어로 검색 시 InputError를 반환해야 한다")
    void searchCards_withEmptySearchTerm_shouldReturnInputError() {
        // given
        ListId listId = createListId();
        UserId userId = createUserId();
        String searchTerm = "";

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(validationMessageResolver.getMessage("validation.search.term.required")).thenReturn("검색어를 입력해주세요");

        // when
        Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("SEARCH_TERM_EMPTY");
        assertThat(result.getLeft().getMessage()).isEqualTo("검색어를 입력해주세요");

        verify(boardListRepository, never()).findById(any());
    }

    @Test
    @DisplayName("null 검색어로 검색 시 InputError를 반환해야 한다")
    void searchCards_withNullSearchTerm_shouldReturnInputError() {
        // given
        ListId listId = createListId();
        UserId userId = createUserId();
        String searchTerm = null;

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(validationMessageResolver.getMessage("validation.search.term.required")).thenReturn("검색어를 입력해주세요");

        // when
        Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("SEARCH_TERM_EMPTY");
        assertThat(result.getLeft().getMessage()).isEqualTo("검색어를 입력해주세요");

        verify(boardListRepository, never()).findById(any());
    }

    @Test
    @DisplayName("공백만 있는 검색어로 검색 시 InputError를 반환해야 한다")
    void searchCards_withWhitespaceOnlySearchTerm_shouldReturnInputError() {
        // given
        ListId listId = createListId();
        UserId userId = createUserId();
        String searchTerm = "   ";

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(validationMessageResolver.getMessage("validation.search.term.required")).thenReturn("검색어를 입력해주세요");

        // when
        Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("SEARCH_TERM_EMPTY");
        assertThat(result.getLeft().getMessage()).isEqualTo("검색어를 입력해주세요");

        verify(boardListRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 리스트로 검색 시 NotFound 오류를 반환해야 한다")
    void searchCards_withNonExistentList_shouldReturnNotFoundFailure() {
        // given
        ListId listId = createListId();
        UserId userId = createUserId();
        String searchTerm = "테스트";

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(boardListRepository.findById(listId)).thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("error.service.card.read.list_not_found"))
                .thenReturn("리스트를 찾을 수 없습니다");

        // when
        Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
        assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(result.getLeft().getMessage()).isEqualTo("리스트를 찾을 수 없습니다");

        verify(boardListRepository).findById(listId);
        verify(boardRepository, never()).findByIdAndOwnerId(any(), any());
    }

    @Test
    @DisplayName("권한이 없는 사용자가 카드 검색 시 PermissionDenied 오류를 반환해야 한다")
    void searchCards_withoutPermission_shouldReturnPermissionDeniedFailure() {
        // given
        ListId listId = createListId();
        BoardId boardId = createBoardId();
        UserId userId = createUserId();
        String searchTerm = "테스트";

        BoardList boardList = createValidBoardList(listId, boardId);

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("error.service.card.read.access_denied")).thenReturn("접근 권한이 없습니다");

        // when
        Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
        assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode()).isEqualTo("PERMISSION_DENIED");
        assertThat(result.getLeft().getMessage()).isEqualTo("접근 권한이 없습니다");

        verify(boardListRepository).findById(listId);
        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
        verify(cardRepository, never()).findByListIdAndTitleContaining(any(), any());
    }

    @Test
    @DisplayName("검색어 앞뒤 공백이 제거되어 검색되어야 한다")
    void searchCards_withTrimmedSearchTerm_shouldSearchWithTrimmedTerm() {
        // given
        ListId listId = createListId();
        BoardId boardId = createBoardId();
        UserId userId = createUserId();
        String searchTerm = "  테스트  ";
        String trimmedSearchTerm = "테스트";

        BoardList boardList = createValidBoardList(listId, boardId);
        Board board = createValidBoard(boardId, userId);
        List<Card> cards = Arrays.asList(
                createValidCard(createCardId(), listId));

        when(commonValidationRules.listIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(commonValidationRules.userIdRequired(any())).thenReturn(cmd -> createValidValidationResult());
        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
        when(cardRepository.findByListIdAndTitleContaining(listId, trimmedSearchTerm)).thenReturn(cards);

        // when
        Either<Failure, List<Card>> result = cardQueryService.searchCards(listId, searchTerm, userId);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(cards);

        verify(cardRepository).findByListIdAndTitleContaining(listId, trimmedSearchTerm);
    }
}