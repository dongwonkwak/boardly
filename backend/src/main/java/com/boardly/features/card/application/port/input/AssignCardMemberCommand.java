package com.boardly.features.card.application.port.input;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.shared.common.value.UserId;

public record AssignCardMemberCommand(
        CardId cardId,
        UserId memberId,
        UserId requesterId) {

}
