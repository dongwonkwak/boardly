package com.boardly.features.dashboard.presentation;

import com.boardly.features.activity.application.port.output.ActivityResponse;
import com.boardly.features.board.application.dto.BoardSummaryDto;
import com.boardly.features.dashboard.application.dto.DashboardResponse;
import com.boardly.features.dashboard.application.dto.DashboardStatisticsDto;
import com.boardly.features.dashboard.application.port.input.GetDashboardCommand;
import com.boardly.features.dashboard.application.usecase.GetDashboardUseCase;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController 테스트")
class DashboardControllerTest {

    @Mock
    private GetDashboardUseCase getDashboardUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    @Mock
    private HttpServletRequest httpRequest;

    private DashboardController controller;

    @BeforeEach
    void setUp() {
        controller = new DashboardController(getDashboardUseCase, failureHandler);
    }

    @Nested
    @DisplayName("대시보드 조회 테스트")
    class GetDashboardTest {

        @Test
        @DisplayName("대시보드 조회 성공")
        void getDashboard_Success() {
            // given
            String userId = "user-123";
            DashboardResponse expectedResponse = createMockDashboardResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getDashboardUseCase.getDashboard(any(GetDashboardCommand.class)))
                    .thenReturn(Either.right(expectedResponse));

            // when
            ResponseEntity<?> response = controller.getDashboard(httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedResponse);

            // verify
            verify(getDashboardUseCase).getDashboard(any(GetDashboardCommand.class));
        }

