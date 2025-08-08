package com.boardly.features.card.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boardly.features.card.application.usecase.ManageCardMemberUseCase;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.card.presentation.request.AssignCardMemberRequest;
import com.boardly.features.card.presentation.request.UnassignCardMemberRequest;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import io.vavr.control.Either;
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

@ExtendWith(MockitoExtension.class)
class CardControllerManageCardMemberTest {

    private CardController cardController;

    @Mock
    private ManageCardMemberUseCase manageCardMemberUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    @Mock
    private MockHttpServletRequest httpRequest;

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_CARD_ID = "test-card-id";
    private static final String TEST_MEMBER_ID = "test-member-id";

    @BeforeEach
    void setUp() {
        cardController = new CardController(
            null, // createCardUseCase
            null, // cardQueryUseCase
            null, // getCardDetailUseCase
            null, // updateCardUseCase
            null, // moveCardUseCase
            null, // cloneCardUseCase
            null, // deleteCardUseCase
            manageCardMemberUseCase,
            null, // manageCardLabelUseCase
            failureHandler
        );

        when(jwt.getSubject()).thenReturn(TEST_USER_ID);
    }

    private CardMember createTestCardMember() {
        return new CardMember(new UserId(TEST_MEMBER_ID));
    }

    private List<CardMember> createTestCardMembers() {
        return List.of(createTestCardMember());
    }

    @Test
    @DisplayName("카드 멤버 할당 성공 시 200 응답을 반환해야 한다")
    void assignCardMember_withValidRequest_shouldReturn200() {
        // given
        AssignCardMemberRequest request = new AssignCardMemberRequest(
            TEST_MEMBER_ID
        );

        when(manageCardMemberUseCase.assignMember(any())).thenReturn(
            Either.right(null)
        );

        // when
        ResponseEntity<?> response = cardController.assignCardMember(
            TEST_CARD_ID,
            request,
            httpRequest,
            jwt
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(manageCardMemberUseCase).assignMember(any());
    }

    @Test
    @DisplayName("카드 멤버 할당 실패 시 failureHandler가 호출되어야 한다")
    void assignCardMember_withFailure_shouldCallFailureHandler() {
        // given
        AssignCardMemberRequest request = new AssignCardMemberRequest(
            TEST_MEMBER_ID
        );
        Failure failure = Failure.ofNotFound("카드를 찾을 수 없습니다");

        when(manageCardMemberUseCase.assignMember(any())).thenReturn(
            Either.left(failure)
        );
        when(failureHandler.handleFailure(failure)).thenReturn(
            ResponseEntity.notFound().build()
        );

        // when
        ResponseEntity<?> response = cardController.assignCardMember(
            TEST_CARD_ID,
            request,
            httpRequest,
            jwt
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(manageCardMemberUseCase).assignMember(any());
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("카드 멤버 해제 성공 시 200 응답을 반환해야 한다")
    void unassignCardMember_withValidRequest_shouldReturn200() {
        // given
        UnassignCardMemberRequest request = new UnassignCardMemberRequest(
            TEST_MEMBER_ID
        );

        when(manageCardMemberUseCase.unassignMember(any())).thenReturn(
            Either.right(null)
        );

        // when
        ResponseEntity<?> response = cardController.unassignCardMember(
            TEST_CARD_ID,
            request,
            httpRequest,
            jwt
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(manageCardMemberUseCase).unassignMember(any());
    }

    @Test
    @DisplayName("카드 멤버 해제 실패 시 failureHandler가 호출되어야 한다")
    void unassignCardMember_withFailure_shouldCallFailureHandler() {
        // given
        UnassignCardMemberRequest request = new UnassignCardMemberRequest(
            TEST_MEMBER_ID
        );
        Failure failure = Failure.ofNotFound("카드를 찾을 수 없습니다");

        when(manageCardMemberUseCase.unassignMember(any())).thenReturn(
            Either.left(failure)
        );
        when(failureHandler.handleFailure(failure)).thenReturn(
            ResponseEntity.notFound().build()
        );

        // when
        ResponseEntity<?> response = cardController.unassignCardMember(
            TEST_CARD_ID,
            request,
            httpRequest,
            jwt
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(manageCardMemberUseCase).unassignMember(any());
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("카드 멤버 목록 조회 성공 시 200 응답을 반환해야 한다")
    void getCardMembers_withValidRequest_shouldReturn200() {
        // given
        List<CardMember> members = createTestCardMembers();

        when(manageCardMemberUseCase.getCardMembers(any(), any())).thenReturn(
            members
        );

        // when
        ResponseEntity<?> response = cardController.getCardMembers(
            TEST_CARD_ID,
            httpRequest,
            jwt
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<CardMember> responseMembers = (List<
            CardMember
        >) response.getBody();
        assertThat(responseMembers).hasSize(1);
        assertNotNull(responseMembers);
        assertThat(responseMembers.get(0).getUserId().getId()).isEqualTo(
            TEST_MEMBER_ID
        );

        verify(manageCardMemberUseCase).getCardMembers(any(), any());
    }

    @Test
    @DisplayName("빈 멤버 목록 조회 시 빈 리스트를 반환해야 한다")
    void getCardMembers_withEmptyList_shouldReturnEmptyList() {
        // given
        List<CardMember> emptyMembers = List.of();

        when(manageCardMemberUseCase.getCardMembers(any(), any())).thenReturn(
            emptyMembers
        );

        // when
        ResponseEntity<?> response = cardController.getCardMembers(
            TEST_CARD_ID,
            httpRequest,
            jwt
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<CardMember> responseMembers = (List<
            CardMember
        >) response.getBody();
        assertThat(responseMembers).isEmpty();

        verify(manageCardMemberUseCase).getCardMembers(any(), any());
    }

    @Test
    @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
    void manageCardMember_shouldExtractUserIdFromJwt() {
        // given
        AssignCardMemberRequest request = new AssignCardMemberRequest(
            TEST_MEMBER_ID
        );

        when(manageCardMemberUseCase.assignMember(any())).thenReturn(
            Either.right(null)
        );

        // when
        cardController.assignCardMember(
            TEST_CARD_ID,
            request,
            httpRequest,
            jwt
        );

        // then
        verify(manageCardMemberUseCase).assignMember(any());
    }
}
