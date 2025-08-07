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
import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.features.card.application.usecase.CloneCardUseCase;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.model.CardPriority;
import com.boardly.features.card.presentation.request.CloneCardRequest;
import com.boardly.features.card.presentation.response.CardResponse;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;

import io.vavr.control.Either;
import com.boardly.features.user.domain.model.UserId;

@ExtendWith(MockitoExtension.class)
class CardControllerCloneCardTest {

    private CardController cardController;

    @Mock
    private CloneCardUseCase cloneCardUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    @Mock
    private MockHttpServletRequest httpRequest;

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_CARD_ID = "test-card-id";
    private static final String TEST_CLONED_CARD_ID = "test-cloned-card-id";
    private static final String TEST_TARGET_LIST_ID = "test-target-list-id";
    private static final String TEST_CLONED_TITLE = "복제된 테스트 카드";
    private static final String TEST_DESCRIPTION = "테스트 카드 설명";

    @BeforeEach
    void setUp() {
        cardController = new CardController(
                null, // createCardUseCase
                null, // cardQueryUseCase
                null, // updateCardUseCase
                null, // moveCardUseCase
                cloneCardUseCase,
                null, // deleteCardUseCase
                null, // manageCardMemberUseCase
                null, // manageCardLabelUseCase
                failureHandler);

        when(jwt.getSubject()).thenReturn(TEST_USER_ID);
    }

    private Card createTestClonedCard() {
        return Card.builder()
                .cardId(new CardId(TEST_CLONED_CARD_ID))
                .title(TEST_CLONED_TITLE)
                .description(TEST_DESCRIPTION)
                .listId(new ListId(TEST_TARGET_LIST_ID))
                .position(1)
                .priority(CardPriority.MEDIUM)
                .isCompleted(false)
                .createdBy(new UserId(TEST_USER_ID))
                .startDate(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private CloneCardRequest createTestRequest() {
        return new CloneCardRequest(TEST_CLONED_TITLE, TEST_TARGET_LIST_ID);
    }

    @Test
    @DisplayName("카드 복제 성공 시 201 응답을 반환해야 한다")
    void cloneCard_withValidRequest_shouldReturn201() {
        // given
        CloneCardRequest request = createTestRequest();
        Card clonedCard = createTestClonedCard();

        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                .thenReturn(Either.right(clonedCard));

        // when
        ResponseEntity<?> response = cardController.cloneCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        CardResponse cardResponse = (CardResponse) response.getBody();
        assertNotNull(cardResponse);
        assertThat(cardResponse.cardId()).isEqualTo(TEST_CLONED_CARD_ID);
        assertThat(cardResponse.title()).isEqualTo(TEST_CLONED_TITLE);
        assertThat(cardResponse.description()).isEqualTo(TEST_DESCRIPTION);
        assertThat(cardResponse.listId()).isEqualTo(TEST_TARGET_LIST_ID);

        verify(cloneCardUseCase).cloneCard(any(CloneCardCommand.class));
    }

    @Test
    @DisplayName("카드 복제 실패 시 failureHandler가 호출되어야 한다")
    void cloneCard_withFailure_shouldCallFailureHandler() {
        // given
        CloneCardRequest request = createTestRequest();
        Failure failure = Failure.ofNotFound("카드를 찾을 수 없습니다");

        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = cardController.cloneCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(cloneCardUseCase).cloneCard(any(CloneCardCommand.class));
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("같은 리스트에 복제하는 경우 성공해야 한다")
    void cloneCard_sameList_shouldReturn201() {
        // given
        CloneCardRequest request = new CloneCardRequest(TEST_CLONED_TITLE, null); // 같은 리스트에 복제
        Card clonedCard = createTestClonedCard();

        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                .thenReturn(Either.right(clonedCard));

        // when
        ResponseEntity<?> response = cardController.cloneCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(CardResponse.class);

        verify(cloneCardUseCase).cloneCard(any(CloneCardCommand.class));
    }

    @Test
    @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
    void cloneCard_shouldExtractUserIdFromJwt() {
        // given
        CloneCardRequest request = createTestRequest();
        Card clonedCard = createTestClonedCard();

        when(cloneCardUseCase.cloneCard(any(CloneCardCommand.class)))
                .thenReturn(Either.right(clonedCard));

        // when
        cardController.cloneCard(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        verify(cloneCardUseCase).cloneCard(any(CloneCardCommand.class));
    }
}