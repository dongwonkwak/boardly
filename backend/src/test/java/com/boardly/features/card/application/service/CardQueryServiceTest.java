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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private CommonValidationRules commonValidationRules;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @InjectMocks
    private CardQueryService cardQueryService;

    private UserId testUserId;
    private CardId testCardId;
    private ListId testListId;
    private BoardId testBoardId;
    private Card testCard;
    private BoardList testBoardList;
    private Board testBoard;

    @BeforeEach
    void setUp() {
        testUserId = new UserId("test-user-123");
        testCardId = new CardId("test-card-123");
        testListId = new ListId("test-list-123");
        testBoardId = new BoardId("test-board-123");

        Instant now = Instant.now();

        testCard = Card.builder()
                .cardId(testCardId)
                .title("테스트 카드")
                .description("테스트 카드 설명")
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
    }

    @Nested
    @DisplayName("getCard 메서드 테스트")
    class GetCardTest {

        @Test
        @DisplayName("카드 조회 성공")
        void getCard_Success() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> cardIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.cardIdRequired(any())).thenReturn(cardIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Card> result = cardQueryService.getCard(testCardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            Card card = result.get();
            assertThat(card).isEqualTo(testCard);
            assertThat(card.getCardId()).isEqualTo(testCardId);
            assertThat(card.getTitle()).isEqualTo("테스트 카드");

            verify(cardRepository).findById(testCardId);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
        }

        @Test
        @DisplayName("카드 ID가 null인 경우 - 검증 실패")
        void getCard_NullCardId_ValidationFailure() {
            // given
            ValidationResult<Object> invalidResult = ValidationResult.invalid("cardId", "카드 ID는 필수입니다", null);
            Validator<Object> cardIdValidator = cmd -> invalidResult;
            Validator<Object> userIdValidator = cmd -> ValidationResult.valid(new Object());
            when(commonValidationRules.cardIdRequired(any())).thenReturn(cardIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(validationMessageResolver.getMessage("validation.input.invalid"))
                    .thenReturn("입력 데이터가 올바르지 않습니다");

            // when
            Either<Failure, Card> result = cardQueryService.getCard(null, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);

            Failure.InputError inputError = (Failure.InputError) failure;
            assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");

            verify(cardRepository, never()).findById(any());
        }

        @Test
        @DisplayName("사용자 ID가 null인 경우 - 검증 실패")
        void getCard_NullUserId_ValidationFailure() {
            // given
            ValidationResult<Object> invalidResult = ValidationResult.invalid("userId", "사용자 ID는 필수입니다", null);
            Validator<Object> cardIdValidator = cmd -> ValidationResult.valid(new Object());
            Validator<Object> userIdValidator = cmd -> invalidResult;
            when(commonValidationRules.cardIdRequired(any())).thenReturn(cardIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(validationMessageResolver.getMessage("validation.input.invalid"))
                    .thenReturn("입력 데이터가 올바르지 않습니다");

            // when
            Either<Failure, Card> result = cardQueryService.getCard(testCardId, null);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);

            verify(cardRepository, never()).findById(any());
        }

        @Test
        @DisplayName("카드를 찾을 수 없는 경우 - NotFound 실패")
        void getCard_CardNotFound_NotFoundFailure() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> cardIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.cardIdRequired(any())).thenReturn(cardIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.not_found"))
                    .thenReturn("이동할 카드를 찾을 수 없습니다");

            // when
            Either<Failure, Card> result = cardQueryService.getCard(testCardId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);

            Failure.NotFound notFound = (Failure.NotFound) failure;
            assertThat(notFound.getMessage()).isEqualTo("이동할 카드를 찾을 수 없습니다");

            verify(cardRepository).findById(testCardId);
            verify(boardListRepository, never()).findById(any());
        }

        @Test
        @DisplayName("리스트를 찾을 수 없는 경우 - NotFound 실패")
        void getCard_ListNotFound_NotFoundFailure() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> cardIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.cardIdRequired(any())).thenReturn(cardIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.list_not_found"))
                    .thenReturn("리스트를 찾을 수 없습니다");

            // when
            Either<Failure, Card> result = cardQueryService.getCard(testCardId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);

            Failure.NotFound notFound = (Failure.NotFound) failure;
            assertThat(notFound.getMessage()).isEqualTo("리스트를 찾을 수 없습니다");

            verify(cardRepository).findById(testCardId);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository, never()).findByIdAndOwnerId(any(), any());
        }

        @Test
        @DisplayName("보드 접근 권한이 없는 경우 - PermissionDenied 실패")
        void getCard_AccessDenied_PermissionDeniedFailure() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> cardIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.cardIdRequired(any())).thenReturn(cardIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.access_denied"))
                    .thenReturn("보드에 접근할 권한이 없습니다");

            // when
            Either<Failure, Card> result = cardQueryService.getCard(testCardId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);

            Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) failure;
            assertThat(permissionDenied.getMessage()).isEqualTo("보드에 접근할 권한이 없습니다");

            verify(cardRepository).findById(testCardId);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
        }
    }

    @Nested
    @DisplayName("getCardsByListId 메서드 테스트")
    class GetCardsByListIdTest {

        @Test
        @DisplayName("리스트별 카드 조회 성공")
        void getCardsByListId_Success() {
            // given
            List<Card> cards = List.of(testCard);
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.findByListIdOrderByPosition(testListId)).thenReturn(cards);

            // when
            Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(testListId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            List<Card> resultCards = result.get();
            assertThat(resultCards).hasSize(1);
            assertThat(resultCards.get(0)).isEqualTo(testCard);

            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardRepository).findByListIdOrderByPosition(testListId);
        }

        @Test
        @DisplayName("빈 카드 목록 조회 성공")
        void getCardsByListId_EmptyList_Success() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.findByListIdOrderByPosition(testListId)).thenReturn(List.of());

            // when
            Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(testListId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            List<Card> resultCards = result.get();
            assertThat(resultCards).isEmpty();

            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardRepository).findByListIdOrderByPosition(testListId);
        }

        @Test
        @DisplayName("리스트 ID가 null인 경우 - 검증 실패")
        void getCardsByListId_NullListId_ValidationFailure() {
            // given
            ValidationResult<Object> invalidResult = ValidationResult.invalid("listId", "리스트 ID는 필수입니다", null);
            Validator<Object> listIdValidator = cmd -> invalidResult;
            Validator<Object> userIdValidator = cmd -> ValidationResult.valid(new Object());
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(validationMessageResolver.getMessage("validation.input.invalid"))
                    .thenReturn("입력 데이터가 올바르지 않습니다");

            // when
            Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(null, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);

            verify(boardListRepository, never()).findById(any());
        }

        @Test
        @DisplayName("리스트를 찾을 수 없는 경우 - NotFound 실패")
        void getCardsByListId_ListNotFound_NotFoundFailure() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.list_not_found"))
                    .thenReturn("리스트를 찾을 수 없습니다");

            // when
            Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(testListId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);

            Failure.NotFound notFound = (Failure.NotFound) failure;
            assertThat(notFound.getMessage()).isEqualTo("리스트를 찾을 수 없습니다");

            verify(boardListRepository).findById(testListId);
            verify(boardRepository, never()).findByIdAndOwnerId(any(), any());
        }

        @Test
        @DisplayName("보드 접근 권한이 없는 경우 - PermissionDenied 실패")
        void getCardsByListId_AccessDenied_PermissionDeniedFailure() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.access_denied"))
                    .thenReturn("보드에 접근할 권한이 없습니다");

            // when
            Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(testListId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);

            Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) failure;
            assertThat(permissionDenied.getMessage()).isEqualTo("보드에 접근할 권한이 없습니다");

            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardRepository, never()).findByListIdOrderByPosition(any());
        }
    }

    @Nested
    @DisplayName("searchCards 메서드 테스트")
    class SearchCardsTest {

        @Test
        @DisplayName("카드 검색 성공")
        void searchCards_Success() {
            // given
            String searchTerm = "테스트";
            List<Card> cards = List.of(testCard);
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.findByListIdAndTitleContaining(testListId, searchTerm)).thenReturn(cards);

            // when
            Either<Failure, List<Card>> result = cardQueryService.searchCards(testListId, searchTerm, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            List<Card> resultCards = result.get();
            assertThat(resultCards).hasSize(1);
            assertThat(resultCards.get(0)).isEqualTo(testCard);

            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardRepository).findByListIdAndTitleContaining(testListId, searchTerm);
        }

        @Test
        @DisplayName("검색어가 null인 경우 - 검증 실패")
        void searchCards_NullSearchTerm_ValidationFailure() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(validationMessageResolver.getMessage("validation.search.term.required"))
                    .thenReturn("검색어를 입력해주세요");

            // when
            Either<Failure, List<Card>> result = cardQueryService.searchCards(testListId, null, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);

            Failure.InputError inputError = (Failure.InputError) failure;
            assertThat(inputError.getMessage()).isEqualTo("검색어를 입력해주세요");
            assertThat(inputError.getErrorCode()).isEqualTo("SEARCH_TERM_EMPTY");

            verify(boardListRepository, never()).findById(any());
        }

        @Test
        @DisplayName("검색어가 빈 문자열인 경우 - 검증 실패")
        void searchCards_EmptySearchTerm_ValidationFailure() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(validationMessageResolver.getMessage("validation.search.term.required"))
                    .thenReturn("검색어를 입력해주세요");

            // when
            Either<Failure, List<Card>> result = cardQueryService.searchCards(testListId, "", testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);

            Failure.InputError inputError = (Failure.InputError) failure;
            assertThat(inputError.getMessage()).isEqualTo("검색어를 입력해주세요");
            assertThat(inputError.getErrorCode()).isEqualTo("SEARCH_TERM_EMPTY");

            verify(boardListRepository, never()).findById(any());
        }

        @Test
        @DisplayName("검색어가 공백만 있는 경우 - 검증 실패")
        void searchCards_WhitespaceSearchTerm_ValidationFailure() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(validationMessageResolver.getMessage("validation.search.term.required"))
                    .thenReturn("검색어를 입력해주세요");

            // when
            Either<Failure, List<Card>> result = cardQueryService.searchCards(testListId, "   ", testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);

            Failure.InputError inputError = (Failure.InputError) failure;
            assertThat(inputError.getMessage()).isEqualTo("검색어를 입력해주세요");
            assertThat(inputError.getErrorCode()).isEqualTo("SEARCH_TERM_EMPTY");

            verify(boardListRepository, never()).findById(any());
        }

        @Test
        @DisplayName("검색 결과가 없는 경우 - 빈 목록 반환")
        void searchCards_NoResults_EmptyList() {
            // given
            String searchTerm = "존재하지않는카드";
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.findByListIdAndTitleContaining(testListId, searchTerm)).thenReturn(List.of());

            // when
            Either<Failure, List<Card>> result = cardQueryService.searchCards(testListId, searchTerm, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            List<Card> resultCards = result.get();
            assertThat(resultCards).isEmpty();

            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardRepository).findByListIdAndTitleContaining(testListId, searchTerm);
        }

        @Test
        @DisplayName("검색어 앞뒤 공백 제거 확인")
        void searchCards_SearchTermTrimmed() {
            // given
            String searchTerm = "  테스트  ";
            String trimmedSearchTerm = "테스트";
            List<Card> cards = List.of(testCard);
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.findByListIdAndTitleContaining(testListId, trimmedSearchTerm)).thenReturn(cards);

            // when
            Either<Failure, List<Card>> result = cardQueryService.searchCards(testListId, searchTerm, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            List<Card> resultCards = result.get();
            assertThat(resultCards).hasSize(1);

            verify(cardRepository).findByListIdAndTitleContaining(testListId, trimmedSearchTerm);
        }
    }

    @Nested
    @DisplayName("공통 검증 메서드 테스트")
    class CommonValidationTest {

        @Test
        @DisplayName("validateCardQuery - 성공")
        void validateCardQuery_Success() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> cardIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.cardIdRequired(any())).thenReturn(cardIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);

            // when
            Either<Failure, Card> result = cardQueryService.getCard(testCardId, testUserId);

            // then
            // 검증이 성공하면 다음 단계로 진행되므로, 실제 검증은 위의 성공 테스트에서 확인
            verify(commonValidationRules).cardIdRequired(any());
            verify(commonValidationRules).userIdRequired(any());
        }

        @Test
        @DisplayName("validateListQuery - 성공")
        void validateListQuery_Success() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);

            // when
            Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(testListId, testUserId);

            // then
            // 검증이 성공하면 다음 단계로 진행되므로, 실제 검증은 위의 성공 테스트에서 확인
            verify(commonValidationRules).listIdRequired(any());
            verify(commonValidationRules).userIdRequired(any());
        }
    }

    @Nested
    @DisplayName("보드 접근 권한 검증 테스트")
    class BoardAccessValidationTest {

        @Test
        @DisplayName("validateBoardAccess - 성공")
        void validateBoardAccess_Success() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.findByListIdOrderByPosition(testListId)).thenReturn(List.of());

            // when
            Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(testListId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
        }

        @Test
        @DisplayName("validateBoardAccess - 리스트 없음")
        void validateBoardAccess_ListNotFound() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.list_not_found"))
                    .thenReturn("리스트를 찾을 수 없습니다");

            // when
            Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(testListId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);
            assertThat(failure.getMessage()).isEqualTo("리스트를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("validateBoardAccess - 보드 접근 권한 없음")
        void validateBoardAccess_AccessDenied() {
            // given
            ValidationResult<Object> validResult = ValidationResult.valid(new Object());
            Validator<Object> listIdValidator = cmd -> validResult;
            Validator<Object> userIdValidator = cmd -> validResult;
            when(commonValidationRules.listIdRequired(any())).thenReturn(listIdValidator);
            when(commonValidationRules.userIdRequired(any())).thenReturn(userIdValidator);
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.access_denied"))
                    .thenReturn("보드에 접근할 권한이 없습니다");

            // when
            Either<Failure, List<Card>> result = cardQueryService.getCardsByListId(testListId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(failure.getMessage()).isEqualTo("보드에 접근할 권한이 없습니다");
        }
    }
}