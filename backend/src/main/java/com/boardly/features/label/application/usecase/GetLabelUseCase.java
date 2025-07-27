package com.boardly.features.label.application.usecase;

import java.util.List;
import java.util.Optional;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.user.domain.model.UserId;

public interface GetLabelUseCase {

    Optional<Label> getLabel(LabelId labelId, UserId requesterId);

    List<Label> getBoardLabels(BoardId boardId, UserId requesterId);

}
