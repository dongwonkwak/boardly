package com.boardly.infrastructure.adapters.out.persistence.board;

import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;
import io.vavr.control.Either;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@org.springframework.stereotype.Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {

    private final BoardJpaRepository boardJpaRepository;
    private final BoardMapper boardMapper;

    @Override
    @Transactional
    public Either<Failure, Board> save(Board board) {
        try {
            BoardEntity savedEntity;

            // 1. 먼저 기존 객체가 있는지 확인
            Optional<BoardEntity> existingEntity = boardJpaRepository.findById(
                board.getBoardId().getId()
            );

            if (existingEntity.isPresent()) {
                // 2. 기존 객체가 있으면 업데이트
                log.debug(
                    "기존 보드 업데이트: boardId={}, title={}",
                    board.getBoardId().getId(),
                    board.getTitle()
                );

                BoardEntity entityToUpdate = existingEntity.get();
                entityToUpdate.updateFromDomainEntity(board);
                savedEntity = boardJpaRepository.save(entityToUpdate);
            } else {
                // 3. 기존 객체가 없으면 새로 저장
                log.debug(
                    "새로운 보드 저장: boardId={}, title={}, ownerId={}",
                    board.getBoardId().getId(),
                    board.getTitle(),
                    board.getOwnerId().getId()
                );

                BoardEntity boardEntity = boardMapper.toEntity(board);
                savedEntity = boardJpaRepository.save(boardEntity);

                log.debug(
                    "새로운 보드 저장 완료: boardId={}, title={}",
                    savedEntity.getBoardId(),
                    savedEntity.getTitle()
                );
            }

            return Either.right(boardMapper.toDomain(savedEntity));
        } catch (DataIntegrityViolationException e) {
            log.error("보드 저장 중 제약 조건 위반 오류: {}", e.getMessage());
            return Either.left(
                Failure.ofConflict("BOARD_CONSTRAINT_VIOLATION")
            );
        } catch (Exception e) {
            log.error("보드 저장 중 오류 발생: {}", e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Board> findById(BoardId boardId) {
        log.debug("findById: boardId={}", boardId.getId());
        return boardJpaRepository
            .findById(boardId.getId())
            .map(boardMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Board> findByOwnerId(UserId ownerId) {
        log.debug("findByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository
            .findByOwnerId(ownerId.getId())
            .stream()
            .map(boardMapper::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Board> findActiveByOwnerId(UserId ownerId) {
        log.debug("findActiveByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository
            .findByOwnerIdAndIsArchivedFalse(ownerId.getId())
            .stream()
            .map(boardMapper::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Board> findArchivedByOwnerId(UserId ownerId) {
        log.debug("findArchivedByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository
            .findByOwnerIdAndIsArchivedTrue(ownerId.getId())
            .stream()
            .map(boardMapper::toDomain)
            .toList();
    }

    @Override
    @Transactional
    public Either<Failure, Void> delete(BoardId boardId) {
        try {
            if (boardJpaRepository.existsById(boardId.getId())) {
                log.debug("보드 삭제: boardId={}", boardId.getId());
                boardJpaRepository.deleteById(boardId.getId());
                return Either.right(null);
            } else {
                return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
            }
        } catch (Exception e) {
            log.error("보드 삭제 중 오류 발생: {}", e.getMessage());
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(BoardId boardId) {
        return boardJpaRepository.existsByBoardId(boardId.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByOwnerId(UserId ownerId) {
        log.debug("countByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository.countByOwnerId(ownerId.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByOwnerId(UserId ownerId) {
        log.debug("countActiveByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository.countByOwnerIdAndIsArchivedFalse(
            ownerId.getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Board> findByOwnerIdAndTitleContaining(
        UserId ownerId,
        String title
    ) {
        log.debug(
            "findByOwnerIdAndTitleContaining: ownerId={}, title={}",
            ownerId.getId(),
            title
        );
        return boardJpaRepository
            .findByOwnerIdAndTitleContaining(ownerId.getId(), title)
            .stream()
            .map(boardMapper::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Board> findByIdAndOwnerId(BoardId boardId, UserId ownerId) {
        log.debug(
            "findByIdAndOwnerId: boardId={}, ownerId={}",
            boardId.getId(),
            ownerId.getId()
        );
        return boardJpaRepository
            .findByBoardIdAndOwnerId(boardId.getId(), ownerId.getId())
            .map(boardMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findBoardNameById(BoardId boardId) {
        log.debug("findBoardNameById: boardId={}", boardId.getId());
        return boardJpaRepository.findTitleById(boardId.getId());
    }
}
