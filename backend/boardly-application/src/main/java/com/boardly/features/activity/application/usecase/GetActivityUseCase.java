package com.boardly.features.activity.application.usecase;

import com.boardly.features.activity.application.dto.ActivityListResponse;
import com.boardly.features.activity.application.query.GetActivityQuery;
import com.boardly.shared.common.error.Failure;
import io.vavr.control.Either;

public interface GetActivityUseCase {
    Either<Failure, ActivityListResponse> getActivities(GetActivityQuery query);
}
