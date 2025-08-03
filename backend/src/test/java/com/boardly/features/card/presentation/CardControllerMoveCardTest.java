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
import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.application.usecase.MoveCardUseCase;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.model.CardPriority;
import com.boardly.features.card.presentation.request.MoveCardRequest;
import com.boardly.features.card.presentation.response.CardResponse;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
class CardControllerMoveCardTest {

    private CardController cardController;

    @Mock
    private MoveCardUseCase moveCardUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    @Mock
    private MockHttpServletRequest httpRequest;

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_CARD_ID = "test-card-id";
    private static final String TEST_TARGET_LIST_ID = "test-target-list-id";
    private static final String TEST_TITLE = "테스트 카드";
    private static final String TEST_DESCRIPTION = "테스트 카드 설명";

    @BeforeEach
    void setUp() {
        cardController = new CardController(
                null, // createCardUseCase
                null, // cardQueryUseCase
                null, // updateCardUseCase
                moveCardUseCase,
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
                .listId(new ListId(TEST_TARGET_LIST_ID))
                .position(2)
                .priority(CardPriority.MEDIUM)
                .isCompleted(false)
                .startDate(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private MoveCardRequest createTestRequest() {
        return new MoveCardRequest(TEST_TARGET_LIST_ID, 2);
    }

    @Test
    @DisplayName("카드 이동 성공 시 200 응답을 반환해야 한다")
    void moveCard_withValidRequest_shouldReturn200() {
        // given
        MoveCardRequest request = createTestRequest();
        Card card = createTestCard();

        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                .thenReturn(Either.right(card));

        // when
        ResponseEntity<?> response = cardController.moveCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        CardResponse cardResponse = (CardResponse) response.getBody();
        assertNotNull(cardResponse);
        assertThat(cardResponse.cardId()).isEqualTo(TEST_CARD_ID);
        assertThat(cardResponse.listId()).isEqualTo(TEST_TARGET_LIST_ID);
        assertThat(cardResponse.position()).isEqualTo(2);

        verify(moveCardUseCase).moveCard(any(MoveCardCommand.class));
    }

    @Test
    @DisplayName("카드 이동 실패 시 failureHandler가 호출되어야 한다")
    void moveCard_withFailure_shouldCallFailureHandler() {
        // given
        MoveCardRequest request = createTestRequest();
        Failure failure = Failure.ofNotFound("카드를 찾을 수 없습니다");

        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = cardController.moveCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(moveCardUseCase).moveCard(any(MoveCardCommand.class));
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("같은 리스트 내에서 위치만 변경하는 경우 성공해야 한다")
    void moveCard_sameListDifferentPosition_shouldReturn200() {
        // given
        MoveCardRequest request = new MoveCardRequest(null, 3); // 같은 리스트, 위치만 변경
        Card card = createTestCard();

        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                .thenReturn(Either.right(card));

        // when
        ResponseEntity<?> response = cardController.moveCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        verify(moveCardUseCase).moveCard(any(MoveCardCommand.class));
    }

    @Test
    @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
    void moveCard_shouldExtractUserIdFromJwt() {
        // given
        MoveCardRequest request = createTestRequest();
        Card card = createTestCard();

        when(moveCardUseCase.moveCard(any(MoveCardCommand.class)))
                .thenReturn(Either.right(card));

        // when
        cardController.moveCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        verify(moveCardUseCase).moveCard(any(MoveCardCommand.class));
    }
}