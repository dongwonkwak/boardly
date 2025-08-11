package com.boardly.features.comment.application.port.input;

import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.shared.common.value.UserId;

public record DeleteCommentCommand(
        CommentId commentId,
        UserId requesterId) {

}
