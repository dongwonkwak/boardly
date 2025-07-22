package com.boardly.features.activity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.boardly.features.activity.application.port.input.CreateActivityCommand;
import com.boardly.features.activity.application.validation.ActivityValidator;
import com.boardly.features.activity.domain.model.Activity;
import com.boardly.features.activity.domain.model.ActivityId;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.activity.domain.repository.ActivityRepository;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.model.UserProfile;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityCreateService 테스트")
class ActivityCreateServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ActivityValidator activityValidator;

    @Mock
    private MessageSource messageSource;

    private ActivityCreateService activityCreateService;
    private ValidationMessageResolver messageResolver;

    @BeforeEach
    void setUp() {
        // 기본 메시지 설정
        lenient().when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
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
        activityCreateService = new ActivityCreateService(
                activityRepository, userFinder, activityValidator, messageResolver);
    }

    @Nested
    @DisplayName("createActivity 성공 테스트")
    class CreateActivitySuccessTest {

        @Test
        @DisplayName("유효한 카드 활동 생성 요청이 성공해야 한다")
        void createActivity_WithValidCardActivity_ShouldSucceed() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "새 카드", "description", "카드 설명");

            CreateActivityCommand command = CreateActivityCommand.forCard(
                    ActivityType.CARD_CREATE,
                    actorId,
                    payload,
                    boardId,
                    listId,
                    cardId);

            User user = createMockUser(actorId);
            Activity expectedActivity = createMockActivity(command, user);

            // when
            when(activityValidator.validateCreate(command))
                    .thenReturn(ValidationResult.valid(command));
            when(userFinder.findUserOrThrow(actorId))
                    .thenReturn(user);
            when(activityRepository.save(any(Activity.class)))
                    .thenReturn(Either.right(expectedActivity));

            Either<Failure, Activity> result = activityCreateService.createActivity(command);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(expectedActivity);

            // verify
            verify(activityValidator).validateCreate(command);
            verify(userFinder).findUserOrThrow(actorId);
            verify(activityRepository).save(any(Activity.class));
        }

        @Test
        @DisplayName("유효한 리스트 활동 생성 요청이 성공해야 한다")
        void createActivity_WithValidListActivity_ShouldSucceed() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            Map<String, Object> payload = Map.of("title", "새 리스트");

            CreateActivityCommand command = CreateActivityCommand.forList(
                    ActivityType.LIST_CREATE,
                    actorId,
                    payload,
                    boardId,
                    listId);

            User user = createMockUser(actorId);
            Activity expectedActivity = createMockActivity(command, user);

            // when
            when(activityValidator.validateCreate(command))
                    .thenReturn(ValidationResult.valid(command));
            when(userFinder.findUserOrThrow(actorId))
                    .thenReturn(user);
            when(activityRepository.save(any(Activity.class)))
                    .thenReturn(Either.right(expectedActivity));

            Either<Failure, Activity> result = activityCreateService.createActivity(command);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(expectedActivity);
        }

        @Test
        @DisplayName("유효한 보드 활동 생성 요청이 성공해야 한다")
        void createActivity_WithValidBoardActivity_ShouldSucceed() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            Map<String, Object> payload = Map.of("title", "새 보드");

            CreateActivityCommand command = CreateActivityCommand.forBoard(
                    ActivityType.BOARD_CREATE,
                    actorId,
                    payload,
                    boardId);

            User user = createMockUser(actorId);
            Activity expectedActivity = createMockActivity(command, user);

            // when
            when(activityValidator.validateCreate(command))
                    .thenReturn(ValidationResult.valid(command));
            when(userFinder.findUserOrThrow(actorId))
                    .thenReturn(user);
            when(activityRepository.save(any(Activity.class)))
                    .thenReturn(Either.right(expectedActivity));

            Either<Failure, Activity> result = activityCreateService.createActivity(command);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(expectedActivity);
        }
    }

    @Nested
    @DisplayName("createActivity 실패 테스트")
    class CreateActivityFailureTest {

        @Test
        @DisplayName("검증 실패 시 실패를 반환해야 한다")
        void createActivity_WithValidationFailure_ShouldReturnFailure() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "새 카드");

            CreateActivityCommand command = CreateActivityCommand.forCard(
                    ActivityType.CARD_CREATE,
                    actorId,
                    payload,
                    boardId,
                    listId,
                    cardId);

            ValidationResult<CreateActivityCommand> validationResult = ValidationResult.invalid(
                    "type", "활동 유형은 필수입니다", null);

            // when
            when(activityValidator.validateCreate(command))
                    .thenReturn(validationResult);

            Either<Failure, Activity> result = activityCreateService.createActivity(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("activity.validation.failed");
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("VALIDATION_ERROR");
        }

        @Test
        @DisplayName("사용자를 찾을 수 없을 때 실패를 반환해야 한다")
        void createActivity_WithUserNotFound_ShouldReturnFailure() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "새 카드");

            CreateActivityCommand command = CreateActivityCommand.forCard(
                    ActivityType.CARD_CREATE,
                    actorId,
                    payload,
                    boardId,
                    listId,
                    cardId);

            // when
            when(activityValidator.validateCreate(command))
                    .thenReturn(ValidationResult.valid(command));
            when(userFinder.findUserOrThrow(actorId))
                    .thenThrow(new UsernameNotFoundException(actorId.getId()));

            Either<Failure, Activity> result = activityCreateService.createActivity(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("activity.user.not.found");
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("USER_NOT_FOUND");
            assertThat(((Failure.NotFound) result.getLeft()).getContext()).isEqualTo(actorId);
        }

        @Test
        @DisplayName("저장 실패 시 실패를 반환해야 한다")
        void createActivity_WithSaveFailure_ShouldReturnFailure() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "새 카드");

            CreateActivityCommand command = CreateActivityCommand.forCard(
                    ActivityType.CARD_CREATE,
                    actorId,
                    payload,
                    boardId,
                    listId,
                    cardId);

            User user = createMockUser(actorId);
            Failure saveFailure = Failure.ofInternalError("저장 실패", "SAVE_ERROR", null);

            // when
            when(activityValidator.validateCreate(command))
                    .thenReturn(ValidationResult.valid(command));
            when(userFinder.findUserOrThrow(actorId))
                    .thenReturn(user);
            when(activityRepository.save(any(Activity.class)))
                    .thenReturn(Either.left(saveFailure));

            Either<Failure, Activity> result = activityCreateService.createActivity(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(saveFailure);
        }
    }

    @Nested
    @DisplayName("ActivityCreationContext 테스트")
    class ActivityCreationContextTest {

        @Test
        @DisplayName("ActivityCreationContext가 올바르게 생성되어야 한다")
        void activityCreationContext_ShouldBeCreatedCorrectly() {
            // given
            UserId actorId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");
            ListId listId = new ListId("list-123");
            CardId cardId = new CardId("card-123");
            Map<String, Object> payload = Map.of("title", "새 카드");

            CreateActivityCommand command = CreateActivityCommand.forCard(
                    ActivityType.CARD_CREATE,
                    actorId,
                    payload,
                    boardId,
                    listId,
                    cardId);

            User user = createMockUser(actorId);

            // when
            ActivityCreationContext context = new ActivityCreationContext(command, user, null, null, null);

            // then
            assertThat(context.getCommand()).isEqualTo(command);
            assertThat(context.getUser()).isEqualTo(user);
            assertThat(context.getActor()).isNull();
            assertThat(context.getPayload()).isNull();
            assertThat(context.getActivity()).isNull();
        }
    }

    private User createMockUser(UserId userId) {
        UserProfile userProfile = new UserProfile("홍", "길동");
        return User.builder()
                .userId(userId)
                .userProfile(userProfile)
                .email("hong@example.com")
                .build();
    }

    private Activity createMockActivity(CreateActivityCommand command, User user) {
        return Activity.create(
                command.type(),
                com.boardly.features.activity.domain.model.Actor.of(
                        user.getUserId().toString(),
                        user.getFirstName(),
                        user.getLastName(),
                        ""),
                com.boardly.features.activity.domain.model.Payload.of(command.payload()),
                command.boardId(),
                command.listId(),
                command.cardId());
    }
}