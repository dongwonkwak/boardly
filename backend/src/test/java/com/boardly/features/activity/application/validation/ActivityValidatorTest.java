package com.boardly.features.activity.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import com.boardly.features.activity.application.port.input.CreateActivityCommand;
import com.boardly.features.activity.application.port.input.GetActivityQuery;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityValidator 테스트")
class ActivityValidatorTest {

    @Mock
    private MessageSource messageSource;

    private ActivityValidator activityValidator;
    private ValidationMessageResolver messageResolver;
    private CommonValidationRules commonValidationRules;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);

        // 기본 메시지 설정 - lenient로 설정하여 불필요한 stubbing 허용
        lenient()
            .when(
                messageSource.getMessage(
                    anyString(),
                    any(Object[].class),
                    any(Locale.class)
                )
            )
            .thenAnswer(invocation -> {
                String code = invocation.getArgument(0);
                Object[] args = invocation.getArgument(1);
                StringBuilder message = new StringBuilder(code);
                if (args != null) {
                    for (Object arg : args) {
                        message.append(" ").append(arg);
                    }
                }
                return message.toString();
            });

        messageResolver = new ValidationMessageResolver(messageSource);
        commonValidationRules = new CommonValidationRules(messageResolver);
        activityValidator = new ActivityValidator(
            commonValidationRules,
            messageResolver
        );
    }

    @Nested
    @DisplayName("CreateActivityCommand 검증 테스트")
    class CreateActivityCommandValidationTest {

        @Test
        @DisplayName("유효한 카드 활동 생성 커맨드는 검증을 통과해야 한다")
        void validateCreate_WithValidCardActivity_ShouldBeValid() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of(
                "title",
                "새 카드",
                "description",
                "카드 설명"
            );

            CreateActivityCommand command = CreateActivityCommand.forCard(
                ActivityType.CARD_CREATE,
                actorId,
                payload,
                "프로젝트 A",
                boardId,
                listId,
                cardId
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("유효한 리스트 활동 생성 커맨드는 검증을 통과해야 한다")
        void validateCreate_WithValidListActivity_ShouldBeValid() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            Map<String, Object> payload = Map.of("title", "새 리스트");

            CreateActivityCommand command = CreateActivityCommand.forList(
                ActivityType.LIST_CREATE,
                actorId,
                payload,
                "프로젝트 A",
                boardId,
                listId
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("유효한 보드 활동 생성 커맨드는 검증을 통과해야 한다")
        void validateCreate_WithValidBoardActivity_ShouldBeValid() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            Map<String, Object> payload = Map.of("title", "새 보드");

            CreateActivityCommand command = CreateActivityCommand.forBoard(
                ActivityType.BOARD_CREATE,
                actorId,
                payload,
                "프로젝트 A",
                boardId
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("활동 유형이 null이면 검증에 실패해야 한다")
        void validateCreate_WithNullType_ShouldBeInvalid() {
            // given
            UserId actorId = new UserId("user-123");
            Map<String, Object> payload = Map.of("title", "새 보드");

            CreateActivityCommand command = new CreateActivityCommand(
                null,
                actorId,
                payload,
                "프로젝트 A",
                null,
                null,
                null
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("type");
        }

        @Test
        @DisplayName("actorId가 null이면 검증에 실패해야 한다")
        void validateCreate_WithNullActorId_ShouldBeInvalid() {
            // given
            Map<String, Object> payload = Map.of("title", "새 보드");

            CreateActivityCommand command = new CreateActivityCommand(
                ActivityType.USER_UPDATE_PROFILE, // 사용자 활동으로 변경
                null,
                payload,
                "프로젝트 A",
                null,
                null,
                null
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "userId"
            );
        }

        @Test
        @DisplayName("payload가 null이면 검증에 실패해야 한다")
        void validateCreate_WithNullPayload_ShouldBeInvalid() {
            // given
            UserId actorId = new UserId("user-123");

            CreateActivityCommand command = new CreateActivityCommand(
                ActivityType.USER_UPDATE_PROFILE, // 사용자 활동으로 변경
                actorId,
                null,
                "프로젝트 A",
                null,
                null,
                null
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "payload"
            );
        }

        @Test
        @DisplayName("payload가 비어있으면 검증에 실패해야 한다")
        void validateCreate_WithEmptyPayload_ShouldBeInvalid() {
            // given
            UserId actorId = new UserId("user-123");
            Map<String, Object> payload = new HashMap<>();

            CreateActivityCommand command = new CreateActivityCommand(
                ActivityType.USER_UPDATE_PROFILE, // 사용자 활동으로 변경
                actorId,
                payload,
                "프로젝트 A",
                null,
                null,
                null
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "payload"
            );
        }

        @Test
        @DisplayName("카드 활동에서 cardId가 null이면 검증에 실패해야 한다")
        void validateCreate_CardActivityWithNullCardId_ShouldBeInvalid() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            Map<String, Object> payload = Map.of("title", "새 카드");

            CreateActivityCommand command = new CreateActivityCommand(
                ActivityType.CARD_CREATE,
                actorId,
                payload,
                "프로젝트 A",
                boardId,
                listId,
                null
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "cardId"
            );
        }

        @Test
        @DisplayName("리스트 활동에서 listId가 null이면 검증에 실패해야 한다")
        void validateCreate_ListActivityWithNullListId_ShouldBeInvalid() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            Map<String, Object> payload = Map.of("title", "새 리스트");

            CreateActivityCommand command = new CreateActivityCommand(
                ActivityType.LIST_CREATE,
                actorId,
                payload,
                null,
                boardId,
                null,
                null
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "listId"
            );
        }

        @Test
        @DisplayName("보드 활동에서 boardId가 null이면 검증에 실패해야 한다")
        void validateCreate_BoardActivityWithNullBoardId_ShouldBeInvalid() {
            // given
            UserId actorId = new UserId("user-123");
            Map<String, Object> payload = Map.of("title", "새 보드");

            CreateActivityCommand command = new CreateActivityCommand(
                ActivityType.BOARD_CREATE,
                actorId,
                payload,
                null,
                null,
                null,
                null
            );

            // when
            ValidationResult<CreateActivityCommand> result =
                activityValidator.validateCreate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "boardId"
            );
        }
    }

    @Nested
    @DisplayName("GetActivityQuery 검증 테스트")
    class GetActivityQueryValidationTest {

        @Test
        @DisplayName("유효한 보드 활동 조회 쿼리는 검증을 통과해야 한다")
        void validateGet_WithValidBoardQuery_ShouldBeValid() {
            // given
            BoardId boardId = new BoardId("board-123");
            GetActivityQuery query = GetActivityQuery.forBoardWithPagination(
                boardId,
                0,
                20
            );

            // when
            ValidationResult<GetActivityQuery> result =
                activityValidator.validateGet(query);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("유효한 사용자 활동 조회 쿼리는 검증을 통과해야 한다")
        void validateGet_WithValidUserQuery_ShouldBeValid() {
            // given
            UserId userId = new UserId("user-123");
            GetActivityQuery query = GetActivityQuery.forUserWithPagination(
                userId,
                0,
                20
            );

            // when
            ValidationResult<GetActivityQuery> result =
                activityValidator.validateGet(query);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("유효한 페이지네이션 쿼리는 검증을 통과해야 한다")
        void validateGet_WithValidPaginationQuery_ShouldBeValid() {
            // given
            BoardId boardId = new BoardId("board-123");
            GetActivityQuery query = GetActivityQuery.forBoardWithPagination(
                boardId,
                0,
                20
            );

            // when
            ValidationResult<GetActivityQuery> result =
                activityValidator.validateGet(query);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("userId와 boardId가 모두 null이면 검증에 실패해야 한다")
        void validateGet_WithNullUserAndBoardId_ShouldBeInvalid() {
            // given
            GetActivityQuery query = new GetActivityQuery(
                null,
                null,
                null,
                null,
                0,
                20
            );

            // when
            ValidationResult<GetActivityQuery> result =
                activityValidator.validateGet(query);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "userId,boardId"
            );
        }

        @Test
        @DisplayName("음수 페이지 번호는 검증에 실패해야 한다")
        void validateGet_WithNegativePage_ShouldBeInvalid() {
            // given
            BoardId boardId = new BoardId("board-123");
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                null,
                null,
                -1,
                20
            );

            // when
            ValidationResult<GetActivityQuery> result =
                activityValidator.validateGet(query);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("page");
        }

        @Test
        @DisplayName("페이지 크기가 최소값보다 작으면 검증에 실패해야 한다")
        void validateGet_WithTooSmallPageSize_ShouldBeInvalid() {
            // given
            BoardId boardId = new BoardId("board-123");
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                null,
                null,
                0,
                0
            );

            // when
            ValidationResult<GetActivityQuery> result =
                activityValidator.validateGet(query);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("size");
        }

        @Test
        @DisplayName("페이지 크기가 최대값보다 크면 검증에 실패해야 한다")
        void validateGet_WithTooLargePageSize_ShouldBeInvalid() {
            // given
            BoardId boardId = new BoardId("board-123");
            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                null,
                null,
                0,
                101
            );

            // when
            ValidationResult<GetActivityQuery> result =
                activityValidator.validateGet(query);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("size");
        }

        @Test
        @DisplayName("since가 until보다 늦으면 검증에 실패해야 한다")
        void validateGet_WithInvalidDateRange_ShouldBeInvalid() {
            // given
            BoardId boardId = new BoardId("board-123");
            Instant since = Instant.now();
            Instant until = since.minusSeconds(3600); // 1시간 전

            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                since,
                until,
                0,
                20
            );

            // when
            ValidationResult<GetActivityQuery> result =
                activityValidator.validateGet(query);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo(
                "since,until"
            );
        }

        @Test
        @DisplayName("유효한 날짜 범위는 검증을 통과해야 한다")
        void validateGet_WithValidDateRange_ShouldBeValid() {
            // given
            BoardId boardId = new BoardId("board-123");
            Instant since = Instant.now().minusSeconds(3600); // 1시간 전
            Instant until = Instant.now();

            GetActivityQuery query = new GetActivityQuery(
                null,
                boardId,
                since,
                until,
                0,
                20
            );

            // when
            ValidationResult<GetActivityQuery> result =
                activityValidator.validateGet(query);

            // then
            assertThat(result.isValid()).isTrue();
        }
    }
}
