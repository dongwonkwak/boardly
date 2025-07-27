package com.boardly.features.comment.application.usecase;

import java.util.List;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface GetCommentUseCase {

    Either<Failure, Comment> getComment(CommentId commentId, UserId requesterId);

    Either<Failure, List<Comment>> getCardComments(CardId cardId, UserId requesterId);

    Either<Failure, List<Comment>> getUserComments(UserId userId, UserId requesterId);
}
