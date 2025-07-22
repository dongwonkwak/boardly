package com.boardly.features.board.domain.repository;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.model.BoardMemberId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

import java.util.List;
import java.util.Optional;

/**
 * 보드 멤버 도메인 Repository 인터페이스
 */
public interface BoardMemberRepository {

    /**
     * 보드 멤버를 저장합니다.
     */
    Either<Failure, BoardMember> save(BoardMember boardMember);

    /**
     * 보드 멤버 ID로 보드 멤버를 조회합니다.
     */
    Optional<BoardMember> findById(BoardMemberId memberId);

    /**
     * 보드 ID와 사용자 ID로 보드 멤버를 조회합니다.
     */
    Optional<BoardMember> findByBoardIdAndUserId(BoardId boardId, UserId userId);

    /**
     * 보드 ID로 모든 활성 멤버를 조회합니다.
     */
    List<BoardMember> findActiveByBoardId(BoardId boardId);

    /**
     * 사용자 ID로 해당 사용자가 멤버로 있는 모든 보드 멤버를 조회합니다.
     */
    List<BoardMember> findByUserId(UserId userId);

    /**
     * 보드 ID와 역할로 멤버를 조회합니다.
     */
    List<BoardMember> findByBoardIdAndRole(BoardId boardId, BoardRole role);

    /**
     * 보드 멤버를 삭제합니다.
     */
    Either<Failure, Void> delete(BoardMemberId memberId);

    /**
     * 보드 멤버가 존재하는지 확인합니다.
     */
    boolean existsByBoardIdAndUserId(BoardId boardId, UserId userId);

    /**
     * 보드의 멤버 수를 조회합니다.
     */
    long countByBoardId(BoardId boardId);

    /**
     * 보드의 활성 멤버 수를 조회합니다.
     */
    long countActiveByBoardId(BoardId boardId);
}