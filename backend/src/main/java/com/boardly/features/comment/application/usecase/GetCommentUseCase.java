package com.boardly.features.comment.application.usecase;

import java.util.List;
import java.util.Optional;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.user.domain.model.UserId;

public interface GetCommentUseCase {

    Optional<Comment> getComment(CommentId commentId, UserId requesterId);

    List<Comment> getCardComments(CardId cardId, UserId requesterId);

    List<Comment> getUserComments(UserId userId, UserId requesterId);
}
