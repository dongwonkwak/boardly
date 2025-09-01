package com.boardly.features.comment.application.command;

import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;

public record CreateCommentCommand(
        CardId cardId,
        UserId authorId,
        String content) {

}
