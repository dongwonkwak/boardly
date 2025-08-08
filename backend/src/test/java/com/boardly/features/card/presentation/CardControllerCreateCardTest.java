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
import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.application.usecase.CreateCardUseCase;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.model.CardPriority;
import com.boardly.features.card.presentation.request.CreateCardRequest;
import com.boardly.features.card.presentation.response.CardResponse;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.response.ErrorResponse;

import io.vavr.control.Either;
import com.boardly.features.user.domain.model.UserId;

@ExtendWith(MockitoExtension.class)
class CardControllerCreateCardTest {

    private CardController cardController;

    @Mock
    private CreateCardUseCase createCardUseCase;

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
                createCardUseCase,
                null, // cardQueryUseCase
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

    private CreateCardRequest createTestRequest() {
        return new CreateCardRequest(TEST_TITLE, TEST_DESCRIPTION, TEST_LIST_ID);
    }

    @Test
    @DisplayName("카드 생성 성공 시 201 응답을 반환해야 한다")
    void createCard_withValidRequest_shouldReturn201() {
        // given
        CreateCardRequest request = createTestRequest();
        Card card = createTestCard();

        when(createCardUseCase.createCard(any(CreateCardCommand.class)))
                .thenReturn(Either.right(card));

        // when
        ResponseEntity<?> response = cardController.createCard(request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        CardResponse cardResponse = (CardResponse) response.getBody();
        assertNotNull(cardResponse);
        assertThat(cardResponse.cardId()).isEqualTo(TEST_CARD_ID);
        assertThat(cardResponse.title()).isEqualTo(TEST_TITLE);
        assertThat(cardResponse.description()).isEqualTo(TEST_DESCRIPTION);
        assertThat(cardResponse.listId()).isEqualTo(TEST_LIST_ID);

        verify(createCardUseCase).createCard(any(CreateCardCommand.class));
    }

    @Test
    @DisplayName("카드 생성 실패 시 failureHandler가 호출되어야 한다")
    void createCard_withFailure_shouldCallFailureHandler() {
        // given
        CreateCardRequest request = createTestRequest();
        Failure failure = Failure.ofInputError("카드 생성에 실패했습니다");
        ErrorResponse errorResponse = ErrorResponse.of("INPUT_ERROR", "카드 생성에 실패했습니다");

        when(createCardUseCase.createCard(any(CreateCardCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.badRequest().body(errorResponse));

        // when
        ResponseEntity<?> response = cardController.createCard(request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);

        verify(createCardUseCase).createCard(any(CreateCardCommand.class));
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
    void createCard_shouldExtractUserIdFromJwt() {
        // given
        CreateCardRequest request = createTestRequest();
        Card card = createTestCard();

        when(createCardUseCase.createCard(any(CreateCardCommand.class)))
                .thenReturn(Either.right(card));

        // when
        cardController.createCard(request, httpRequest, jwt);

        // then
        verify(createCardUseCase).createCard(any(CreateCardCommand.class));
    }
}