package com.boardly.features.board.domain.repository;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

import java.util.List;
import java.util.Optional;

/**
 * 보드 도메인 Repository 인터페이스
 * 도메인 레이어에서 정의하여 의존성 역전 원칙을 적용
 */
public interface BoardRepository {

    /**
     * 보드를 저장합니다.
     */
    Either<Failure, Board> save(Board board);

    /**
     * 보드 ID로 보드를 조회합니다.
     */
    Optional<Board> findById(BoardId boardId);

    /**
     * 소유자 ID로 보드 목록을 조회합니다.
     */
    List<Board> findByOwnerId(UserId ownerId);

    /**
     * 소유자 ID로 활성 보드 목록을 조회합니다 (아카이브되지 않은 보드).
     */
    List<Board> findActiveByOwnerId(UserId ownerId);

    /**
     * 소유자 ID로 아카이브된 보드 목록을 조회합니다.
     */
    List<Board> findArchivedByOwnerId(UserId ownerId);

    /**
     * 보드를 삭제합니다.
     */
    Either<Failure, Void> delete(BoardId boardId);

    /**
     * 보드가 존재하는지 확인합니다.
     */
    boolean existsById(BoardId boardId);

    /**
     * 소유자별 보드 개수를 조회합니다.
     */
    long countByOwnerId(UserId ownerId);

    /**
     * 소유자별 활성 보드 개수를 조회합니다.
     */
    long countActiveByOwnerId(UserId ownerId);

    /**
     * 제목으로 보드를 검색합니다.
     */
    List<Board> findByOwnerIdAndTitleContaining(UserId ownerId, String title);

    /**
     * 소유자 ID와 보드 ID로 보드를 조회합니다.
     */
    Optional<Board> findByIdAndOwnerId(BoardId boardId, UserId ownerId);
}