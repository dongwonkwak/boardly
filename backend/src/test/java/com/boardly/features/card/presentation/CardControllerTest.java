package com.boardly.features.card.presentation;

import com.boardly.features.card.application.port.input.*;
import com.boardly.features.card.application.usecase.*;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.presentation.request.*;
import com.boardly.features.card.presentation.response.CardResponse;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.response.ErrorResponse;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CreateCardUseCase createCardUseCase;

    @Mock
    private CardQueryUseCase cardQueryUseCase;

    @Mock
    private UpdateCardUseCase updateCardUseCase;

    @Mock
    private MoveCardUseCase moveCardUseCase;

    @Mock
    private CloneCardUseCase cloneCardUseCase;

    @Mock
    private DeleteCardUseCase deleteCardUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    private CardController controller;

    @BeforeEach
    void setUp() {
        controller = new CardController(
                createCardUseCase,
                cardQueryUseCase,
                updateCardUseCase,
                moveCardUseCase,
                cloneCardUseCase,
                deleteCardUseCase,
                failureHandler);
    }

    private Card createSampleCard(String cardId, String title, String description, String listId, int position) {
        return Card.builder()
                .cardId(new CardId(cardId))
                .title(title)
                .description(description)
                .listId(new ListId(listId))
                .position(position)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("카드 생성 성공")
    void createCard_Success() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        CreateCardRequest request = new CreateCardRequest("새 카드", "카드 설명", listId);
        Card createdCard = createSampleCard("card-new", "새 카드", "카드 설명", listId, 1);

        when(jwt.getSubject()).thenReturn(userId);
        when(createCardUseCase.createCard(any(CreateCardCommand.class)))
                .thenReturn(Either.right(createdCard));

        // when
        ResponseEntity<?> response = controller.createCard(request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.cardId()).isEqualTo("card-new");
        assertThat(responseBody.title()).isEqualTo("새 카드");
        assertThat(responseBody.description()).isEqualTo("카드 설명");
        assertThat(responseBody.listId()).isEqualTo(listId);
        assertThat(responseBody.position()).isEqualTo(1);
    }

    @Test
    @DisplayName("카드 생성 실패 - 검증 오류")
    void createCard_ValidationError() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        CreateCardRequest request = new CreateCardRequest("", "카드 설명", listId); // 빈 제목
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("title")
                .message("카드 제목은 필수입니다")
                .rejectedValue("")
                .build();
        Failure validationFailure = Failure.ofValidation("INVALID_INPUT", List.of(violation));
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(createCardUseCase.createCard(any(CreateCardCommand.class)))
                .thenReturn(Either.left(validationFailure));
        when(failureHandler.handleFailure(validationFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.createCard(request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("카드 생성 실패 - 리스트 없음")
    void createCard_ListNotFound() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        CreateCardRequest request = new CreateCardRequest("새 카드", "카드 설명", listId);
        Failure notFoundFailure = Failure.ofNotFound("LIST_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(createCardUseCase.createCard(any(CreateCardCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.createCard(request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("카드 조회 성공")
    void getCard_Success() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        Card card = createSampleCard(cardId, "테스트 카드", "카드 설명", "list-123", 1);

        when(jwt.getSubject()).thenReturn(userId);
        when(cardQueryUseCase.getCard(any(CardId.class), any(UserId.class)))
                .thenReturn(Either.right(card));

        // when
        ResponseEntity<?> response = controller.getCard(cardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.cardId()).isEqualTo(cardId);
        assertThat(responseBody.title()).isEqualTo("테스트 카드");
        assertThat(responseBody.description()).isEqualTo("카드 설명");
    }

    @Test
    @DisplayName("카드 조회 실패 - 카드 없음")
    void getCard_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        Failure notFoundFailure = Failure.ofNotFound("CARD_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(cardQueryUseCase.getCard(any(CardId.class), any(UserId.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.getCard(cardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("리스트별 카드 목록 조회 성공")
    void getCardsByListId_Success() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        List<Card> cards = List.of(
                createSampleCard("card-1", "카드 1", "설명 1", listId, 1),
                createSampleCard("card-2", "카드 2", "설명 2", listId, 2));

        when(jwt.getSubject()).thenReturn(userId);
        when(cardQueryUseCase.getCardsByListId(any(ListId.class), any(UserId.class)))
                .thenReturn(Either.right(cards));

        // when
        ResponseEntity<?> response = controller.getCardsByListId(listId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CardResponse> responses = (List<CardResponse>) response.getBody();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).cardId()).isEqualTo("card-1");
        assertThat(responses.get(0).title()).isEqualTo("카드 1");
        assertThat(responses.get(0).position()).isEqualTo(1);
        assertThat(responses.get(1).cardId()).isEqualTo("card-2");
        assertThat(responses.get(1).title()).isEqualTo("카드 2");
        assertThat(responses.get(1).position()).isEqualTo(2);
    }

    @Test
    @DisplayName("리스트별 카드 목록 조회 성공 - 빈 목록")
    void getCardsByListId_EmptyList() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";

        when(jwt.getSubject()).thenReturn(userId);
        when(cardQueryUseCase.getCardsByListId(any(ListId.class), any(UserId.class)))
                .thenReturn(Either.right(List.of()));

        // when
        ResponseEntity<?> response = controller.getCardsByListId(listId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CardResponse> responses = (List<CardResponse>) response.getBody();
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("리스트별 카드 목록 조회 실패 - 리스트 없음")
    void getCardsByListId_ListNotFound() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        Failure notFoundFailure = Failure.ofNotFound("LIST_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(cardQueryUseCase.getCardsByListId(any(ListId.class), any(UserId.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.getCardsByListId(listId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("카드 수정 성공")
    void updateCard_Success() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        UpdateCardRequest request = new UpdateCardRequest("수정된 카드", "수정된 설명");
        Card updatedCard = createSampleCard(cardId, "수정된 카드", "수정된 설명", "list-123", 1);

        when(jwt.getSubject()).thenReturn(userId);
        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                .thenReturn(Either.right(updatedCard));

        // when
        ResponseEntity<?> response = controller.updateCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.cardId()).isEqualTo(cardId);
        assertThat(responseBody.title()).isEqualTo("수정된 카드");
        assertThat(responseBody.description()).isEqualTo("수정된 설명");
    }

    @Test
    @DisplayName("카드 수정 실패 - 카드 없음")
    void updateCard_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        UpdateCardRequest request = new UpdateCardRequest("수정된 카드", "수정된 설명");
        Failure notFoundFailure = Failure.ofNotFound("CARD_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("카드 수정 실패 - 아카이브된 보드")
    void updateCard_ArchivedBoard() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        UpdateCardRequest request = new UpdateCardRequest("수정된 카드", "수정된 설명");
        Failure conflictFailure = Failure.ofConflict("ARCHIVED_BOARD_CARD_UPDATE_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("카드 이동 성공 - 같은 리스트 내 이동")
    void moveCard_Success_SameList() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        String listId = "list-123";
        MoveCardRequest request = new MoveCardRequest(null, 3); // 같은 리스트 내에서 위치만 변경
        Card movedCard = createSampleCard(cardId, "이동된 카드", "설명", listId, 3);

        when(jwt.getSubject()).thenReturn(userId);
        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                .thenReturn(Either.right(movedCard));

        // when
        ResponseEntity<?> response = controller.moveCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.cardId()).isEqualTo(cardId);
        assertThat(responseBody.position()).isEqualTo(3);
    }

    @Test
    @DisplayName("카드 이동 성공 - 다른 리스트로 이동")
    void moveCard_Success_DifferentList() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        String targetListId = "list-456";
        MoveCardRequest request = new MoveCardRequest(targetListId, 1);
        Card movedCard = createSampleCard(cardId, "이동된 카드", "설명", targetListId, 1);

        when(jwt.getSubject()).thenReturn(userId);
        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                .thenReturn(Either.right(movedCard));

        // when
        ResponseEntity<?> response = controller.moveCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.cardId()).isEqualTo(cardId);
        assertThat(responseBody.listId()).isEqualTo(targetListId);
        assertThat(responseBody.position()).isEqualTo(1);
    }

    @Test
    @DisplayName("카드 이동 실패 - 카드 없음")
    void moveCard_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        MoveCardRequest request = new MoveCardRequest("list-456", 1);
        Failure notFoundFailure = Failure.ofNotFound("CARD_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.moveCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("카드 이동 실패 - 아카이브된 보드")
    void moveCard_ArchivedBoard() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        MoveCardRequest request = new MoveCardRequest("list-456", 1);
        Failure conflictFailure = Failure.ofConflict("ARCHIVED_BOARD_CARD_MOVE_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.moveCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("카드 복제 성공")
    void cloneCard_Success() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        String targetListId = "list-456";
        CloneCardRequest request = new CloneCardRequest("복제된 카드", targetListId);
        Card clonedCard = createSampleCard("card-cloned", "복제된 카드", "카드 설명", targetListId, 1);

        when(jwt.getSubject()).thenReturn(userId);
        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                .thenReturn(Either.right(clonedCard));

        // when
        ResponseEntity<?> response = controller.cloneCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.cardId()).isEqualTo("card-cloned");
        assertThat(responseBody.title()).isEqualTo("복제된 카드");
        assertThat(responseBody.listId()).isEqualTo(targetListId);
    }

    @Test
    @DisplayName("카드 복제 성공 - 같은 리스트에 복제")
    void cloneCard_Success_SameList() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        CloneCardRequest request = new CloneCardRequest("복제된 카드", null); // 같은 리스트에 복제
        Card clonedCard = createSampleCard("card-cloned", "복제된 카드", "카드 설명", "list-123", 2);

        when(jwt.getSubject()).thenReturn(userId);
        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                .thenReturn(Either.right(clonedCard));

        // when
        ResponseEntity<?> response = controller.cloneCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.cardId()).isEqualTo("card-cloned");
        assertThat(responseBody.title()).isEqualTo("복제된 카드");
    }

    @Test
    @DisplayName("카드 복제 실패 - 카드 없음")
    void cloneCard_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        CloneCardRequest request = new CloneCardRequest("복제된 카드", "list-456");
        Failure notFoundFailure = Failure.ofNotFound("CARD_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.cloneCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("카드 복제 실패 - 리스트 카드 개수 제한 초과")
    void cloneCard_ListLimitExceeded() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        CloneCardRequest request = new CloneCardRequest("복제된 카드", "list-456");
        Failure conflictFailure = Failure.ofConflict("LIST_CARD_LIMIT_EXCEEDED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.cloneCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("카드 삭제 성공")
    void deleteCard_Success() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteCardUseCase.deleteCard(any(DeleteCardCommand.class)))
                .thenReturn(Either.right(null));

        // when
        ResponseEntity<?> response = controller.deleteCard(cardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("카드 삭제 실패 - 카드 없음")
    void deleteCard_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        Failure notFoundFailure = Failure.ofNotFound("CARD_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteCardUseCase.deleteCard(any(DeleteCardCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.deleteCard(cardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("카드 삭제 실패 - 아카이브된 보드")
    void deleteCard_ArchivedBoard() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        Failure conflictFailure = Failure.ofConflict("ARCHIVED_BOARD_CARD_DELETE_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteCardUseCase.deleteCard(any(DeleteCardCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.deleteCard(cardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("카드 검색 성공")
    void searchCards_Success() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        String searchTerm = "테스트";
        List<Card> cards = List.of(
                createSampleCard("card-1", "테스트 카드 1", "설명 1", listId, 1),
                createSampleCard("card-2", "테스트 카드 2", "설명 2", listId, 2));

        when(jwt.getSubject()).thenReturn(userId);
        when(cardQueryUseCase.searchCards(any(ListId.class), any(String.class), any(UserId.class)))
                .thenReturn(Either.right(cards));

        // when
        ResponseEntity<?> response = controller.searchCards(listId, searchTerm, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CardResponse> responses = (List<CardResponse>) response.getBody();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).title()).contains("테스트");
        assertThat(responses.get(1).title()).contains("테스트");
    }

    @Test
    @DisplayName("카드 검색 성공 - 검색 결과 없음")
    void searchCards_NoResults() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        String searchTerm = "존재하지않는카드";

        when(jwt.getSubject()).thenReturn(userId);
        when(cardQueryUseCase.searchCards(any(ListId.class), any(String.class), any(UserId.class)))
                .thenReturn(Either.right(List.of()));

        // when
        ResponseEntity<?> response = controller.searchCards(listId, searchTerm, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CardResponse> responses = (List<CardResponse>) response.getBody();
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("카드 검색 실패 - 리스트 없음")
    void searchCards_ListNotFound() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        String searchTerm = "테스트";
        Failure notFoundFailure = Failure.ofNotFound("LIST_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(cardQueryUseCase.searchCards(any(ListId.class), any(String.class), any(UserId.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.searchCards(listId, searchTerm, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("카드 생성 - 설명 없이")
    void createCard_WithoutDescription() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        CreateCardRequest request = new CreateCardRequest("새 카드", null, listId);
        Card createdCard = createSampleCard("card-new", "새 카드", null, listId, 1);

        when(jwt.getSubject()).thenReturn(userId);
        when(createCardUseCase.createCard(any(CreateCardCommand.class)))
                .thenReturn(Either.right(createdCard));

        // when
        ResponseEntity<?> response = controller.createCard(request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.title()).isEqualTo("새 카드");
        assertThat(responseBody.description()).isNull();
    }

    @Test
    @DisplayName("카드 수정 - 설명 없이")
    void updateCard_WithoutDescription() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        UpdateCardRequest request = new UpdateCardRequest("수정된 카드", null);
        Card updatedCard = createSampleCard(cardId, "수정된 카드", null, "list-123", 1);

        when(jwt.getSubject()).thenReturn(userId);
        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                .thenReturn(Either.right(updatedCard));

        // when
        ResponseEntity<?> response = controller.updateCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.title()).isEqualTo("수정된 카드");
        assertThat(responseBody.description()).isNull();
    }

    @Test
    @DisplayName("카드 복제 - 제목 없이")
    void cloneCard_WithoutTitle() throws Exception {
        // given
        String userId = "user-123";
        String cardId = "card-123";
        CloneCardRequest request = new CloneCardRequest(null, "list-456"); // 제목 없이 복제
        Card clonedCard = createSampleCard("card-cloned", "원본 카드 (복사본)", "카드 설명", "list-456", 1);

        when(jwt.getSubject()).thenReturn(userId);
        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                .thenReturn(Either.right(clonedCard));

        // when
        ResponseEntity<?> response = controller.cloneCard(cardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CardResponse responseBody = (CardResponse) response.getBody();
        assertThat(responseBody.cardId()).isEqualTo("card-cloned");
        assertThat(responseBody.listId()).isEqualTo("list-456");
    }
}