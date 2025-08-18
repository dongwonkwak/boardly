package com.boardly.features.boardlist.application.policy;

import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardListMovePolicy {

    private final BoardListRepository boardListRepository;

    public Either<Failure, Void> canMoveWithinSameBoard(BoardList boardList, int newPosition) {
        return validatePosition(newPosition)
            .flatMap(v -> validatePositionRange(boardList.getBoardId(), newPosition));
    }

    public boolean hasPositionChanged(BoardList boardList, int newPosition) {
        return boardList.getPosition() != newPosition;
    }

    private Either<Failure, Void> validatePosition(int position) {
        if (position < 0) {
            return Either.left(Failure.ofConflict("POSITION_INVALID"));
        }
        return Either.right(null);
    }

    private Either<Failure, Void> validatePositionRange(BoardId boardId, int position) {
        long listCount = boardListRepository.countByBoardId(boardId);
        if (position > listCount) {
            return Either.left(Failure.ofConflict("POSITION_OUT_OF_RANGE"));
        }
        return Either.right(null);
    }
}
