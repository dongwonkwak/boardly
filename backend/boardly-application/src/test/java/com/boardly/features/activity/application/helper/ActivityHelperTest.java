package com.boardly.features.activity.application.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.boardly.features.activity.application.command.CreateActivityCommand;
import com.boardly.features.activity.application.usecase.CreateActivityUseCase;
import com.boardly.features.activity.domain.*;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.*;
import io.vavr.control.Either;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ActivityHelperTest {

  @Test
  @DisplayName("logActivitySync: 정상 호출 시 CreateActivityUseCase에 기대한 커맨드 전달")
  void logActivitySync_success() {
    CreateActivityUseCase useCase = mock(CreateActivityUseCase.class);
    ActivityHelper helper = new ActivityHelper(useCase);

    ActivityType type = ActivityType.CARD_MOVE;
    UserId actorId = new UserId("u-1");
    Map<String, Object> payload = Map.of("k", "v");
    String boardName = "Board";
    BoardId boardId = new BoardId("b-1");
    ListId listId = new ListId("l-1");
    CardId cardId = new CardId("c-1");

    when(useCase.createActivity(any(CreateActivityCommand.class)))
        .thenReturn(Either.right(Activity.create(
            type,
            Actor.of("1", "fn", "ln", ""),
            Payload.of(payload),
            boardName,
            boardId,
            listId,
            cardId
        )));

    helper.logActivitySync(type, actorId, payload, boardName, boardId, listId, cardId);

    ArgumentCaptor<CreateActivityCommand> captor = ArgumentCaptor.forClass(CreateActivityCommand.class);
    verify(useCase, times(1)).createActivity(captor.capture());
    CreateActivityCommand cmd = captor.getValue();

    assertEquals(type, cmd.type());
    assertEquals(actorId, cmd.actorId());
    assertEquals(boardName, cmd.boardName());
    assertEquals(boardId, cmd.boardId());
    assertEquals(listId, cmd.listId());
    assertEquals(cardId, cmd.cardId());
    assertEquals("v", cmd.payload().get("k"));
  }

  @Test
  @DisplayName("logActivitySync: 실패 반환에도 예외 없이 종료 (로깅만)")
  void logActivitySync_failureHandled() {
    CreateActivityUseCase useCase = mock(CreateActivityUseCase.class);
    ActivityHelper helper = new ActivityHelper(useCase);

    when(useCase.createActivity(any(CreateActivityCommand.class)))
        .thenReturn(Either.left(Failure.ofInternalServerError("boom")));

    assertDoesNotThrow(() -> helper.logActivitySync(
        ActivityType.BOARD_CREATE,
        new UserId("u-1"),
        Map.of("x", 1),
        "Board",
        new BoardId("b-1"),
        null,
        null
    ));

    verify(useCase, times(1)).createActivity(any(CreateActivityCommand.class));
  }
}
