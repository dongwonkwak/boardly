package com.boardly.features.comment.application.usecase;

import com.boardly.features.comment.application.port.input.CreateCommentCommand;
import com.boardly.features.comment.domain.Comment;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

public interface CreateCommentUseCase {

    Either<Failure, Comment> createComment(CreateCommentCommand command);

}
