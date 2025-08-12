package com.boardly.features.comment.application.port.input;

import com.boardly.shared.common.value.CommentId;
import com.boardly.shared.common.value.UserId;

public record UpdateCommentCommand(
        CommentId commentId,
        UserId requesterId,
        String content) {

}
