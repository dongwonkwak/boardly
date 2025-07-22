package com.boardly.features.board.infrastructure.persistence;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.model.BoardMemberId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BoardMemberRepositoryImpl implements BoardMemberRepository {

    private final BoardMemberJpaRepository boardMemberJpaRepository;

    @Override
    public Either<Failure, BoardMember> save(BoardMember boardMember) {
        try {
            BoardMemberEntity entity = BoardMemberEntity.fromDomainEntity(boardMember);
            BoardMemberEntity savedEntity = boardMemberJpaRepository.save(entity);
            log.debug("보드 멤버 저장 완료: memberId={}, boardId={}, userId={}",
                    savedEntity.getMemberId(), savedEntity.getBoardId(), savedEntity.getUserId());
            return Either.right(savedEntity.toDomainEntity());
        } catch (Exception e) {
            log.error("보드 멤버 저장 중 오류 발생: {}", e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    @Override
    public Optional<BoardMember> findById(BoardMemberId memberId) {
        log.debug("findById: memberId={}", memberId.getId());
        return boardMemberJpaRepository.findById(memberId.getId())
                .map(BoardMemberEntity::toDomainEntity);
    }

    @Override
    public Optional<BoardMember> findByBoardIdAndUserId(BoardId boardId, UserId userId) {
        log.debug("findByBoardIdAndUserId: boardId={}, userId={}", boardId.getId(), userId.getId());
        return boardMemberJpaRepository.findByBoardIdAndUserId(boardId.getId(), userId.getId())
                .map(BoardMemberEntity::toDomainEntity);
    }

    @Override
    public List<BoardMember> findActiveByBoardId(BoardId boardId) {
        log.debug("findActiveByBoardId: boardId={}", boardId.getId());
        return boardMemberJpaRepository.findByBoardIdAndIsActiveTrue(boardId.getId())
                .stream()
                .map(BoardMemberEntity::toDomainEntity)
                .toList();
    }

    @Override
    public List<BoardMember> findByUserId(UserId userId) {
        log.debug("findByUserId: userId={}", userId.getId());
        return boardMemberJpaRepository.findByUserId(userId.getId())
                .stream()
                .map(BoardMemberEntity::toDomainEntity)
                .toList();
    }

    @Override
    public List<BoardMember> findByBoardIdAndRole(BoardId boardId, BoardRole role) {
        log.debug("findByBoardIdAndRole: boardId={}, role={}", boardId.getId(), role);
        return boardMemberJpaRepository.findByBoardIdAndRole(boardId.getId(), role)
                .stream()
                .map(BoardMemberEntity::toDomainEntity)
                .toList();
    }

    @Override
    public Either<Failure, Void> delete(BoardMemberId memberId) {
        try {
            if (boardMemberJpaRepository.existsById(memberId.getId())) {
                log.debug("보드 멤버 삭제: memberId={}", memberId.getId());
                boardMemberJpaRepository.deleteById(memberId.getId());
                return Either.right(null);
            } else {
                return Either.left(Failure.ofNotFound("BOARD_MEMBER_NOT_FOUND"));
            }
        } catch (Exception e) {
            log.error("보드 멤버 삭제 중 오류 발생: {}", e.getMessage());
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    @Override
    public boolean existsByBoardIdAndUserId(BoardId boardId, UserId userId) {
        return boardMemberJpaRepository.existsByBoardIdAndUserId(boardId.getId(), userId.getId());
    }

    @Override
    public long countByBoardId(BoardId boardId) {
        log.debug("countByBoardId: boardId={}", boardId.getId());
        return boardMemberJpaRepository.countByBoardId(boardId.getId());
    }

    @Override
    public long countActiveByBoardId(BoardId boardId) {
        log.debug("countActiveByBoardId: boardId={}", boardId.getId());
        return boardMemberJpaRepository.countByBoardIdAndIsActiveTrue(boardId.getId());
    }

    @Override
    public Either<Failure, Void> deleteByBoardId(BoardId boardId) {
        try {
            log.debug("보드의 모든 멤버 삭제 시작: boardId={}", boardId.getId());
            boardMemberJpaRepository.deleteByBoardId(boardId.getId());
            log.debug("보드의 모든 멤버 삭제 완료: boardId={}", boardId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("보드의 멤버 삭제 실패: boardId={}, error={}", boardId.getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError("보드의 멤버 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
}