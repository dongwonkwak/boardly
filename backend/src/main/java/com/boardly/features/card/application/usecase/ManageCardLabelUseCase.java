package com.boardly.features.card.application.usecase;

import java.util.List;

import com.boardly.features.card.application.port.input.AddCardLabelCommand;
import com.boardly.features.card.application.port.input.RemoveCardLabelCommand;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface ManageCardLabelUseCase {

    Either<Failure, Void> addLabel(AddCardLabelCommand command);

    Either<Failure, Void> removeLabel(RemoveCardLabelCommand command);

    List<LabelId> getCardLabels(CardId cardId, UserId requesterId);

}
