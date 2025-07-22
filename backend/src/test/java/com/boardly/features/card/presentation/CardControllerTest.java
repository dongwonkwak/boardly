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
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardController 테스트")
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

        @Mock
        private HttpServletRequest httpRequest;

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

        private Failure createSampleFailure(String message) {
                return Failure.ofInputError(message);
        }

        @Nested
        @DisplayName("카드 생성 테스트")
        class CreateCardTest {

                @Test
                @DisplayName("카드 생성 성공")
                void createCard_Success() {
                        // given
                        String userId = "user-123";
                        CreateCardRequest request = new CreateCardRequest("테스트 카드", "테스트 설명", "list-123");
                        Card createdCard = createSampleCard("card-123", "테스트 카드", "테스트 설명", "list-123", 1);

                        when(jwt.getSubject()).thenReturn(userId);
                        when(createCardUseCase.createCard(any(CreateCardCommand.class)))
                                        .thenReturn(Either.right(createdCard));

                        // when
                        ResponseEntity<?> response = controller.createCard(request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

                        CardResponse cardResponse = (CardResponse) response.getBody();
                        assertThat(cardResponse.cardId()).isEqualTo("card-123");
                        assertThat(cardResponse.title()).isEqualTo("테스트 카드");
                        assertThat(cardResponse.description()).isEqualTo("테스트 설명");
                        assertThat(cardResponse.listId()).isEqualTo("list-123");

                        verify(createCardUseCase).createCard(any(CreateCardCommand.class));
                }

                @Test
                @DisplayName("카드 생성 실패 - 검증 오류")
                void createCard_ValidationError() {
                        // given
                        String userId = "user-123";
                        CreateCardRequest request = new CreateCardRequest("", "테스트 설명", "list-123");
                        Failure failure = createSampleFailure("제목은 필수입니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(createCardUseCase.createCard(any(CreateCardCommand.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(ResponseEntity.badRequest()
                                                        .body(ErrorResponse.of("VALIDATION_ERROR", "제목은 필수입니다")));

                        // when
                        ResponseEntity<?> response = controller.createCard(request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        verify(failureHandler).handleFailure(failure);
                }
        }

        @Nested
        @DisplayName("카드 조회 테스트")
        class GetCardTest {

                @Test
                @DisplayName("카드 조회 성공")
                void getCard_Success() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        Card card = createSampleCard(cardId, "테스트 카드", "테스트 설명", "list-123", 1);

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cardQueryUseCase.getCard(any(CardId.class), any(UserId.class)))
                                        .thenReturn(Either.right(card));

                        // when
                        ResponseEntity<?> response = controller.getCard(cardId, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

                        CardResponse cardResponse = (CardResponse) response.getBody();
                        assertThat(cardResponse.cardId()).isEqualTo(cardId);
                        assertThat(cardResponse.title()).isEqualTo("테스트 카드");

                        verify(cardQueryUseCase).getCard(any(CardId.class), any(UserId.class));
                }

                @Test
                @DisplayName("카드 조회 실패 - 카드 없음")
                void getCard_NotFound() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        Failure failure = createSampleFailure("카드를 찾을 수 없습니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cardQueryUseCase.getCard(any(CardId.class), any(UserId.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(ResponseEntity.notFound().build());

                        // when
                        ResponseEntity<?> response = controller.getCard(cardId, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        verify(failureHandler).handleFailure(failure);
                }
        }

        @Nested
        @DisplayName("리스트별 카드 목록 조회 테스트")
        class GetCardsByListIdTest {

                @Test
                @DisplayName("리스트별 카드 목록 조회 성공")
                void getCardsByListId_Success() {
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
                        ResponseEntity<?> response = controller.getCardsByListId(listId, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isInstanceOf(List.class);

                        @SuppressWarnings("unchecked")
                        List<CardResponse> cardResponses = (List<CardResponse>) response.getBody();
                        assertThat(cardResponses).hasSize(2);
                        assertThat(cardResponses.get(0).title()).isEqualTo("카드 1");
                        assertThat(cardResponses.get(1).title()).isEqualTo("카드 2");

                        verify(cardQueryUseCase).getCardsByListId(any(ListId.class), any(UserId.class));
                }

                @Test
                @DisplayName("리스트별 카드 목록 조회 성공 - 빈 목록")
                void getCardsByListId_Success_EmptyList() {
                        // given
                        String userId = "user-123";
                        String listId = "list-123";
                        List<Card> cards = List.of();

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cardQueryUseCase.getCardsByListId(any(ListId.class), any(UserId.class)))
                                        .thenReturn(Either.right(cards));

                        // when
                        ResponseEntity<?> response = controller.getCardsByListId(listId, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isInstanceOf(List.class);

                        @SuppressWarnings("unchecked")
                        List<CardResponse> cardResponses = (List<CardResponse>) response.getBody();
                        assertThat(cardResponses).isEmpty();

                        verify(cardQueryUseCase).getCardsByListId(any(ListId.class), any(UserId.class));
                }

                @Test
                @DisplayName("리스트별 카드 목록 조회 실패 - 리스트 없음")
                void getCardsByListId_NotFound() {
                        // given
                        String userId = "user-123";
                        String listId = "list-123";
                        Failure failure = createSampleFailure("리스트를 찾을 수 없습니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cardQueryUseCase.getCardsByListId(any(ListId.class), any(UserId.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(ResponseEntity.notFound().build());

                        // when
                        ResponseEntity<?> response = controller.getCardsByListId(listId, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        verify(failureHandler).handleFailure(failure);
                }
        }

        @Nested
        @DisplayName("카드 수정 테스트")
        class UpdateCardTest {

                @Test
                @DisplayName("카드 수정 성공")
                void updateCard_Success() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        UpdateCardRequest request = new UpdateCardRequest("수정된 카드", "수정된 설명");
                        Card updatedCard = createSampleCard(cardId, "수정된 카드", "수정된 설명", "list-123", 1);

                        when(jwt.getSubject()).thenReturn(userId);
                        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                                        .thenReturn(Either.right(updatedCard));

                        // when
                        ResponseEntity<?> response = controller.updateCard(cardId, request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

                        CardResponse cardResponse = (CardResponse) response.getBody();
                        assertThat(cardResponse.cardId()).isEqualTo(cardId);
                        assertThat(cardResponse.title()).isEqualTo("수정된 카드");
                        assertThat(cardResponse.description()).isEqualTo("수정된 설명");

                        verify(updateCardUseCase).updateCard(any(UpdateCardCommand.class));
                }

                @Test
                @DisplayName("카드 수정 실패 - 카드 없음")
                void updateCard_NotFound() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        UpdateCardRequest request = new UpdateCardRequest("수정된 카드", "수정된 설명");
                        Failure failure = createSampleFailure("카드를 찾을 수 없습니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(ResponseEntity.notFound().build());

                        // when
                        ResponseEntity<?> response = controller.updateCard(cardId, request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        verify(failureHandler).handleFailure(failure);
                }

                @Test
                @DisplayName("카드 수정 실패 - 아카이브된 보드")
                void updateCard_ArchivedBoard() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        UpdateCardRequest request = new UpdateCardRequest("수정된 카드", "수정된 설명");
                        Failure failure = Failure.ofConflict("아카이브된 보드의 카드는 수정할 수 없습니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT)
                                                        .body(ErrorResponse.of("CONFLICT",
                                                                        "아카이브된 보드의 카드는 수정할 수 없습니다")));

                        // when
                        ResponseEntity<?> response = controller.updateCard(cardId, request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        verify(failureHandler).handleFailure(failure);
                }
        }

        @Nested
        @DisplayName("카드 이동 테스트")
        class MoveCardTest {

                @Test
                @DisplayName("카드 이동 성공 - 같은 리스트 내 이동")
                void moveCard_Success_SameList() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        MoveCardRequest request = new MoveCardRequest(null, 3);
                        Card movedCard = createSampleCard(cardId, "테스트 카드", "테스트 설명", "list-123", 3);

                        when(jwt.getSubject()).thenReturn(userId);
                        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                                        .thenReturn(Either.right(movedCard));

                        // when
                        ResponseEntity<?> response = controller.moveCard(cardId, request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

                        CardResponse cardResponse = (CardResponse) response.getBody();
                        assertThat(cardResponse.cardId()).isEqualTo(cardId);
                        assertThat(cardResponse.position()).isEqualTo(3);

                        verify(moveCardUseCase).moveCard(any(MoveCardCommand.class));
                }

                @Test
                @DisplayName("카드 이동 성공 - 다른 리스트로 이동")
                void moveCard_Success_DifferentList() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        MoveCardRequest request = new MoveCardRequest("list-456", 1);
                        Card movedCard = createSampleCard(cardId, "테스트 카드", "테스트 설명", "list-456", 1);

                        when(jwt.getSubject()).thenReturn(userId);
                        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                                        .thenReturn(Either.right(movedCard));

                        // when
                        ResponseEntity<?> response = controller.moveCard(cardId, request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

                        CardResponse cardResponse = (CardResponse) response.getBody();
                        assertThat(cardResponse.cardId()).isEqualTo(cardId);
                        assertThat(cardResponse.listId()).isEqualTo("list-456");
                        assertThat(cardResponse.position()).isEqualTo(1);

                        verify(moveCardUseCase).moveCard(any(MoveCardCommand.class));
                }

                @Test
                @DisplayName("카드 이동 실패 - 카드 없음")
                void moveCard_NotFound() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        MoveCardRequest request = new MoveCardRequest("list-456", 1);
                        Failure failure = createSampleFailure("카드를 찾을 수 없습니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(ResponseEntity.notFound().build());

                        // when
                        ResponseEntity<?> response = controller.moveCard(cardId, request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        verify(failureHandler).handleFailure(failure);
                }
        }

        @Nested
        @DisplayName("카드 복제 테스트")
        class CloneCardTest {

                @Test
                @DisplayName("카드 복제 성공 - 같은 리스트에 복제")
                void cloneCard_Success_SameList() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        CloneCardRequest request = new CloneCardRequest("복제된 카드", null);
                        Card clonedCard = createSampleCard("card-456", "복제된 카드", "테스트 설명", "list-123", 2);

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                                        .thenReturn(Either.right(clonedCard));

                        // when
                        ResponseEntity<?> response = controller.cloneCard(cardId, request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

                        CardResponse cardResponse = (CardResponse) response.getBody();
                        assertThat(cardResponse.cardId()).isEqualTo("card-456");
                        assertThat(cardResponse.title()).isEqualTo("복제된 카드");
                        assertThat(cardResponse.listId()).isEqualTo("list-123");

                        verify(cloneCardUseCase).cloneCard(any(CloneCardCommand.class));
                }

                @Test
                @DisplayName("카드 복제 성공 - 다른 리스트에 복제")
                void cloneCard_Success_DifferentList() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        CloneCardRequest request = new CloneCardRequest("복제된 카드", "list-456");
                        Card clonedCard = createSampleCard("card-456", "복제된 카드", "테스트 설명", "list-456", 1);

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                                        .thenReturn(Either.right(clonedCard));

                        // when
                        ResponseEntity<?> response = controller.cloneCard(cardId, request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

                        CardResponse cardResponse = (CardResponse) response.getBody();
                        assertThat(cardResponse.cardId()).isEqualTo("card-456");
                        assertThat(cardResponse.title()).isEqualTo("복제된 카드");
                        assertThat(cardResponse.listId()).isEqualTo("list-456");

                        verify(cloneCardUseCase).cloneCard(any(CloneCardCommand.class));
                }

                @Test
                @DisplayName("카드 복제 실패 - 카드 없음")
                void cloneCard_NotFound() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        CloneCardRequest request = new CloneCardRequest("복제된 카드", "list-456");
                        Failure failure = createSampleFailure("카드를 찾을 수 없습니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(ResponseEntity.notFound().build());

                        // when
                        ResponseEntity<?> response = controller.cloneCard(cardId, request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        verify(failureHandler).handleFailure(failure);
                }
        }

        @Nested
        @DisplayName("카드 삭제 테스트")
        class DeleteCardTest {

                @Test
                @DisplayName("카드 삭제 성공")
                void deleteCard_Success() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";

                        when(jwt.getSubject()).thenReturn(userId);
                        when(deleteCardUseCase.deleteCard(any(DeleteCardCommand.class)))
                                        .thenReturn(Either.right(null));

                        // when
                        ResponseEntity<?> response = controller.deleteCard(cardId, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                        assertThat(response.getBody()).isNull();

                        verify(deleteCardUseCase).deleteCard(any(DeleteCardCommand.class));
                }

                @Test
                @DisplayName("카드 삭제 실패 - 카드 없음")
                void deleteCard_NotFound() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        Failure failure = createSampleFailure("카드를 찾을 수 없습니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(deleteCardUseCase.deleteCard(any(DeleteCardCommand.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(ResponseEntity.notFound().build());

                        // when
                        ResponseEntity<?> response = controller.deleteCard(cardId, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        verify(failureHandler).handleFailure(failure);
                }

                @Test
                @DisplayName("카드 삭제 실패 - 아카이브된 보드")
                void deleteCard_ArchivedBoard() {
                        // given
                        String userId = "user-123";
                        String cardId = "card-123";
                        Failure failure = Failure.ofConflict("아카이브된 보드의 카드는 삭제할 수 없습니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(deleteCardUseCase.deleteCard(any(DeleteCardCommand.class)))
                                        .thenReturn(Either.left(failure));
                                    when(failureHandler.handleFailure(failure))
                    .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(ErrorResponse.of("CONFLICT", "아카이브된 보드의 카드는 삭제할 수 없습니다")));

                        // when
                        ResponseEntity<?> response = controller.deleteCard(cardId, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        verify(failureHandler).handleFailure(failure);
                }
        }

        @Nested
        @DisplayName("카드 검색 테스트")
        class SearchCardsTest {

                @Test
                @DisplayName("카드 검색 성공")
                void searchCards_Success() {
                        // given
                        String userId = "user-123";
                        String listId = "list-123";
                        String searchTerm = "테스트";
                        List<Card> cards = List.of(
                                        createSampleCard("card-1", "테스트 카드 1", "설명 1", listId, 1),
                                        createSampleCard("card-2", "테스트 카드 2", "설명 2", listId, 2));

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cardQueryUseCase.searchCards(any(ListId.class), eq(searchTerm), any(UserId.class)))
                                        .thenReturn(Either.right(cards));

                        // when
                        ResponseEntity<?> response = controller.searchCards(listId, searchTerm, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isInstanceOf(List.class);

                        @SuppressWarnings("unchecked")
                        List<CardResponse> cardResponses = (List<CardResponse>) response.getBody();
                        assertThat(cardResponses).hasSize(2);
                        assertThat(cardResponses.get(0).title()).isEqualTo("테스트 카드 1");
                        assertThat(cardResponses.get(1).title()).isEqualTo("테스트 카드 2");

                        verify(cardQueryUseCase).searchCards(any(ListId.class), eq(searchTerm), any(UserId.class));
                }

                @Test
                @DisplayName("카드 검색 성공 - 검색 결과 없음")
                void searchCards_Success_NoResults() {
                        // given
                        String userId = "user-123";
                        String listId = "list-123";
                        String searchTerm = "존재하지않는카드";
                        List<Card> cards = List.of();

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cardQueryUseCase.searchCards(any(ListId.class), eq(searchTerm), any(UserId.class)))
                                        .thenReturn(Either.right(cards));

                        // when
                        ResponseEntity<?> response = controller.searchCards(listId, searchTerm, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isInstanceOf(List.class);

                        @SuppressWarnings("unchecked")
                        List<CardResponse> cardResponses = (List<CardResponse>) response.getBody();
                        assertThat(cardResponses).isEmpty();

                        verify(cardQueryUseCase).searchCards(any(ListId.class), eq(searchTerm), any(UserId.class));
                }

                @Test
                @DisplayName("카드 검색 실패 - 리스트 없음")
                void searchCards_NotFound() {
                        // given
                        String userId = "user-123";
                        String listId = "list-123";
                        String searchTerm = "테스트";
                        Failure failure = createSampleFailure("리스트를 찾을 수 없습니다");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(cardQueryUseCase.searchCards(any(ListId.class), eq(searchTerm), any(UserId.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(ResponseEntity.notFound().build());

                        // when
                        ResponseEntity<?> response = controller.searchCards(listId, searchTerm, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        verify(failureHandler).handleFailure(failure);
                }
        }
}