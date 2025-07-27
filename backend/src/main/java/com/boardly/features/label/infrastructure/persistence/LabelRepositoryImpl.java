package com.boardly.features.label.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LabelRepositoryImpl implements LabelRepository {

    private final LabelJpaRepository labelJpaRepository;
    private final LabelMapper labelMapper;

    @Override
    public Either<Failure, Label> save(Label label) {
        log.debug("라벨 저장 시작: labelId={}, boardId={}, name={}",
                label.getLabelId(), label.getBoardId(), label.getName());

        try {
            var entity = labelMapper.toEntity(label);
            var savedEntity = labelJpaRepository.save(entity);
            var savedLabel = labelMapper.toDomain(savedEntity);
            log.debug("라벨 저장 성공: labelId={}, name={}", savedLabel.getLabelId(), savedLabel.getName());

            return Either.right(savedLabel);
        } catch (Exception e) {
            log.error("라벨 저장 실패: labelId={}, name={}, 예외={}",
                    label.getLabelId(), label.getName(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("라벨 저장 실패: " + e.getMessage()));
        }
    }

    @Override
    public Optional<Label> findById(LabelId labelId) {
        log.debug("라벨 조회 시작: labelId={}", labelId);

        var label = labelJpaRepository.findById(labelId.getId())
                .map(labelMapper::toDomain);

        if (label.isPresent()) {
            log.debug("라벨 조회 완료: labelId={}, name={}", labelId, label.get().getName());
        }

        return label;
    }

    @Override
    public List<Label> findByBoardIdOrderByName(BoardId boardId) {
        log.debug("보드별 라벨 조회 시작: boardId={}", boardId.getId());
        var entities = labelJpaRepository.findByBoardIdOrderByName(boardId.getId());
        var labels = entities.stream()
                .map(labelMapper::toDomain)
                .toList();
        log.debug("보드별 라벨 조회 완료: boardId={}, 라벨 개수={}", boardId.getId(), labels.size());
        return labels;
    }

    @Override
    public Optional<Label> findByBoardIdAndName(BoardId boardId, String name) {
        log.debug("보드별 라벨명 조회 시작: boardId={}, name={}", boardId.getId(), name);
        var entity = labelJpaRepository.findByBoardIdAndName(boardId.getId(), name);
        var label = entity.map(labelMapper::toDomain);
        if (label.isPresent()) {
            log.debug("보드별 라벨명 조회 완료: boardId={}, name={}", boardId.getId(), name);
        }
        return label;
    }

    @Override
    public Either<Failure, Void> delete(LabelId labelId) {
        log.debug("라벨 삭제 시작: labelId={}", labelId);

        try {
            labelJpaRepository.deleteById(labelId.getId());
            log.debug("라벨 삭제 완료: labelId={}", labelId);
            return Either.right(null);
        } catch (Exception e) {
            log.error("라벨 삭제 실패: labelId={}, 예외={}", labelId, e.getMessage());
            return Either.left(Failure.ofInternalServerError("라벨 삭제 실패: " + e.getMessage()));
        }
    }

    @Override
    public boolean existsById(LabelId labelId) {
        return labelJpaRepository.existsById(labelId.getId());
    }

    @Override
    public int countByBoardId(BoardId boardId) {
        return labelJpaRepository.countByBoardId(boardId.getId());
    }

    @Override
    public Either<Failure, Void> deleteByBoardId(BoardId boardId) {
        log.debug("보드별 라벨 삭제 시작: boardId={}", boardId.getId());

        try {
            labelJpaRepository.deleteByBoardId(boardId.getId());
            log.debug("보드별 라벨 삭제 완료: boardId={}", boardId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("보드별 라벨 삭제 실패: boardId={}, 예외={}", boardId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("보드별 라벨 삭제 실패: " + e.getMessage()));
        }
    }
}
