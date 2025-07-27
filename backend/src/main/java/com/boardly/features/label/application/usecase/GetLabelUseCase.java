package com.boardly.features.label.application.usecase;

import java.util.List;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface GetLabelUseCase {

    Either<Failure, Label> getLabel(LabelId labelId, UserId requesterId);

    Either<Failure, List<Label>> getBoardLabels(BoardId boardId, UserId requesterId);

}
