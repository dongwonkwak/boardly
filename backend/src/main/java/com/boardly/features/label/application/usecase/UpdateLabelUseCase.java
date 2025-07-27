package com.boardly.features.label.application.usecase;

import com.boardly.features.label.application.port.input.UpdateLabelCommand;
import com.boardly.features.label.domain.model.Label;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface UpdateLabelUseCase {

    Either<Failure, Label> updateLabel(UpdateLabelCommand command);

}
