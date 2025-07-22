package com.boardly.features.board.infrastructure.persistence;

import com.boardly.features.board.domain.model.BoardRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardMemberJpaRepository extends JpaRepository<BoardMemberEntity, String> {

    /**
     * 보드 ID와 사용자 ID로 보드 멤버를 조회합니다.
     */
    Optional<BoardMemberEntity> findByBoardIdAndUserId(String boardId, String userId);

    /**
     * 보드 ID로 모든 활성 멤버를 조회합니다.
     */
    List<BoardMemberEntity> findByBoardIdAndIsActiveTrue(String boardId);

    /**
     * 사용자 ID로 해당 사용자가 멤버로 있는 모든 보드 멤버를 조회합니다.
     */
    List<BoardMemberEntity> findByUserId(String userId);

    /**
     * 보드 ID와 역할로 멤버를 조회합니다.
     */
    List<BoardMemberEntity> findByBoardIdAndRole(String boardId, BoardRole role);

    /**
     * 보드 멤버가 존재하는지 확인합니다.
     */
    boolean existsByBoardIdAndUserId(String boardId, String userId);

    /**
     * 보드의 멤버 수를 조회합니다.
     */
    long countByBoardId(String boardId);

    /**
     * 보드의 활성 멤버 수를 조회합니다.
     */
    long countByBoardIdAndIsActiveTrue(String boardId);

    /**
     * 보드 ID와 사용자 ID로 활성 멤버를 조회합니다.
     */
    Optional<BoardMemberEntity> findByBoardIdAndUserIdAndIsActiveTrue(String boardId, String userId);
}