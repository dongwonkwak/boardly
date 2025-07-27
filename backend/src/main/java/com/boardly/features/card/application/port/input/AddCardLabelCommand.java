package com.boardly.features.card.application.port.input;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.user.domain.model.UserId;

public record AddCardLabelCommand(
        CardId cardId,
        LabelId labelId,
        UserId requesterId) {

}
