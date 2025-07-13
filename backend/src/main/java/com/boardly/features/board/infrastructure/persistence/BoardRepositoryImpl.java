package com.boardly.features.board.infrastructure.persistence;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {

    private final BoardJpaRepository boardJpaRepository;

    @Override
    public Either<Failure, Board> save(Board board) {
        try {
            BoardEntity savedEntity;
            
            // 새로운 객체인지 기존 객체인지 판단
            if (board.isNew()) {
                // 새로운 객체 저장
                log.debug("새로운 보드 저장: boardId={}, title={}, ownerId={}", 
                    board.getBoardId().getId(), board.getTitle(), board.getOwnerId().getId());
                BoardEntity boardEntity = BoardEntity.fromDomainEntity(board);
                savedEntity = boardJpaRepository.save(boardEntity);
                log.debug("새로운 보드 저장 완료: boardId={}, title={}", 
                    savedEntity.getBoardId(), savedEntity.getTitle());
            } else {
                // 기존 객체 업데이트
                log.debug("기존 보드 업데이트: boardId={}, title={}", 
                    board.getBoardId().getId(), board.getTitle());
                Optional<BoardEntity> existingEntity = boardJpaRepository.findById(board.getBoardId().getId());
                
                if (existingEntity.isEmpty()) {
                    return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
                }
                
                BoardEntity entityToUpdate = existingEntity.get();
                entityToUpdate.updateFromDomainEntity(board);
                savedEntity = boardJpaRepository.save(entityToUpdate);
            }
            
            return Either.right(savedEntity.toDomainEntity());
            
        } catch (DataIntegrityViolationException e) {
            log.error("보드 저장 중 제약 조건 위반 오류: {}", e.getMessage());
            return Either.left(Failure.ofConflict("BOARD_CONSTRAINT_VIOLATION"));
        } catch (Exception e) {
            log.error("보드 저장 중 오류 발생: {}", e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    @Override
    public Optional<Board> findById(BoardId boardId) {
        log.debug("findById: boardId={}", boardId.getId());
        return boardJpaRepository.findById(boardId.getId())
                .map(BoardEntity::toDomainEntity);
    }

    @Override
    public List<Board> findByOwnerId(UserId ownerId) {
        log.debug("findByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository.findByOwnerId(ownerId.getId())
                .stream()
                .map(BoardEntity::toDomainEntity)
                .toList();
    }

    @Override
    public List<Board> findActiveByOwnerId(UserId ownerId) {
        log.debug("findActiveByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository.findByOwnerIdAndIsArchivedFalse(ownerId.getId())
                .stream()
                .map(BoardEntity::toDomainEntity)
                .toList();
    }

    @Override
    public List<Board> findArchivedByOwnerId(UserId ownerId) {
        log.debug("findArchivedByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository.findByOwnerIdAndIsArchivedTrue(ownerId.getId())
                .stream()
                .map(BoardEntity::toDomainEntity)
                .toList();
    }

    @Override
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
    public boolean existsById(BoardId boardId) {
        return boardJpaRepository.existsByBoardId(boardId.getId());
    }

    @Override
    public long countByOwnerId(UserId ownerId) {
        log.debug("countByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository.countByOwnerId(ownerId.getId());
    }

    @Override
    public long countActiveByOwnerId(UserId ownerId) {
        log.debug("countActiveByOwnerId: ownerId={}", ownerId.getId());
        return boardJpaRepository.countByOwnerIdAndIsArchivedFalse(ownerId.getId());
    }

    @Override
    public List<Board> findByOwnerIdAndTitleContaining(UserId ownerId, String title) {
        log.debug("findByOwnerIdAndTitleContaining: ownerId={}, title={}", ownerId.getId(), title);
        return boardJpaRepository.findByOwnerIdAndTitleContaining(ownerId.getId(), title)
                .stream()
                .map(BoardEntity::toDomainEntity)
                .toList();
    }

    @Override
    public Optional<Board> findByIdAndOwnerId(BoardId boardId, UserId ownerId) {
        log.debug("findByIdAndOwnerId: boardId={}, ownerId={}", boardId.getId(), ownerId.getId());
        return boardJpaRepository.findByBoardIdAndOwnerId(boardId.getId(), ownerId.getId())
                .map(BoardEntity::toDomainEntity);
    }
} 