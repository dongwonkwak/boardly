package com.boardly.features.activity.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.activity.domain.model.Activity;
import com.boardly.features.activity.domain.model.Actor;
import com.boardly.features.activity.domain.model.Payload;
import com.boardly.features.activity.domain.repository.ActivityRepository;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.User;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.features.activity.application.port.input.CreateActivityCommand;
import com.boardly.features.activity.application.usecase.CreateActivityUseCase;
import com.boardly.features.activity.application.validation.ActivityValidator;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity 생성 과정의 중간 데이터를 담는 헬퍼 클래스
 */
@lombok.Value
class ActivityCreationContext {
    CreateActivityCommand command;
    User user;
    Actor actor;
    Payload payload;
    Activity activity;
    String boardName;
}

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ActivityCreateService implements CreateActivityUseCase {

    private final ActivityRepository activityRepository;
    private final UserFinder userFinder;
    private final ActivityValidator activityValidator;
    private final ValidationMessageResolver messageResolver;

    @Override
    public Either<Failure, Activity> createActivity(CreateActivityCommand command) {
        log.info("ActivityCreateService.createActivity() called with command: {}", command);

        return validateCommand(command)
                .flatMap(this::findUser)
                .flatMap(this::createActor)
                .flatMap(this::createPayload)
                .flatMap(this::createActivity)
                .flatMap(this::saveActivity);
    }

    /**
     * 명령 유효성 검사
     */
    private Either<Failure, CreateActivityCommand> validateCommand(CreateActivityCommand command) {
        var validationResult = activityValidator.validateCreate(command);
        if (validationResult.isInvalid()) {
            log.warn("Activity creation failed due to validation: {}", validationResult.getErrors());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("activity.validation.failed"),
                    "VALIDATION_ERROR",
                    validationResult.getErrorsAsCollection().stream().toList()));
        }
        return Either.right(command);
    }

    /**
     * 사용자 조회
     */
    private Either<Failure, ActivityCreationContext> findUser(CreateActivityCommand command) {
        try {
            User user = userFinder.findUserOrThrow(command.actorId());
            return Either.right(new ActivityCreationContext(command, user, null, null, null, command.boardName()));
        } catch (Exception e) {
            log.warn("Activity creation failed due to user not found: {}", command.actorId(), e);
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("activity.user.not.found"),
                    "USER_NOT_FOUND",
                    command.actorId()));
        }
    }

    /**
     * Actor 생성
     */
    private Either<Failure, ActivityCreationContext> createActor(ActivityCreationContext context) {
        Actor actor = Actor.of(
                context.getUser().getUserId().toString(),
                context.getUser().getFirstName(),
                context.getUser().getLastName(),
                ""); // UserProfile에는 profileImageUrl이 없으므로 빈 문자열 사용
        return Either.right(new ActivityCreationContext(context.getCommand(), context.getUser(), actor, null, null,
                context.getBoardName()));
    }

    /**
     * Payload 생성
     */
    private Either<Failure, ActivityCreationContext> createPayload(ActivityCreationContext context) {
        // boardName을 payload에 포함
        Map<String, Object> payloadWithBoardName = new HashMap<>(context.getCommand().payload());
        if (context.getBoardName() != null) {
            payloadWithBoardName.put("boardName", context.getBoardName());
        }

        Payload payload = Payload.of(payloadWithBoardName);
        return Either.right(new ActivityCreationContext(context.getCommand(), context.getUser(), context.getActor(),
                payload, null, context.getBoardName()));
    }

    /**
     * Activity 도메인 객체 생성
     */
    private Either<Failure, ActivityCreationContext> createActivity(ActivityCreationContext context) {
        Activity activity = Activity.create(
                context.getCommand().type(),
                context.getActor(),
                context.getPayload(),
                context.getBoardName(),
                context.getCommand().boardId(),
                context.getCommand().listId(),
                context.getCommand().cardId());
        return Either.right(new ActivityCreationContext(context.getCommand(), context.getUser(), context.getActor(),
                context.getPayload(), activity, context.getBoardName()));
    }

    /**
     * Activity 저장
     */
    private Either<Failure, Activity> saveActivity(ActivityCreationContext context) {
        Either<Failure, Activity> saveResult = activityRepository.save(context.getActivity());
        if (saveResult.isLeft()) {
            log.error("Activity creation failed due to repository error: {}", saveResult.getLeft());
            return Either.left(saveResult.getLeft());
        }

        Activity savedActivity = saveResult.get();
        log.info("Activity created successfully: {}", savedActivity.getId());
        return Either.right(savedActivity);
    }
}
