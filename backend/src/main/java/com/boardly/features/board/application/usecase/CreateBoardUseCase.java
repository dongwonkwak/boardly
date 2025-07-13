package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.domain.model.Board;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface CreateBoardUseCase {

  Either<Failure, Board> createBoard(CreateBoardCommand command);

}
