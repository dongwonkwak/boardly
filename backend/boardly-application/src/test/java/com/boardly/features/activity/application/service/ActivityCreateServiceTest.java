package com.boardly.features.activity.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.boardly.features.activity.application.command.CreateActivityCommand;
import com.boardly.features.activity.application.validation.ActivityValidator;
import com.boardly.features.activity.domain.Activity;
import com.boardly.features.activity.domain.ActivityType;
import com.boardly.features.activity.domain.port.ActivityRepository;
import com.boardly.features.user.domain.User;
import com.boardly.features.user.domain.UserProfile;
import com.boardly.features.user.domain.port.UserFinder;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import io.vavr.control.Either;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityCreateServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ActivityValidator activityValidator;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private ActivityCreateService service;

    private CreateActivityCommand validCommand;
    private UserId actorId;
    private BoardId boardId;
    private ListId listId;
    private CardId cardId;
    private Map<String, Object> payload;

    @BeforeEach
    void setUp() {
        actorId = new UserId("user-1");
        boardId = new BoardId("board-1");
        listId = new ListId("list-1");
        cardId = new CardId("card-1");
        payload = new HashMap<>();
        payload.put("k", "v");

        validCommand = CreateActivityCommand.of(
            ActivityType.CARD_CREATE,
            actorId,
            payload,
            "My Board",
            boardId,
            listId,
            cardId
        );
    }

    @Test
    @DisplayName("createActivity: 성공 경로")
    void createActivity_success() {
        when(activityValidator.validateCreate(validCommand)).thenReturn(
            ValidationResult.valid(validCommand)
        );

        User user = User.builder()
            .userId(actorId)
            .email("user@example.com")
            .hashedPassword("hashed")
            .userProfile(new UserProfile("First", "Last"))
            .isActive(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(userFinder.findUserOrThrow(actorId)).thenReturn(user);

        // repository.save는 전달받은 Activity를 그대로 성공으로 반환
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv ->
            Either.right(inv.getArgument(0))
        );

        Either<Failure, Activity> result = service.createActivity(validCommand);

        assertTrue(result.isRight());
        Activity saved = result.get();
        assertEquals(ActivityType.CARD_CREATE, saved.getType());
        assertEquals("My Board", saved.getBoardName());
        assertNotNull(saved.getId());

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(
            Activity.class
        );
        verify(activityRepository, times(1)).save(captor.capture());
        Activity toSave = captor.getValue();
        assertEquals(boardId, toSave.getBoardId());
        assertEquals(listId, toSave.getListId());
        assertEquals(cardId, toSave.getCardId());
        assertEquals("v", toSave.getPayload().getString("k"));
    }

    @Test
    @DisplayName("createActivity: 검증 실패시 InputError 반환")
    void createActivity_validationFailure() {
        when(activityValidator.validateCreate(validCommand)).thenReturn(
            ValidationResult.invalid("type", "invalid", null)
        );
        when(
            messageResolver.getMessage("activity.validation.failed")
        ).thenReturn("Validation failed");

        Either<Failure, Activity> result = service.createActivity(validCommand);

        assertTrue(result.isLeft());
        Failure failure = result.getLeft();
        assertInstanceOf(Failure.InputError.class, failure);
        assertEquals("Validation failed", failure.getMessage());
        assertEquals(
            "VALIDATION_ERROR",
            ((Failure.InputError) failure).getErrorCode()
        );

        verifyNoInteractions(userFinder);
        verifyNoInteractions(activityRepository);
    }

    @Test
    @DisplayName("createActivity: 사용자 미존재시 NotFound 반환")
    void createActivity_userNotFound() {
        when(activityValidator.validateCreate(validCommand)).thenReturn(
            ValidationResult.valid(validCommand)
        );
        when(userFinder.findUserOrThrow(actorId)).thenThrow(
            new RuntimeException("no user")
        );
        when(messageResolver.getMessage("activity.user.not.found")).thenReturn(
            "User not found"
        );

        Either<Failure, Activity> result = service.createActivity(validCommand);

        assertTrue(result.isLeft());
        Failure failure = result.getLeft();
        assertInstanceOf(Failure.NotFound.class, failure);
        assertEquals("User not found", failure.getMessage());
        assertEquals(
            "USER_NOT_FOUND",
            ((Failure.NotFound) failure).getErrorCode()
        );

        verifyNoInteractions(activityRepository);
    }

    @Test
    @DisplayName("createActivity: 저장 실패시 동일 Failure 전파")
    void createActivity_repositoryFailure() {
        when(activityValidator.validateCreate(validCommand)).thenReturn(
            ValidationResult.valid(validCommand)
        );

        User user = User.builder()
            .userId(actorId)
            .email("user@example.com")
            .hashedPassword("hashed")
            .userProfile(new UserProfile("First", "Last"))
            .isActive(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(userFinder.findUserOrThrow(actorId)).thenReturn(user);

        Failure dbError = Failure.ofInternalServerError("db error");
        when(activityRepository.save(any(Activity.class))).thenReturn(
            Either.left(dbError)
        );

        Either<Failure, Activity> result = service.createActivity(validCommand);

        assertTrue(result.isLeft());
        assertSame(dbError, result.getLeft());
    }
}
