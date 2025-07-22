package com.boardly.features.activity.application.usecase;

import com.boardly.features.activity.application.port.input.GetActivityQuery;
import com.boardly.features.activity.application.port.output.ActivityListResponse;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface GetActivityUseCase {

    Either<Failure, ActivityListResponse> getActivities(GetActivityQuery query);

}
