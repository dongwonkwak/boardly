package com.boardly.features.comment.application.command;

import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;

public record CreateCommentCommand(
        CardId cardId,
        WorkspaceId workspaceId,
        UserId authorId,
        String content) {

}
