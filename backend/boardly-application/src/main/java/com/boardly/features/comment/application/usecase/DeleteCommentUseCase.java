package com.boardly.features.comment.application.usecase;

import com.boardly.features.comment.application.command.DeleteCommentCommand;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

public interface DeleteCommentUseCase {

    Either<Failure, Void> deleteComment(DeleteCommentCommand command);

}
