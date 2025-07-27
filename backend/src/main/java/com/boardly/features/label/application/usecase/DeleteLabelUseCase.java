package com.boardly.features.label.application.usecase;

import com.boardly.features.label.application.port.input.DeleteLabelCommand;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface DeleteLabelUseCase {

    Either<Failure, Void> deleteLabel(DeleteLabelCommand command);

}
