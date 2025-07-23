package com.boardly.features.activity.application.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.activity.application.port.input.CreateActivityCommand;
import com.boardly.features.activity.application.usecase.CreateActivityUseCase;
import com.boardly.features.activity.domain.model.Activity;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.activity.domain.model.Actor;
import com.boardly.features.activity.domain.model.Payload;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.features.user.domain.model.UserId;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityHelper 테스트")
class ActivityHelperTest {

    @Mock
    private CreateActivityUseCase createActivityUseCase;

    @Captor
    private ArgumentCaptor<CreateActivityCommand> commandCaptor;

    private ActivityHelper activityHelper;

    @BeforeEach
    void setUp() {
        activityHelper = new ActivityHelper(createActivityUseCase);
    }

    @Nested
    @DisplayName("logActivity 테스트")
    class LogActivityTest {

        @Test
        @DisplayName("성공적인 활동 로그 생성")
        void logActivity_WithSuccess_ShouldCreateActivity() {
            // given
            ActivityType type = ActivityType.CARD_CREATE;
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "새 카드");

            Activity expectedActivity = createMockActivity(type, actorId, payload, boardId, listId, cardId);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logActivity(type, actorId, payload, boardId, listId, cardId);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();

            assertThat(capturedCommand.type()).isEqualTo(type);
            assertThat(capturedCommand.actorId()).isEqualTo(actorId);
            assertThat(capturedCommand.payload()).isEqualTo(payload);
            assertThat(capturedCommand.boardId()).isEqualTo(boardId);
            assertThat(capturedCommand.listId()).isEqualTo(listId);
            assertThat(capturedCommand.cardId()).isEqualTo(cardId);
        }

        @Test
        @DisplayName("활동 생성 실패 시 에러 로그만 기록")
        void logActivity_WithFailure_ShouldLogError() {
            // given
            ActivityType type = ActivityType.CARD_CREATE;
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "새 카드");

