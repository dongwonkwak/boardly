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
import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.application.usecase.UpdateCardUseCase;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.model.CardPriority;
import com.boardly.features.card.presentation.request.UpdateCardRequest;
import com.boardly.features.card.presentation.response.CardResponse;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;

import io.vavr.control.Either;
import com.boardly.features.user.domain.model.UserId;

@ExtendWith(MockitoExtension.class)
class CardControllerUpdateCardTest {

    private CardController cardController;

    @Mock
    private UpdateCardUseCase updateCardUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    @Mock
    private MockHttpServletRequest httpRequest;

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_CARD_ID = "test-card-id";
    private static final String TEST_LIST_ID = "test-list-id";
    private static final String TEST_TITLE = "수정된 카드 제목";
    private static final String TEST_DESCRIPTION = "수정된 카드 설명";

    @BeforeEach
    void setUp() {
        cardController = new CardController(
                null, // createCardUseCase
                null, // cardQueryUseCase
                updateCardUseCase,
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
                .createdBy(new UserId(TEST_USER_ID))
                .startDate(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private UpdateCardRequest createTestRequest() {
        return new UpdateCardRequest(TEST_TITLE, TEST_DESCRIPTION);
    }

    @Test
    @DisplayName("카드 수정 성공 시 200 응답을 반환해야 한다")
    void updateCard_withValidRequest_shouldReturn200() {
        // given
        UpdateCardRequest request = createTestRequest();
        Card card = createTestCard();

        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                .thenReturn(Either.right(card));

        // when
        ResponseEntity<?> response = cardController.updateCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        CardResponse cardResponse = (CardResponse) response.getBody();
        assertNotNull(cardResponse);
        assertThat(cardResponse.cardId()).isEqualTo(TEST_CARD_ID);
        assertThat(cardResponse.title()).isEqualTo(TEST_TITLE);
        assertThat(cardResponse.description()).isEqualTo(TEST_DESCRIPTION);
        assertThat(cardResponse.listId()).isEqualTo(TEST_LIST_ID);

        verify(updateCardUseCase).updateCard(any(UpdateCardCommand.class));
    }

    @Test
    @DisplayName("카드 수정 실패 시 failureHandler가 호출되어야 한다")
    void updateCard_withFailure_shouldCallFailureHandler() {
        // given
        UpdateCardRequest request = createTestRequest();
        Failure failure = Failure.ofNotFound("카드를 찾을 수 없습니다");

        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = cardController.updateCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(updateCardUseCase).updateCard(any(UpdateCardCommand.class));
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
    void updateCard_shouldExtractUserIdFromJwt() {
        // given
        UpdateCardRequest request = createTestRequest();
        Card card = createTestCard();

        when(updateCardUseCase.updateCard(any(UpdateCardCommand.class)))
                .thenReturn(Either.right(card));

        // when
        cardController.updateCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        verify(updateCardUseCase).updateCard(any(UpdateCardCommand.class));
    }

    @Test
    @DisplayName("카드 우선순위 업데이트 성공 시 200 응답을 반환해야 한다")
    void updateCardPriority_withValidRequest_shouldReturn200() {
        // given
        String priority = "HIGH";
        Card card = createTestCard();

        when(updateCardUseCase.updateCardPriority(TEST_CARD_ID, priority))
                .thenReturn(Either.right(card));

        // when
        ResponseEntity<?> response = cardController.updateCardPriority(TEST_CARD_ID,
                new com.boardly.features.card.presentation.request.UpdateCardPriorityRequest(priority),
                httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        verify(updateCardUseCase).updateCardPriority(TEST_CARD_ID, priority);
    }

    @Test
    @DisplayName("카드 완료 상태 업데이트 성공 시 200 응답을 반환해야 한다")
    void updateCardCompleted_withValidRequest_shouldReturn200() {
        // given
        boolean isCompleted = true;
        Card card = createTestCard();

        when(updateCardUseCase.updateCardCompleted(TEST_CARD_ID, isCompleted))
                .thenReturn(Either.right(card));

        // when
        ResponseEntity<?> response = cardController.updateCardCompleted(TEST_CARD_ID,
                new com.boardly.features.card.presentation.request.UpdateCardCompletedRequest(
                        isCompleted),
                httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        verify(updateCardUseCase).updateCardCompleted(TEST_CARD_ID, isCompleted);
    }

    @Test
    @DisplayName("카드 시작일 업데이트 성공 시 200 응답을 반환해야 한다")
    void updateCardStartDate_withValidRequest_shouldReturn200() {
        // given
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        Card card = createTestCard();

        when(updateCardUseCase.updateCardStartDate(TEST_CARD_ID, startDate))
                .thenReturn(Either.right(card));

        // when
        ResponseEntity<?> response = cardController.updateCardStartDate(TEST_CARD_ID,
                new com.boardly.features.card.presentation.request.UpdateCardStartDateRequest(
                        startDate),
                httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        verify(updateCardUseCase).updateCardStartDate(TEST_CARD_ID, startDate);
    }
}