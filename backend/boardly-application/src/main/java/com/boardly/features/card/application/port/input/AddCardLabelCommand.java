package com.boardly.features.card.application.command;

import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.LabelId;
import com.boardly.shared.common.value.UserId;

public record AddCardLabelCommand(
        CardId cardId,
        LabelId labelId,
        UserId requesterId) {

}
