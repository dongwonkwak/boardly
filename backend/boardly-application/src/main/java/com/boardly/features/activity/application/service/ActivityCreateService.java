package com.boardly.features.activity.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.activity.domain.Activity;
import com.boardly.features.activity.domain.Actor;
import com.boardly.features.activity.domain.Payload;
import com.boardly.features.activity.domain.port.ActivityRepository;
import com.boardly.features.user.domain.port.UserFinder;
import com.boardly.features.user.domain.User;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.features.activity.application.command.CreateActivityCommand;
import com.boardly.features.activity.application.usecase.CreateActivityUseCase;
import com.boardly.features.activity.application.validation.ActivityValidator;
import com.boardly.shared.common.error.Failure;

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

    private static final String MSG_VALIDATION_FAILED = "activity.validation.failed";
    private static final String MSG_USER_NOT_FOUND = "activity.user.not.found";
    private static final String ERROR_CODE_VALIDATION = "VALIDATION_ERROR";
    private static final String ERROR_CODE_USER_NOT_FOUND = "USER_NOT_FOUND";

    private final ActivityRepository activityRepository;
    private final UserFinder userFinder;
    private final ActivityValidator activityValidator;
    private final MessageResolver messageResolver;

    @Override
    public Either<Failure, Activity> createActivity(CreateActivityCommand command) {
        log.info("createActivity called: {}", command);

        return validateCommand(command)
                .flatMap(this::findUser)
                .flatMap(this::createActor)
                .flatMap(this::createPayload)
                .flatMap(this::createActivity)
                .flatMap(this::saveActivity);
    }

    private Either<Failure, CreateActivityCommand> validateCommand(CreateActivityCommand command) {
        var validationResult = activityValidator.validateCreate(command);
        if (validationResult.isInvalid()) {
            log.warn("Validation failed: {}", validationResult.getErrors());
            return Either.left(
                Failure.ofInputError(
                    messageResolver.getMessage(MSG_VALIDATION_FAILED),
                    ERROR_CODE_VALIDATION,
                    validationResult.getErrorsAsCollection().stream().toList()
                )
            );
        }
        return Either.right(command);
    }

    private Either<Failure, ActivityCreationContext> findUser(CreateActivityCommand command) {
        try {
            User user = userFinder.findUserOrThrow(command.actorId());
            return Either.right(new ActivityCreationContext(command, user, null, null, null, command.boardName()));
        } catch (Exception e) {
            log.warn("User not found: {}", command.actorId(), e);
            return Either.left(
                Failure.ofNotFound(
                    messageResolver.getMessage(MSG_USER_NOT_FOUND),
                    ERROR_CODE_USER_NOT_FOUND,
                    command.actorId()
                )
            );
        }
    }

    private Either<Failure, ActivityCreationContext> createActor(ActivityCreationContext context) {
        Actor actor = Actor.of(
                context.user().getUserId().toString(),
                context.user().getFirstName(),
                context.user().getLastName(),
                ""
        );
        return Either.right(new ActivityCreationContext(
                context.command(), context.user(), actor, null, null, context.boardName()
        ));
    }

    private Either<Failure, ActivityCreationContext> createPayload(ActivityCreationContext context) {
        Map<String, Object> payloadData = new HashMap<>(context.command().payload());
        if (context.boardName() != null) {
            payloadData.put("boardName", context.boardName());
        }

        Payload payload = Payload.of(payloadData);
        return Either.right(new ActivityCreationContext(
                context.command(), context.user(), context.actor(), payload, null, context.boardName()
        ));
    }

    private Either<Failure, ActivityCreationContext> createActivity(ActivityCreationContext context) {
        Activity activity = Activity.create(
                context.command().type(),
                context.actor(),
                contextPayloadOrEmpty(context),
                context.boardName(),
                context.command().boardId(),
                context.command().listId(),
                context.command().cardId()
        );
        return Either.right(new ActivityCreationContext(
                context.command(), context.user(), context.actor(), context.payload(), activity, context.boardName()
        ));
    }

    private Payload contextPayloadOrEmpty(ActivityCreationContext context) {
        return context.payload() != null ? context.payload() : Payload.empty();
    }

    private Either<Failure, Activity> saveActivity(ActivityCreationContext context) {
        Either<Failure, Activity> saveResult = activityRepository.save(context.activity());
        if (saveResult.isLeft()) {
            log.error("Repository error: {}", saveResult.getLeft());
            return Either.left(saveResult.getLeft());
        }

        Activity savedActivity = saveResult.get();
        log.info("Activity created successfully: {}", savedActivity.getId());
        return Either.right(savedActivity);
    }

    private static record ActivityCreationContext(
            CreateActivityCommand command,
            User user,
            Actor actor,
            Payload payload,
            Activity activity,
            String boardName
    ) {}
}
