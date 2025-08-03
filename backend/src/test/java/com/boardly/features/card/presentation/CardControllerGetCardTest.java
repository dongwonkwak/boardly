package com.boardly.features.card.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

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
class CardControllerGetCardTest {

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
    private static final String TEST_CARD_ID = "test-card-id";
    private static final String TEST_LIST_ID = "test-list-id";
    private static final String TEST_TITLE = "테스트 카드";
    private static final String TEST_DESCRIPTION = "테스트 카드 설명";

    @BeforeEach
    void setUp() {
        cardController = new CardController(
                null, // createCardUseCase
                cardQueryUseCase,
                null, // updateCardUseCase
                null, // moveCardUseCase
                null, // cloneCardUseCase
                null, // deleteCardUseCase
                null, // manageCardMemberUseCase
                null, // manageCardLabelUseCase
                failureHandler);

        when(jwt.getSubject()).thenReturn(TEST_USER_ID);
    }

    private Card createTestCard() {
        return Card.builder()
                .cardId(new CardId(TEST_CARD_ID))
                .title(TEST_TITLE)
                .description(TEST_DESCRIPTION)
                .listId(new ListId(TEST_LIST_ID))
                .position(1)
                .priority(CardPriority.MEDIUM)
                .isCompleted(false)
                .startDate(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("카드 조회 성공 시 200 응답을 반환해야 한다")
    void getCard_withValidRequest_shouldReturn200() {
        // given
        Card card = createTestCard();

        when(cardQueryUseCase.getCard(any(CardId.class), any(UserId.class)))
                .thenReturn(Either.right(card));

        // when
        ResponseEntity<?> response = cardController.getCard(TEST_CARD_ID, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        CardResponse cardResponse = (CardResponse) response.getBody();
        assertNotNull(cardResponse);
        assertThat(cardResponse.cardId()).isEqualTo(TEST_CARD_ID);
        assertThat(cardResponse.title()).isEqualTo(TEST_TITLE);
        assertThat(cardResponse.description()).isEqualTo(TEST_DESCRIPTION);
        assertThat(cardResponse.listId()).isEqualTo(TEST_LIST_ID);

        verify(cardQueryUseCase).getCard(any(CardId.class), any(UserId.class));
    }

    @Test
    @DisplayName("카드 조회 실패 시 failureHandler가 호출되어야 한다")
    void getCard_withFailure_shouldCallFailureHandler() {
        // given
        Failure failure = Failure.ofNotFound("카드를 찾을 수 없습니다");

        when(cardQueryUseCase.getCard(any(CardId.class), any(UserId.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = cardController.getCard(TEST_CARD_ID, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(cardQueryUseCase).getCard(any(CardId.class), any(UserId.class));
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
    void getCard_shouldExtractUserIdFromJwt() {
        // given
        Card card = createTestCard();

        when(cardQueryUseCase.getCard(any(CardId.class), any(UserId.class)))
                .thenReturn(Either.right(card));

        // when
        cardController.getCard(TEST_CARD_ID, httpRequest, jwt);

        // then
        verify(cardQueryUseCase).getCard(any(CardId.class), any(UserId.class));
    }
}