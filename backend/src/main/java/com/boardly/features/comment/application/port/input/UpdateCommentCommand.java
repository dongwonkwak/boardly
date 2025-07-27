package com.boardly.features.comment.application.port.input;

import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.user.domain.model.UserId;

public record UpdateCommentCommand(
        CommentId commentId,
        UserId requesterId,
        String content) {

}
