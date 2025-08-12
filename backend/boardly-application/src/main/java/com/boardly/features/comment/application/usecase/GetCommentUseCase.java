package com.boardly.features.comment.application.usecase;

import java.util.List;

import com.boardly.shared.common.value.CardId;
import com.boardly.features.comment.domain.Comment;
import com.boardly.shared.common.value.CommentId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

public interface GetCommentUseCase {

    Either<Failure, Comment> getComment(CommentId commentId, UserId requesterId);

    Either<Failure, List<Comment>> getCardComments(CardId cardId, UserId requesterId);

    Either<Failure, List<Comment>> getUserComments(UserId userId, UserId requesterId);
}
