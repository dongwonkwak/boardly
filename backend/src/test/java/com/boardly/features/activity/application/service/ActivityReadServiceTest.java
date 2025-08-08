package com.boardly.features.activity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boardly.features.activity.application.port.input.GetActivityQuery;
import com.boardly.features.activity.application.port.output.ActivityListResponse;
import com.boardly.features.activity.application.port.output.ActivityResponse;
import com.boardly.features.activity.application.port.output.ActorResponse;
import com.boardly.features.activity.domain.model.Activity;
import com.boardly.features.activity.domain.model.ActivityId;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.activity.domain.model.Actor;
import com.boardly.features.activity.domain.model.Payload;
import com.boardly.features.activity.domain.repository.ActivityRepository;
import com.boardly.features.board.application.dto.BoardNameDto;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityReadService 테스트")
class ActivityReadServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MessageSource messageSource;

    private ActivityReadService activityReadService;
    private ValidationMessageResolver messageResolver;

    @BeforeEach
    void setUp() {
        messageResolver = new ValidationMessageResolver(messageSource);
        activityReadService = new ActivityReadService(
            activityRepository,
            boardRepository,
            messageResolver
        );
    }

    @Nested
    @DisplayName("getActivities 성공 테스트")
    class GetActivitiesSuccessTest {

        @Test
        @DisplayName("보드 ID로 활동 목록 조회가 성공해야 한다")
        void getActivities_WithBoardId_ShouldSucceed() {
            // given
            BoardId boardId = new BoardId("board-123");
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                null,
                null,
                0,
                10
            );

            List<Activity> mockActivities = createMockActivities();
            when(
                activityRepository.findByBoardIdOrderByTimestampDesc(
                    boardId,
                    0,
                    10
                )
            ).thenReturn(mockActivities);
            when(activityRepository.countByBoardId(boardId)).thenReturn(2L);

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isRight()).isTrue();
            ActivityListResponse response = result.get();
            assertThat(response.activities()).hasSize(2);
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.currentPage()).isEqualTo(0);
            assertThat(response.totalPages()).isEqualTo(1);

            verify(activityRepository).findByBoardIdOrderByTimestampDesc(
                boardId,
                0,
                10
            );
            verify(activityRepository).countByBoardId(boardId);
        }

        @Test
        @DisplayName("사용자 ID로 활동 목록 조회가 성공해야 한다")
        void getActivities_WithUserId_ShouldSucceed() {
            // given
            UserId userId = new UserId("user-123");
            GetActivityQuery query = new GetActivityQuery(
                userId,
                null,
                null,
                null,
                0,
                10
            );

            List<Activity> mockActivities = createMockActivities();
            when(
                activityRepository.findByActorIdOrderByTimestampDesc(
                    userId,
                    0,
                    10
                )
            ).thenReturn(mockActivities);
            when(activityRepository.countByActorId(userId)).thenReturn(2L);

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isRight()).isTrue();
            ActivityListResponse response = result.get();
            assertThat(response.activities()).hasSize(2);
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.currentPage()).isEqualTo(0);
            assertThat(response.totalPages()).isEqualTo(1);

            verify(activityRepository).findByActorIdOrderByTimestampDesc(
                userId,
                0,
                10
            );
            verify(activityRepository).countByActorId(userId);
        }

        @Test
        @DisplayName("페이징이 포함된 보드 활동 조회가 성공해야 한다")
        void getActivities_WithBoardIdAndPaging_ShouldSucceed() {
            // given
            BoardId boardId = new BoardId("board-123");
            int page = 1;
            int size = 10;
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                null,
                null,
                page,
                size
            );

            List<Activity> mockActivities = createMockActivities();
            when(
                activityRepository.findByBoardIdOrderByTimestampDesc(
                    boardId,
                    page,
                    size
                )
            ).thenReturn(mockActivities);
            when(activityRepository.countByBoardId(boardId)).thenReturn(25L);

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isRight()).isTrue();
            ActivityListResponse response = result.get();
            assertThat(response.activities()).hasSize(2);
            assertThat(response.totalCount()).isEqualTo(25);
            assertThat(response.currentPage()).isEqualTo(page);
            assertThat(response.totalPages()).isEqualTo(3);

            verify(activityRepository).findByBoardIdOrderByTimestampDesc(
                boardId,
                page,
                size
            );
            verify(activityRepository).countByBoardId(boardId);
        }

        @Test
        @DisplayName("페이징이 포함된 사용자 활동 조회가 성공해야 한다")
        void getActivities_WithUserIdAndPaging_ShouldSucceed() {
            // given
            UserId userId = new UserId("user-123");
            int page = 2;
            int size = 20;
            GetActivityQuery query = new GetActivityQuery(
                userId,
                null,
                null,
                null,
                page,
                size
            );

            List<Activity> mockActivities = createMockActivities();
            when(
                activityRepository.findByActorIdOrderByTimestampDesc(
                    userId,
                    page,
                    size
                )
            ).thenReturn(mockActivities);
            when(activityRepository.countByActorId(userId)).thenReturn(50L);

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isRight()).isTrue();
            ActivityListResponse response = result.get();
            assertThat(response.activities()).hasSize(2);
            assertThat(response.totalCount()).isEqualTo(50);
            assertThat(response.currentPage()).isEqualTo(page);
            assertThat(response.totalPages()).isEqualTo(3);

            verify(activityRepository).findByActorIdOrderByTimestampDesc(
                userId,
                page,
                size
            );
            verify(activityRepository).countByActorId(userId);
        }

        @Test
        @DisplayName("시간 범위가 포함된 보드 활동 조회가 성공해야 한다")
        void getActivities_WithBoardIdAndTimeRange_ShouldSucceed() {
            // given
            BoardId boardId = new BoardId("board-123");
            Instant since = Instant.now().minusSeconds(3600);
            Instant until = Instant.now();
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                since,
                until,
                0,
                50
            );

            List<Activity> mockActivities = createMockActivities();
            when(
                activityRepository.findByBoardIdAndTimestampBetween(
                    boardId,
                    since,
                    until
                )
            ).thenReturn(mockActivities);

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isRight()).isTrue();
            ActivityListResponse response = result.get();
            assertThat(response.activities()).hasSize(2);
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.currentPage()).isEqualTo(0);
            assertThat(response.totalPages()).isEqualTo(1);

            verify(activityRepository).findByBoardIdAndTimestampBetween(
                boardId,
                since,
                until
            );
        }

        @Test
        @DisplayName("시작 시간만 포함된 보드 활동 조회가 성공해야 한다")
        void getActivities_WithBoardIdAndSinceOnly_ShouldSucceed() {
            // given
            BoardId boardId = new BoardId("board-123");
            Instant since = Instant.now().minusSeconds(3600);
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                since,
                null,
                0,
                50
            );

            List<Activity> mockActivities = createMockActivities();
            when(
                activityRepository.findByBoardIdAndTimestampAfter(
                    boardId,
                    since
                )
            ).thenReturn(mockActivities);

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isRight()).isTrue();
            ActivityListResponse response = result.get();
            assertThat(response.activities()).hasSize(2);
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.currentPage()).isEqualTo(0);
            assertThat(response.totalPages()).isEqualTo(1);

            verify(activityRepository).findByBoardIdAndTimestampAfter(
                boardId,
                since
            );
        }

        @Test
        @DisplayName("기본 페이징 값으로 조회가 성공해야 한다")
        void getActivities_WithDefaultPaging_ShouldSucceed() {
            // given
            BoardId boardId = new BoardId("board-123");
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                null,
                null,
                0,
                50
            );

            List<Activity> mockActivities = createMockActivities();
            // page(0), size(50)은 기본값이므로 페이징이 없는 것으로 간주되어 of()가 호출됨
            when(
                activityRepository.findByBoardIdOrderByTimestampDesc(boardId)
            ).thenReturn(mockActivities);

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isRight()).isTrue();
            ActivityListResponse response = result.get();
            assertThat(response.activities()).hasSize(2);
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.currentPage()).isEqualTo(0);
            assertThat(response.totalPages()).isEqualTo(1);

            verify(activityRepository).findByBoardIdOrderByTimestampDesc(
                boardId
            );
        }
    }

    @Nested
    @DisplayName("getActivities 실패 테스트")
    class GetActivitiesFailureTest {

        @Test
        @DisplayName("보드 ID와 사용자 ID가 모두 없을 때 실패를 반환해야 한다")
        void getActivities_WithNoBoardIdAndNoUserId_ShouldReturnFailure() {
            // given
            GetActivityQuery query = new GetActivityQuery(
                null,
                null,
                null,
                null,
                0,
                50
            );

            when(
                messageSource.getMessage(
                    eq("validation.activity.query.invalid"),
                    any(Object[].class),
                    anyString(),
                    any(Locale.class)
                )
            ).thenReturn("보드 ID 또는 사용자 ID 중 하나는 필수입니다.");

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);
            assertThat(failure.getMessage()).contains(
                "보드 ID 또는 사용자 ID 중 하나는 필수입니다"
            );
        }

        @Test
        @DisplayName(
            "보드 ID와 사용자 ID가 모두 null일 때 실패를 반환해야 한다"
        )
        void getActivities_WithNullBoardIdAndNullUserId_ShouldReturnFailure() {
            // given
            GetActivityQuery query = new GetActivityQuery(
                null,
                null,
                null,
                null,
                0,
                50
            );

            when(
                messageSource.getMessage(
                    eq("validation.activity.query.invalid"),
                    any(Object[].class),
                    anyString(),
                    any(Locale.class)
                )
            ).thenReturn("보드 ID 또는 사용자 ID 중 하나는 필수입니다.");

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);
            assertThat(failure.getMessage()).contains(
                "보드 ID 또는 사용자 ID 중 하나는 필수입니다"
            );
        }

        @Test
        @DisplayName("예외 발생 시 실패를 반환해야 한다")
        void getActivities_WithException_ShouldReturnFailure() {
            // given
            BoardId boardId = new BoardId("board-123");
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                null,
                null,
                0,
                10
            );

            when(
                activityRepository.findByBoardIdOrderByTimestampDesc(
                    boardId,
                    0,
                    10
                )
            ).thenThrow(new RuntimeException("Database error"));
            when(
                messageSource.getMessage(
                    eq("activity.read.error"),
                    any(Object[].class),
                    anyString(),
                    any(Locale.class)
                )
            ).thenReturn("활동 목록 조회 중 오류가 발생했습니다.");

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InternalError.class);
            assertThat(failure.getMessage()).contains(
                "활동 목록 조회 중 오류가 발생했습니다"
            );
        }
    }

    @Nested
    @DisplayName("ActivityResponse 변환 테스트")
    class ActivityResponseConversionTest {

        @Test
        @DisplayName("Activity가 올바르게 ActivityResponse로 변환되어야 한다")
        void activity_ShouldBeConvertedToActivityResponseCorrectly() {
            // given
            BoardId boardId = new BoardId("board-123");
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                null,
                null,
                0,
                10
            );

            Activity mockActivity = createMockActivity();
            List<Activity> mockActivities = List.of(mockActivity);
            when(
                activityRepository.findByBoardIdOrderByTimestampDesc(
                    boardId,
                    0,
                    10
                )
            ).thenReturn(mockActivities);
            when(activityRepository.countByBoardId(boardId)).thenReturn(1L);

            // BoardRepository 모킹 추가
            when(boardRepository.findBoardNameById(boardId)).thenReturn(
                Optional.of(new BoardNameDto("프로젝트 A"))
            );

            // when
            Either<Failure, ActivityListResponse> result =
                activityReadService.getActivities(query);

            // then
            assertThat(result.isRight()).isTrue();
            ActivityListResponse response = result.get();
            assertThat(response.activities()).hasSize(1);

            ActivityResponse activityResponse = response.activities().get(0);
            assertThat(activityResponse.getId()).isEqualTo("activity-123");
            assertThat(activityResponse.getType()).isEqualTo(
                ActivityType.CARD_CREATE
            );
            assertThat(activityResponse.getTimestamp()).isEqualTo(
                Instant.parse("2024-01-01T00:00:00Z")
            );
            assertThat(activityResponse.getBoardId()).isEqualTo("board-123");
            assertThat(activityResponse.getBoardName()).isEqualTo("프로젝트 A");

            ActorResponse actorResponse = activityResponse.getActor();
            assertThat(actorResponse.getId()).isEqualTo("user-123");
            assertThat(actorResponse.getFirstName()).isEqualTo("홍");
            assertThat(actorResponse.getLastName()).isEqualTo("길동");
            assertThat(actorResponse.getProfileImageUrl()).isEqualTo(
                "https://example.com/profile.jpg"
            );

            Map<String, Object> payload = activityResponse.getPayload();
            assertThat(payload).containsEntry("title", "새 카드");
            assertThat(payload).containsEntry("description", "카드 설명");
        }
    }

    // 헬퍼 메서드들
    private List<Activity> createMockActivities() {
        return Arrays.asList(createMockActivity(), createMockActivity2());
    }

    private Activity createMockActivity() {
        ActivityId activityId = new ActivityId("activity-123");
        UserId actorId = new UserId("user-123");
        BoardId boardId = new BoardId("board-123");
        Actor actor = Actor.builder()
            .id(actorId.getId())
            .firstName("홍")
            .lastName("길동")
            .profileImageUrl("https://example.com/profile.jpg")
            .build();

        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("title", "새 카드");
        payloadData.put("description", "카드 설명");
        Payload payload = Payload.of(payloadData);

        return Activity.builder()
            .id(activityId)
            .type(ActivityType.CARD_CREATE)
            .actor(actor)
            .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
            .payload(payload)
            .boardId(boardId)
            .build();
    }

    private Activity createMockActivity2() {
        ActivityId activityId = new ActivityId("activity-456");
        UserId actorId = new UserId("user-456");
        BoardId boardId = new BoardId("board-456");
        Actor actor = Actor.builder()
            .id(actorId.getId())
            .firstName("김")
            .lastName("철수")
            .profileImageUrl("https://example.com/profile2.jpg")
            .build();

        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("listName", "새 리스트");
        Payload payload = Payload.of(payloadData);

        return Activity.builder()
            .id(activityId)
            .type(ActivityType.LIST_CREATE)
            .actor(actor)
            .timestamp(Instant.parse("2024-01-02T00:00:00Z"))
            .payload(payload)
            .boardId(boardId)
            .build();
    }
}
