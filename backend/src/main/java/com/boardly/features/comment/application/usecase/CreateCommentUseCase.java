package com.boardly.features.comment.application.usecase;

import com.boardly.features.comment.application.port.input.CreateCommentCommand;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface CreateCommentUseCase {

    Either<Failure, Comment> createComment(CreateCommentCommand command);

}
