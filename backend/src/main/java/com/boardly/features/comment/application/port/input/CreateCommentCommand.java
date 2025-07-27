package com.boardly.features.comment.application.port.input;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;

public record CreateCommentCommand(
        CardId cardId,
        UserId authorId,
        String content) {

}
