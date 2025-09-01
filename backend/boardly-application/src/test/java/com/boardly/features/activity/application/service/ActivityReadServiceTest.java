package com.boardly.features.activity.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boardly.features.activity.application.dto.ActivityListResponse;
import com.boardly.features.activity.application.dto.ActivityResponse;
import com.boardly.features.activity.application.query.GetActivityQuery;
import com.boardly.features.activity.domain.*;
import com.boardly.features.activity.domain.port.ActivityRepository;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.*;
import com.boardly.shared.validation.MessageResolver;
import io.vavr.control.Either;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityReadServiceTest {

  @Mock private ActivityRepository activityRepository;
  @Mock private BoardRepository boardRepository;
  @Mock private MessageResolver messageResolver;

  @InjectMocks private ActivityReadService service;

  private BoardId boardId;
  private UserId userId;

  @BeforeEach
  void init() {
    boardId = new BoardId("board-1");
    userId = new UserId("user-1");
  }

  private Activity newActivity(ActivityType type) {
    return Activity.create(
        type,
        Actor.of("1", "a", "b", ""),
        Payload.of(java.util.Map.of("k", "v")),
        "Board",
        boardId,
        new ListId("list-1"),
        new CardId("card-1")
    );
  }

  @Test
  @DisplayName("보드 ID 기준 기본 조회(페이징 X)")
  void getActivities_forBoard_noPaging() {
    GetActivityQuery query = GetActivityQuery.forBoard(boardId);
    List<Activity> activities = List.of(newActivity(ActivityType.CARD_CREATE));
    when(activityRepository.findByBoardIdOrderByTimestampDesc(boardId)).thenReturn(activities);
    when(boardRepository.findBoardNameById(boardId)).thenReturn(Optional.of("B1"));

    Either<Failure, ActivityListResponse> result = service.getActivities(query);

    assertTrue(result.isRight());
    ActivityListResponse resp = result.get();
    assertEquals(1, resp.activities().size());
    ActivityResponse ar = resp.activities().get(0);
    assertEquals("B1", ar.getBoardName());
    assertEquals(boardId.getId(), ar.getBoardId());
  }

  @Test
  @DisplayName("보드 ID + since + 페이징")
  void getActivities_forBoard_sinceWithPaging() {
    Instant since = Instant.now().minusSeconds(3600);
    GetActivityQuery query = new GetActivityQuery(null, boardId, since, null, 0, 10);
    List<Activity> activities = List.of(newActivity(ActivityType.CARD_MOVE));
    when(activityRepository.findByBoardIdAndTimestampAfter(boardId, since, 0, 10))
        .thenReturn(activities);
    when(activityRepository.countByBoardIdAndTimestampAfter(boardId, since)).thenReturn(1L);

    Either<Failure, ActivityListResponse> result = service.getActivities(query);

    assertTrue(result.isRight());
    ActivityListResponse resp = result.get();
    assertEquals(1, resp.totalCount());
    assertEquals(1, resp.totalPages());
    assertFalse(resp.hasNextPage());
  }

  @Test
  @DisplayName("사용자 ID 기준 조회 + 페이징 X")
  void getActivities_forUser_noPaging() {
    GetActivityQuery query = GetActivityQuery.forUser(userId);
    List<Activity> activities = List.of(newActivity(ActivityType.CARD_RENAME));
    when(activityRepository.findByActorIdOrderByTimestampDesc(userId)).thenReturn(activities);

    Either<Failure, ActivityListResponse> result = service.getActivities(query);

    assertTrue(result.isRight());
    assertEquals(1, result.get().activities().size());
  }

  @Test
  @DisplayName("쿼리 유효성: boardId/userId 모두 null이면 400 리턴")
  void getActivities_invalidQuery() {
    GetActivityQuery query = new GetActivityQuery(null, null, null, null, 0, 50);
    when(messageResolver.getDomainValidationMessageWithDefault(eq("activity"), eq("query"), eq("invalid"), anyString()))
        .thenReturn("보드 ID 또는 사용자 ID 중 하나는 필수입니다.");

    Either<Failure, ActivityListResponse> result = service.getActivities(query);

    assertTrue(result.isLeft());
    assertInstanceOf(Failure.InputError.class, result.getLeft());
  }

  @Test
  @DisplayName("예외 발생 시 500으로 매핑")
  void getActivities_exceptionHandled() {
    GetActivityQuery query = GetActivityQuery.forBoard(boardId);
    when(activityRepository.findByBoardIdOrderByTimestampDesc(boardId))
        .thenThrow(new RuntimeException("boom"));
    when(messageResolver.getMessageWithDefault(anyString(), anyString()))
        .thenReturn("활동 목록 조회 중 오류가 발생했습니다.");

    Either<Failure, ActivityListResponse> result = service.getActivities(query);

    assertTrue(result.isLeft());
    assertInstanceOf(Failure.InternalError.class, result.getLeft());
  }
}
