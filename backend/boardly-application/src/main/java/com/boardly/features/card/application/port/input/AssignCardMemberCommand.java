package com.boardly.features.card.application.command;

import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;

public record AssignCardMemberCommand(
        CardId cardId,
        UserId memberId,
        UserId requesterId) {

}
