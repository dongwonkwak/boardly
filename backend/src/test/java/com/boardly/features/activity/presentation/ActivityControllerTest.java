package com.boardly.features.activity.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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

import com.boardly.features.activity.application.port.input.GetActivityQuery;
import com.boardly.features.activity.application.port.output.ActivityListResponse;
import com.boardly.features.activity.application.port.output.ActivityResponse;
import com.boardly.features.activity.application.port.output.ActorResponse;
import com.boardly.features.activity.application.usecase.GetActivityUseCase;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.response.ErrorResponse;

import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityController 테스트")
class ActivityControllerTest {

    @Mock
    private GetActivityUseCase getActivityUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    @Mock
    private HttpServletRequest httpRequest;

    private ActivityController controller;

    @BeforeEach
    void setUp() {
        controller = new ActivityController(getActivityUseCase, failureHandler);
    }

    @Nested
    @DisplayName("보드 활동 목록 조회 테스트")
    class GetBoardActivitiesTest {

        @Test
        @DisplayName("보드 활동 목록 조회 성공")
        void getBoardActivities_Success() {
            // given
            String userId = "user-123";
            String boardId = "board-123";
            int page = 0;
            int size = 50;
            Instant since = Instant.parse("2024-01-01T00:00:00Z");

            ActivityListResponse expectedResponse = createMockActivityListResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            ResponseEntity<?> response = controller.getBoardActivities(
                    boardId, page, size, since, httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedResponse);

            // verify
            verify(getActivityUseCase).getActivities(any(GetActivityQuery.class));
        }

        @Test
        @DisplayName("기본 파라미터로 보드 활동 목록 조회 성공")
        void getBoardActivities_WithDefaultParameters_Success() {
            // given
            String userId = "user-123";
            String boardId = "board-123";

            ActivityListResponse expectedResponse = createMockActivityListResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            ResponseEntity<?> response = controller.getBoardActivities(
                    boardId, 0, 50, null, httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("페이징 파라미터로 보드 활동 목록 조회 성공")
        void getBoardActivities_WithPagingParameters_Success() {
            // given
            String userId = "user-123";
            String boardId = "board-123";
            int page = 2;
            int size = 20;

            ActivityListResponse expectedResponse = createMockActivityListResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            ResponseEntity<?> response = controller.getBoardActivities(
                    boardId, page, size, null, httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("보드 활동 목록 조회 실패 시 에러 응답 반환")
        void getBoardActivities_WhenFailure_ReturnsErrorResponse() {
            // given
            String userId = "user-123";
            String boardId = "board-123";
            Failure failure = Failure.ofNotFound("보드를 찾을 수 없습니다");
            ErrorResponse errorResponse = ErrorResponse.of("NOT_FOUND", "보드를 찾을 수 없습니다");

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.left(failure));
            when(failureHandler.handleFailure(failure))
                    .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));

            // when
            ResponseEntity<?> response = controller.getBoardActivities(
                    boardId, 0, 50, null, httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo(errorResponse);

            // verify
            verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("GetActivityQuery가 올바른 파라미터로 생성되는지 확인")
        void getBoardActivities_ShouldCreateCorrectQuery() {
            // given
            String userId = "user-123";
            String boardId = "board-123";
            int page = 1;
            int size = 25;
            Instant since = Instant.parse("2024-01-01T00:00:00Z");

            ActivityListResponse expectedResponse = createMockActivityListResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            controller.getBoardActivities(boardId, page, size, since, httpRequest, jwt);

            // then
            verify(getActivityUseCase).getActivities(eq(GetActivityQuery.builder()
                    .boardId(new BoardId(boardId))
                    .page(page)
                    .size(size)
                    .since(since)
                    .build()));
        }
    }

    @Nested
    @DisplayName("내 활동 목록 조회 테스트")
    class GetMyActivitiesTest {

        @Test
        @DisplayName("내 활동 목록 조회 성공")
        void getMyActivities_Success() {
            // given
            String userId = "user-123";
            int page = 0;
            int size = 50;

            ActivityListResponse expectedResponse = createMockActivityListResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            ResponseEntity<?> response = controller.getMyActivities(page, size, httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedResponse);

            // verify
            verify(getActivityUseCase).getActivities(any(GetActivityQuery.class));
        }

        @Test
        @DisplayName("기본 파라미터로 내 활동 목록 조회 성공")
        void getMyActivities_WithDefaultParameters_Success() {
            // given
            String userId = "user-123";

            ActivityListResponse expectedResponse = createMockActivityListResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            ResponseEntity<?> response = controller.getMyActivities(0, 50, httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("페이징 파라미터로 내 활동 목록 조회 성공")
        void getMyActivities_WithPagingParameters_Success() {
            // given
            String userId = "user-123";
            int page = 3;
            int size = 15;

            ActivityListResponse expectedResponse = createMockActivityListResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            ResponseEntity<?> response = controller.getMyActivities(page, size, httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("내 활동 목록 조회 실패 시 에러 응답 반환")
        void getMyActivities_WhenFailure_ReturnsErrorResponse() {
            // given
            String userId = "user-123";
            Failure failure = Failure.ofInputError("잘못된 요청입니다");
            ErrorResponse errorResponse = ErrorResponse.of("BAD_REQUEST", "잘못된 요청입니다");

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.left(failure));
            when(failureHandler.handleFailure(failure))
                    .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));

            // when
            ResponseEntity<?> response = controller.getMyActivities(0, 50, httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(errorResponse);

            // verify
            verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("GetActivityQuery가 올바른 파라미터로 생성되는지 확인")
        void getMyActivities_ShouldCreateCorrectQuery() {
            // given
            String userId = "user-123";
            int page = 2;
            int size = 30;

            ActivityListResponse expectedResponse = createMockActivityListResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            controller.getMyActivities(page, size, httpRequest, jwt);

            // then
            verify(getActivityUseCase).getActivities(eq(GetActivityQuery.builder()
                    .userId(new UserId(userId))
                    .page(page)
                    .size(size)
                    .build()));
        }
    }

    @Nested
    @DisplayName("JWT 토큰 처리 테스트")
    class JwtTokenTest {

        @Test
        @DisplayName("JWT에서 사용자 ID를 올바르게 추출하는지 확인")
        void shouldExtractUserIdFromJwt() {
            // given
            String userId = "user-456";
            String boardId = "board-123";

            ActivityListResponse expectedResponse = createMockActivityListResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getActivityUseCase.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            controller.getBoardActivities(boardId, 0, 50, null, httpRequest, jwt);

            // then
            verify(jwt).getSubject();
        }
    }

    private ActivityListResponse createMockActivityListResponse() {
        ActorResponse actor = ActorResponse.builder()
                .id("user-123")
                .firstName("홍")
                .lastName("길동")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        ActivityResponse activity = ActivityResponse.builder()
                .id("activity-123")
                .type(ActivityType.CARD_CREATE)
                .actor(actor)
                .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
                .payload(Map.of("title", "새 카드", "description", "카드 설명"))
                .build();

        return ActivityListResponse.builder()
                .activities(List.of(activity))
                .totalCount(1)
                .currentPage(0)
                .totalPages(1)
                .hasNextPage(false)
                .hasPreviousPage(false)
                .build();
    }
}