            Failure failure = Failure.ofInputError("활동 생성 실패");
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.left(failure));

            // when
            activityHelper.logActivity(type, actorId, payload, boardId, listId, cardId);

            // then
            verify(createActivityUseCase).createActivity(any(CreateActivityCommand.class));
        }

        @Test
        @DisplayName("예외 발생 시 에러 로그만 기록")
        void logActivity_WithException_ShouldLogError() {
            // given
            ActivityType type = ActivityType.CARD_CREATE;
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "새 카드");

            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenThrow(new RuntimeException("예상치 못한 오류"));

            // when
            activityHelper.logActivity(type, actorId, payload, boardId, listId, cardId);

            // then
            verify(createActivityUseCase).createActivity(any(CreateActivityCommand.class));
        }
    }

    @Nested
    @DisplayName("logActivitySync 테스트")
    class LogActivitySyncTest {

        @Test
        @DisplayName("동기 활동 로그 생성 성공")
        void logActivitySync_WithSuccess_ShouldCreateActivity() {
            // given
            ActivityType type = ActivityType.CARD_CREATE;
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "새 카드");

            Activity expectedActivity = createMockActivity(type, actorId, payload, boardId, listId, cardId);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logActivitySync(type, actorId, payload, boardId, listId, cardId);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();

            assertThat(capturedCommand.type()).isEqualTo(type);
            assertThat(capturedCommand.actorId()).isEqualTo(actorId);
            assertThat(capturedCommand.payload()).isEqualTo(payload);
        }
    }

    @Nested
    @DisplayName("logCardCreate 테스트")
    class LogCardCreateTest {

        @Test
        @DisplayName("카드 생성 활동 로그 생성")
        void logCardCreate_ShouldCreateCardCreateActivity() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            String listName = "할 일";
            String cardTitle = "새 카드";

            Activity expectedActivity = createMockActivity(ActivityType.CARD_CREATE, actorId,
                    Map.of("listName", listName, "cardTitle", cardTitle, "listId", listId.getId(), "cardId",
                            cardId.getId()),
                    boardId, listId, cardId);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logCardCreate(actorId, listName, cardTitle, boardId, listId, cardId);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();

            assertThat(capturedCommand.type()).isEqualTo(ActivityType.CARD_CREATE);
            assertThat(capturedCommand.payload()).containsEntry("listName", listName);
            assertThat(capturedCommand.payload()).containsEntry("cardTitle", cardTitle);
            assertThat(capturedCommand.payload()).containsEntry("listId", listId.getId());
            assertThat(capturedCommand.payload()).containsEntry("cardId", cardId.getId());
        }
    }

    @Nested
    @DisplayName("logCardMove 테스트")
    class LogCardMoveTest {

        @Test
        @DisplayName("카드 이동 활동 로그 생성")
        void logCardMove_ShouldCreateCardMoveActivity() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId sourceListId = new ListId("list-123");
            ListId destListId = new ListId("list-456");
            CardId cardId = new CardId("card-123");
            String cardTitle = "이동할 카드";
            String sourceListName = "할 일";
            String destListName = "진행 중";

            Activity expectedActivity = createMockActivity(ActivityType.CARD_MOVE, actorId,
                    Map.of("cardTitle", cardTitle, "sourceListName", sourceListName, "destListName", destListName,
                            "cardId", cardId.getId(), "sourceListId", sourceListId.getId(), "destListId",
                            destListId.getId()),
                    boardId, destListId, cardId);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logCardMove(actorId, cardTitle, sourceListName, destListName,
                    boardId, sourceListId, destListId, cardId);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();

            assertThat(capturedCommand.type()).isEqualTo(ActivityType.CARD_MOVE);
            assertThat(capturedCommand.payload()).containsEntry("cardTitle", cardTitle);
            assertThat(capturedCommand.payload()).containsEntry("sourceListName", sourceListName);
            assertThat(capturedCommand.payload()).containsEntry("destListName", destListName);
            assertThat(capturedCommand.listId()).isEqualTo(destListId); // 목적지 리스트 ID가 설정되어야 함
        }
    }

    @Nested
    @DisplayName("logListCreate 테스트")
    class LogListCreateTest {

        @Test
        @DisplayName("리스트 생성 활동 로그 생성")
        void logListCreate_ShouldCreateListCreateActivity() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            String listName = "새 리스트";
            String boardName = "프로젝트 보드";

            Activity expectedActivity = createMockActivity(ActivityType.LIST_CREATE, actorId,
                    Map.of("listName", listName, "listId", listId.getId(), "boardName", boardName),
                    boardId, listId, null);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logListCreate(actorId, listName, boardName, boardId, listId);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();

            assertThat(capturedCommand.type()).isEqualTo(ActivityType.LIST_CREATE);
            assertThat(capturedCommand.payload()).containsEntry("listName", listName);
            assertThat(capturedCommand.payload()).containsEntry("boardName", boardName);
            assertThat(capturedCommand.cardId()).isNull();
        }
    }

    @Nested
    @DisplayName("logBoardCreate 테스트")
    class LogBoardCreateTest {

        @Test
        @DisplayName("보드 생성 활동 로그 생성")
        void logBoardCreate_ShouldCreateBoardCreateActivity() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            String boardName = "새 보드";

            Activity expectedActivity = createMockActivity(ActivityType.BOARD_CREATE, actorId,
                    Map.of("boardName", boardName, "boardId", boardId.getId()),
                    boardId, null, null);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logBoardCreate(actorId, boardName, boardId);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();

            assertThat(capturedCommand.type()).isEqualTo(ActivityType.BOARD_CREATE);
            assertThat(capturedCommand.payload()).containsEntry("boardName", boardName);
            assertThat(capturedCommand.payload()).containsEntry("boardId", boardId.getId());
            assertThat(capturedCommand.listId()).isNull();
            assertThat(capturedCommand.cardId()).isNull();
        }
    }

    @Nested
    @DisplayName("logCardActivity 테스트")
    class LogCardActivityTest {

        @Test
        @DisplayName("유효한 카드 활동 타입으로 활동 로그 생성")
        void logCardActivity_WithValidCardType_ShouldCreateActivity() {
            // given
            ActivityType type = ActivityType.CARD_RENAME;
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("oldTitle", "이전 제목", "newTitle", "새 제목");

            Activity expectedActivity = createMockActivity(type, actorId, payload, boardId, listId, cardId);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logCardActivity(type, actorId, payload, boardId, listId, cardId);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.type()).isEqualTo(type);
        }

        @Test
        @DisplayName("유효하지 않은 카드 활동 타입으로 호출 시 경고 로그만 기록")
        void logCardActivity_WithInvalidCardType_ShouldLogWarning() {
            // given
            ActivityType type = ActivityType.BOARD_CREATE; // 카드 활동이 아닌 타입
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "테스트");

            // when
            activityHelper.logCardActivity(type, actorId, payload, boardId, listId, cardId);

            // then
            verify(createActivityUseCase, times(0)).createActivity(any(CreateActivityCommand.class));
        }
    }

    @Nested
    @DisplayName("logListActivity 테스트")
    class LogListActivityTest {

        @Test
        @DisplayName("유효한 리스트 활동 타입으로 활동 로그 생성")
        void logListActivity_WithValidListType_ShouldCreateActivity() {
            // given
            ActivityType type = ActivityType.LIST_RENAME;
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            Map<String, Object> payload = Map.of("oldName", "이전 이름", "newName", "새 이름");

            Activity expectedActivity = createMockActivity(type, actorId, payload, boardId, listId, null);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logListActivity(type, actorId, payload, boardId, listId);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.type()).isEqualTo(type);
            assertThat(capturedCommand.cardId()).isNull();
        }

        @Test
        @DisplayName("유효하지 않은 리스트 활동 타입으로 호출 시 경고 로그만 기록")
        void logListActivity_WithInvalidListType_ShouldLogWarning() {
            // given
            ActivityType type = ActivityType.CARD_CREATE; // 리스트 활동이 아닌 타입
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            Map<String, Object> payload = Map.of("title", "테스트");

            // when
            activityHelper.logListActivity(type, actorId, payload, boardId, listId);

            // then
            verify(createActivityUseCase, times(0)).createActivity(any(CreateActivityCommand.class));
        }
    }

    @Nested
    @DisplayName("logBoardActivity 테스트")
    class LogBoardActivityTest {

        @Test
        @DisplayName("유효한 보드 활동 타입으로 활동 로그 생성")
        void logBoardActivity_WithValidBoardType_ShouldCreateActivity() {
            // given
            ActivityType type = ActivityType.BOARD_RENAME;
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            Map<String, Object> payload = Map.of("oldName", "이전 이름", "newName", "새 이름");

            Activity expectedActivity = createMockActivity(type, actorId, payload, boardId, null, null);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logBoardActivity(type, actorId, payload, boardId);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.type()).isEqualTo(type);
            assertThat(capturedCommand.listId()).isNull();
            assertThat(capturedCommand.cardId()).isNull();
        }

        @Test
        @DisplayName("유효하지 않은 보드 활동 타입으로 호출 시 경고 로그만 기록")
        void logBoardActivity_WithInvalidBoardType_ShouldLogWarning() {
            // given
            ActivityType type = ActivityType.CARD_CREATE; // 보드 활동이 아닌 타입
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            Map<String, Object> payload = Map.of("title", "테스트");

            // when
            activityHelper.logBoardActivity(type, actorId, payload, boardId);

            // then
            verify(createActivityUseCase, times(0)).createActivity(any(CreateActivityCommand.class));
        }
    }

    @Nested
    @DisplayName("logUserActivity 테스트")
    class LogUserActivityTest {

        @Test
        @DisplayName("유효한 사용자 활동 타입으로 활동 로그 생성")
        void logUserActivity_WithValidUserType_ShouldCreateActivity() {
            // given
            ActivityType type = ActivityType.USER_UPDATE_PROFILE;
            UserId actorId = new UserId("user-123");
            Map<String, Object> payload = Map.of("oldName", "이전 이름", "newName", "새 이름");

            Activity expectedActivity = createMockActivity(type, actorId, payload, null, null, null);
            when(createActivityUseCase.createActivity(any(CreateActivityCommand.class)))
                    .thenReturn(Either.right(expectedActivity));

            // when
            activityHelper.logUserActivity(type, actorId, payload);

            // then
            verify(createActivityUseCase).createActivity(commandCaptor.capture());
            CreateActivityCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.type()).isEqualTo(type);
            assertThat(capturedCommand.boardId()).isNull();
            assertThat(capturedCommand.listId()).isNull();
            assertThat(capturedCommand.cardId()).isNull();
        }

        @Test
        @DisplayName("유효하지 않은 사용자 활동 타입으로 호출 시 경고 로그만 기록")
        void logUserActivity_WithInvalidUserType_ShouldLogWarning() {
            // given
            ActivityType type = ActivityType.CARD_CREATE; // 사용자 활동이 아닌 타입
            UserId actorId = new UserId("user-123");
            Map<String, Object> payload = Map.of("title", "테스트");

            // when
            activityHelper.logUserActivity(type, actorId, payload);

            // then
            verify(createActivityUseCase, times(0)).createActivity(any(CreateActivityCommand.class));
        }
    }

    @Nested
    @DisplayName("검증 메서드 테스트")
    class ValidationMethodTest {

        @Test
        @DisplayName("카드 활동 타입 검증")
        void isCardActivityType_ShouldReturnCorrectResult() {
            // given & when & then
            assertThat(activityHelper.isCardActivityType(ActivityType.CARD_CREATE)).isTrue();
            assertThat(activityHelper.isCardActivityType(ActivityType.CARD_MOVE)).isTrue();
            assertThat(activityHelper.isCardActivityType(ActivityType.CARD_RENAME)).isTrue();
            assertThat(activityHelper.isCardActivityType(ActivityType.LIST_CREATE)).isFalse();
            assertThat(activityHelper.isCardActivityType(ActivityType.BOARD_CREATE)).isFalse();
            assertThat(activityHelper.isCardActivityType(ActivityType.USER_UPDATE_PROFILE)).isFalse();
        }

        @Test
        @DisplayName("리스트 활동 타입 검증")
        void isListActivityType_ShouldReturnCorrectResult() {
            // given & when & then
            assertThat(activityHelper.isListActivityType(ActivityType.LIST_CREATE)).isTrue();
            assertThat(activityHelper.isListActivityType(ActivityType.LIST_RENAME)).isTrue();
            assertThat(activityHelper.isListActivityType(ActivityType.LIST_MOVE)).isTrue();
            assertThat(activityHelper.isListActivityType(ActivityType.CARD_CREATE)).isFalse();
            assertThat(activityHelper.isListActivityType(ActivityType.BOARD_CREATE)).isFalse();
            assertThat(activityHelper.isListActivityType(ActivityType.USER_UPDATE_PROFILE)).isFalse();
        }

        @Test
        @DisplayName("보드 활동 타입 검증")
        void isBoardActivityType_ShouldReturnCorrectResult() {
            // given & when & then
            assertThat(activityHelper.isBoardActivityType(ActivityType.BOARD_CREATE)).isTrue();
            assertThat(activityHelper.isBoardActivityType(ActivityType.BOARD_RENAME)).isTrue();
            assertThat(activityHelper.isBoardActivityType(ActivityType.BOARD_MOVE)).isTrue();
            assertThat(activityHelper.isBoardActivityType(ActivityType.CARD_CREATE)).isFalse();
            assertThat(activityHelper.isBoardActivityType(ActivityType.LIST_CREATE)).isFalse();
            assertThat(activityHelper.isBoardActivityType(ActivityType.USER_UPDATE_PROFILE)).isFalse();
        }

        @Test
        @DisplayName("사용자 활동 타입 검증")
        void isUserActivityType_ShouldReturnCorrectResult() {
            // given & when & then
            assertThat(activityHelper.isUserActivityType(ActivityType.USER_UPDATE_PROFILE)).isTrue();
            assertThat(activityHelper.isUserActivityType(ActivityType.USER_CHANGE_LANGUAGE)).isTrue();
            assertThat(activityHelper.isUserActivityType(ActivityType.USER_CHANGE_PASSWORD)).isTrue();
            assertThat(activityHelper.isUserActivityType(ActivityType.CARD_CREATE)).isFalse();
            assertThat(activityHelper.isUserActivityType(ActivityType.LIST_CREATE)).isFalse();
            assertThat(activityHelper.isUserActivityType(ActivityType.BOARD_CREATE)).isFalse();
        }
    }

    // =================================================================
    // 🛠️ 헬퍼 메서드들
    // =================================================================

    private Activity createMockActivity(ActivityType type, UserId actorId, Map<String, Object> payload,
            BoardId boardId, ListId listId, CardId cardId) {
        return Activity.create(
                type,
                Actor.of(actorId.getId(), "홍", "길동", ""),
                Payload.of(payload),
                boardId,
                listId,
                cardId);
    }
}