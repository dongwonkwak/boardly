package com.boardly.features.activity.application.usecase;

import com.boardly.features.activity.application.port.input.CreateActivityCommand;
import com.boardly.features.activity.domain.model.Activity;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface CreateActivityUseCase {

    Either<Failure, Activity> createActivity(CreateActivityCommand command);

}
