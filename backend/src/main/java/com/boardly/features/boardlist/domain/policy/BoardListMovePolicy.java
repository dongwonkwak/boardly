package com.boardly.features.boardlist.domain.policy;

import org.springframework.stereotype.Component;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보드 리스트 이동 정책
 * 
 * <p>
 * 보드 리스트 이동과 관련된 비즈니스 규칙을 정의하고 검증합니다.
 * 위치 유효성, 이동 권한 등의 정책을 관리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoardListMovePolicy {

    private final BoardListRepository boardListRepository;

    /**
     * 같은 보드 내에서 리스트 이동이 가능한지 검증합니다.
     * 
     * @param boardList   이동할 리스트
     * @param newPosition 새로운 위치
     * @return 성공 시 Right(Void), 실패 시 Left(Failure)
     */
    public Either<Failure, Void> canMoveWithinSameBoard(BoardList boardList, int newPosition) {
        log.debug("같은 보드 내 리스트 이동 정책 검증: listId={}, oldPosition={}, newPosition={}",
                boardList.getListId().getId(), boardList.getPosition(), newPosition);

        // 위치 유효성 검증
        return validatePosition(newPosition)
                .flatMap(v -> validatePositionRange(boardList.getBoardId(), newPosition))
                .peek(v -> log.debug("같은 보드 내 리스트 이동 정책 검증 성공"));
    }

    /**
     * 위치 값의 기본 유효성을 검증합니다.
     */
    private Either<Failure, Void> validatePosition(int position) {
        if (position < 0) {
            log.warn("잘못된 리스트 위치: position={}", position);
            return Either.left(Failure.ofConflict("POSITION_INVALID"));
        }
        return Either.right(null);
    }

    /**
     * 보드 내 위치 범위를 검증합니다.
     */
    private Either<Failure, Void> validatePositionRange(BoardId boardId, int position) {
        long listCount = boardListRepository.countByBoardId(boardId);

        // 새로운 위치는 현재 리스트 개수보다 클 수 없음 (0-based index)
        if (position > listCount) {
            log.warn("위치 범위 초과: boardId={}, position={}, listCount={}",
                    boardId.getId(), position, listCount);
            return Either.left(Failure.ofConflict("POSITION_OUT_OF_RANGE"));
        }

        return Either.right(null);
    }

    /**
     * 리스트 이동이 유효한지 확인합니다.
     * 
     * @param boardList   이동할 리스트
     * @param newPosition 새로운 위치
     * @return 이동 가능하면 true, 그렇지 않으면 false
     */
    public boolean isValidMove(BoardList boardList, int newPosition) {
        return validatePosition(newPosition)
                .flatMap(v -> validatePositionRange(boardList.getBoardId(), newPosition))
                .isRight();
    }

    /**
     * 리스트의 현재 위치와 새로운 위치가 다른지 확인합니다.
     * 
     * @param boardList   이동할 리스트
     * @param newPosition 새로운 위치
     * @return 위치가 다르면 true, 같으면 false
     */
    public boolean hasPositionChanged(BoardList boardList, int newPosition) {
        return boardList.getPosition() != newPosition;
    }
}