        @Test
        @DisplayName("대시보드 조회 실패 - 사용자를 찾을 수 없음")
        void getDashboard_UserNotFound() {
            // given
            String userId = "user-123";
            Failure failure = Failure.ofNotFound("사용자를 찾을 수 없습니다");
            ErrorResponse errorResponse = ErrorResponse.of("USER_NOT_FOUND", "사용자를 찾을 수 없습니다");

            when(jwt.getSubject()).thenReturn(userId);
            when(getDashboardUseCase.getDashboard(any(GetDashboardCommand.class)))
                    .thenReturn(Either.left(failure));
            when(failureHandler.handleFailure(failure))
                    .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));

            // when
            ResponseEntity<?> response = controller.getDashboard(httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo(errorResponse);

            // verify
            verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("대시보드 조회 실패 - 입력 데이터 유효하지 않음")
        void getDashboard_InvalidInput() {
            // given
            String userId = "user-123";
            Failure failure = Failure.ofInputError("입력 데이터가 유효하지 않습니다");
            ErrorResponse errorResponse = ErrorResponse.of("INVALID_INPUT", "입력 데이터가 유효하지 않습니다");

            when(jwt.getSubject()).thenReturn(userId);
            when(getDashboardUseCase.getDashboard(any(GetDashboardCommand.class)))
                    .thenReturn(Either.left(failure));
            when(failureHandler.handleFailure(failure))
                    .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));

            // when
            ResponseEntity<?> response = controller.getDashboard(httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(errorResponse);

            // verify
            verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("대시보드 조회 실패 - 권한 없음")
        void getDashboard_PermissionDenied() {
            // given
            String userId = "user-123";
            Failure failure = Failure.ofForbidden("대시보드 조회 권한이 없습니다");
            ErrorResponse errorResponse = ErrorResponse.of("PERMISSION_DENIED", "대시보드 조회 권한이 없습니다");

            when(jwt.getSubject()).thenReturn(userId);
            when(getDashboardUseCase.getDashboard(any(GetDashboardCommand.class)))
                    .thenReturn(Either.left(failure));
            when(failureHandler.handleFailure(failure))
                    .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));

            // when
            ResponseEntity<?> response = controller.getDashboard(httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isEqualTo(errorResponse);

            // verify
            verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("대시보드 조회 실패 - 내부 서버 오류")
        void getDashboard_InternalServerError() {
            // given
            String userId = "user-123";
            Failure failure = Failure.ofInternalServerError("내부 서버 오류가 발생했습니다");
            ErrorResponse errorResponse = ErrorResponse.of("INTERNAL_ERROR", "내부 서버 오류가 발생했습니다");

            when(jwt.getSubject()).thenReturn(userId);
            when(getDashboardUseCase.getDashboard(any(GetDashboardCommand.class)))
                    .thenReturn(Either.left(failure));
            when(failureHandler.handleFailure(failure))
                    .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(errorResponse));

            // when
            ResponseEntity<?> response = controller.getDashboard(httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isEqualTo(errorResponse);

            // verify
            verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("대시보드 조회 실패 - 비즈니스 룰 위반")
        void getDashboard_BusinessRuleViolation() {
            // given
            String userId = "user-123";
            Failure failure = Failure.ofBusinessRuleViolation("비즈니스 룰을 위반했습니다");
            ErrorResponse errorResponse = ErrorResponse.of("BUSINESS_RULE_VIOLATION", "비즈니스 룰을 위반했습니다");

            when(jwt.getSubject()).thenReturn(userId);
            when(getDashboardUseCase.getDashboard(any(GetDashboardCommand.class)))
                    .thenReturn(Either.left(failure));
            when(failureHandler.handleFailure(failure))
                    .thenReturn(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .body(errorResponse));

            // when
            ResponseEntity<?> response = controller.getDashboard(httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isEqualTo(errorResponse);

            // verify
            verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("빈 대시보드 조회 성공")
        void getDashboard_EmptyDashboard() {
            // given
            String userId = "user-123";
            DashboardResponse emptyResponse = createEmptyDashboardResponse();

            when(jwt.getSubject()).thenReturn(userId);
            when(getDashboardUseCase.getDashboard(any(GetDashboardCommand.class)))
                    .thenReturn(Either.right(emptyResponse));

            // when
            ResponseEntity<?> response = controller.getDashboard(httpRequest, jwt);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            DashboardResponse responseBody = (DashboardResponse) response.getBody();
            assertThat(responseBody.getBoards()).isEmpty();
            assertThat(responseBody.getRecentActivity()).isEmpty();
            assertThat(responseBody.getStatistics().getTotalBoards()).isEqualTo(0);
            assertThat(responseBody.getStatistics().getTotalCards()).isEqualTo(0);
            assertThat(responseBody.getStatistics().getStarredBoards()).isEqualTo(0);
            assertThat(responseBody.getStatistics().getArchivedBoards()).isEqualTo(0);

            // verify
            verify(getDashboardUseCase).getDashboard(any(GetDashboardCommand.class));
        }
    }

    // ==================== HELPER METHODS ====================

    private DashboardResponse createMockDashboardResponse() {
        List<BoardSummaryDto> boards = List.of(
                BoardSummaryDto.builder()
                        .id("board-1")
                        .title("테스트 보드 1")
                        .description("테스트 보드 1 설명")
                        .createdAt(Instant.now())
                        .listCount(3)
                        .cardCount(10)
                        .isStarred(true)
                        .color("blue")
                        .role("owner")
                        .build(),
                BoardSummaryDto.builder()
                        .id("board-2")
                        .title("테스트 보드 2")
                        .description("테스트 보드 2 설명")
                        .createdAt(Instant.now())
                        .listCount(2)
                        .cardCount(5)
                        .isStarred(false)
                        .color("green")
                        .role("member")
                        .build());

        List<ActivityResponse> activities = List.of(
                ActivityResponse.builder()
                        .id("activity-1")
                        .type(com.boardly.features.activity.domain.model.ActivityType.CARD_CREATE)
                        .actor(com.boardly.features.activity.application.port.output.ActorResponse.builder()
                                .id("user-123")
                                .firstName("홍")
                                .lastName("길동")
                                .profileImageUrl("https://example.com/profile.jpg")
                                .build())
                        .timestamp(Instant.now())
                        .payload(java.util.Map.of("title", "새로운 카드"))
                        .boardName("프로젝트 A")
                        .boardId("board_123")
                        .build(),
                ActivityResponse.builder()
                        .id("activity-2")
                        .type(com.boardly.features.activity.domain.model.ActivityType.BOARD_UPDATE_DESCRIPTION)
                        .actor(com.boardly.features.activity.application.port.output.ActorResponse.builder()
                                .id("user-123")
                                .firstName("홍")
                                .lastName("길동")
                                .profileImageUrl("https://example.com/profile.jpg")
                                .build())
                        .timestamp(Instant.now())
                        .payload(java.util.Map.of("description", "보드 설명 업데이트"))
                        .boardName("프로젝트 B")
                        .boardId("board_456")
                        .build());

        DashboardStatisticsDto statistics = DashboardStatisticsDto.builder()
                .totalBoards(2)
                .totalCards(15)
                .starredBoards(1)
                .archivedBoards(0)
                .build();

        return DashboardResponse.builder()
                .boards(boards)
                .recentActivity(activities)
                .statistics(statistics)
                .build();
    }

    private DashboardResponse createEmptyDashboardResponse() {
        DashboardStatisticsDto emptyStatistics = DashboardStatisticsDto.builder()
                .totalBoards(0)
                .totalCards(0)
                .starredBoards(0)
                .archivedBoards(0)
                .build();

        return DashboardResponse.builder()
                .boards(List.of())
                .recentActivity(List.of())
                .statistics(emptyStatistics)
                .build();
    }
}