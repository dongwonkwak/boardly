package com.boardly.features.activity.application.usecase;

import com.boardly.features.activity.application.command.CreateActivityCommand;
import com.boardly.features.activity.domain.Activity;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

public interface CreateActivityUseCase {

    Either<Failure, Activity> createActivity(CreateActivityCommand command);

}
