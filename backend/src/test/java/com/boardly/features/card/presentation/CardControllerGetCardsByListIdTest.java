package com.boardly.features.card.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.application.usecase.CardQueryUseCase;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.model.CardPriority;
import com.boardly.features.card.presentation.response.CardResponse;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
class CardControllerGetCardsByListIdTest {

    private CardController cardController;

    @Mock
    private CardQueryUseCase cardQueryUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    @Mock
    private MockHttpServletRequest httpRequest;

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_LIST_ID = "test-list-id";
    private static final String TEST_CARD_ID_1 = "test-card-id-1";
    private static final String TEST_CARD_ID_2 = "test-card-id-2";
    private static final String TEST_TITLE_1 = "테스트 카드 1";
    private static final String TEST_TITLE_2 = "테스트 카드 2";
    private static final String TEST_DESCRIPTION = "테스트 카드 설명";

    @BeforeEach
    void setUp() {
        cardController = new CardController(
                null, // createCardUseCase
                cardQueryUseCase,
                null, // getCardDetailUseCase
                null, // updateCardUseCase
                null, // moveCardUseCase
                null, // cloneCardUseCase
                null, // deleteCardUseCase
                null, // manageCardMemberUseCase
                null, // manageCardLabelUseCase
                failureHandler);

        when(jwt.getSubject()).thenReturn(TEST_USER_ID);
    }

    private Card createTestCard(String cardId, String title, int position) {
        return Card.builder()
                .cardId(new CardId(cardId))
                .title(title)
                .description(TEST_DESCRIPTION)
                .listId(new ListId(TEST_LIST_ID))
                .position(position)
                .priority(CardPriority.MEDIUM)
                .isCompleted(false)
                .createdBy(new UserId(TEST_USER_ID))
                .startDate(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private List<Card> createTestCards() {
        return List.of(
                createTestCard(TEST_CARD_ID_1, TEST_TITLE_1, 1),
                createTestCard(TEST_CARD_ID_2, TEST_TITLE_2, 2));
    }

    @Test
    @DisplayName("리스트별 카드 목록 조회 성공 시 200 응답을 반환해야 한다")
    void getCardsByListId_withValidRequest_shouldReturn200() {
        // given
        List<Card> cards = createTestCards();

        when(cardQueryUseCase.getCardsByListId(any(ListId.class), any(UserId.class)))
                .thenReturn(Either.right(cards));

        // when
        ResponseEntity<?> response = cardController.getCardsByListId(TEST_LIST_ID, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<CardResponse> cardResponses = (List<CardResponse>) response.getBody();
        assertNotNull(cardResponses);
        assertThat(cardResponses).hasSize(2);
        assertThat(cardResponses.get(0).cardId()).isEqualTo(TEST_CARD_ID_1);
        assertThat(cardResponses.get(0).title()).isEqualTo(TEST_TITLE_1);
        assertThat(cardResponses.get(1).cardId()).isEqualTo(TEST_CARD_ID_2);
        assertThat(cardResponses.get(1).title()).isEqualTo(TEST_TITLE_2);

        verify(cardQueryUseCase).getCardsByListId(any(ListId.class), any(UserId.class));
    }

    @Test
    @DisplayName("리스트별 카드 목록 조회 실패 시 failureHandler가 호출되어야 한다")
    void getCardsByListId_withFailure_shouldCallFailureHandler() {
        // given
        Failure failure = Failure.ofNotFound("리스트를 찾을 수 없습니다");

        when(cardQueryUseCase.getCardsByListId(any(ListId.class), any(UserId.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = cardController.getCardsByListId(TEST_LIST_ID, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(cardQueryUseCase).getCardsByListId(any(ListId.class), any(UserId.class));
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("빈 카드 목록 조회 시 빈 리스트를 반환해야 한다")
    void getCardsByListId_withEmptyList_shouldReturnEmptyList() {
        // given
        List<Card> emptyCards = List.of();

        when(cardQueryUseCase.getCardsByListId(any(ListId.class), any(UserId.class)))
                .thenReturn(Either.right(emptyCards));

        // when
        ResponseEntity<?> response = cardController.getCardsByListId(TEST_LIST_ID, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<CardResponse> cardResponses = (List<CardResponse>) response.getBody();
        assertThat(cardResponses).isEmpty();

        verify(cardQueryUseCase).getCardsByListId(any(ListId.class), any(UserId.class));
    }

    @Test
    @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
    void getCardsByListId_shouldExtractUserIdFromJwt() {
        // given
        List<Card> cards = createTestCards();

        when(cardQueryUseCase.getCardsByListId(any(ListId.class), any(UserId.class)))
                .thenReturn(Either.right(cards));

        // when
        cardController.getCardsByListId(TEST_LIST_ID, httpRequest, jwt);

        // then
        verify(cardQueryUseCase).getCardsByListId(any(ListId.class), any(UserId.class));
    }
}