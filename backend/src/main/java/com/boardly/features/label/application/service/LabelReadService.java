package com.boardly.features.label.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.label.application.usecase.GetLabelUseCase;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LabelReadService implements GetLabelUseCase {

    private final LabelRepository labelRepository;
    private final BoardRepository boardRepository;
    private final ValidationMessageResolver validationMessageResolver;

    @Override
    public Either<Failure, Label> getLabel(LabelId labelId, UserId requesterId) {
        log.debug("라벨 조회 시작: labelId={}, requesterId={}", labelId.getId(), requesterId.getId());

        // 1. 라벨 존재 확인
        Optional<Label> labelOpt = labelRepository.findById(labelId);
        if (labelOpt.isEmpty()) {
            log.warn("라벨을 찾을 수 없음: labelId={}", labelId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.label.not.found")));
        }

        Label label = labelOpt.get();

        // 2. 보드 접근 권한 확인
        Either<Failure, Boolean> accessResult = validateBoardAccess(label.getBoardId(), requesterId);
        if (accessResult.isLeft()) {
            log.warn("라벨 조회 권한 없음: labelId={}, requesterId={}, 오류={}",
                    labelId.getId(), requesterId.getId(), accessResult.getLeft().getMessage());
            return Either.left(accessResult.getLeft());
        }

        log.debug("라벨 조회 완료: labelId={}, name={}", labelId.getId(), label.getName());
        return Either.right(label);
    }

    @Override
    public Either<Failure, List<Label>> getBoardLabels(BoardId boardId, UserId requesterId) {
        log.debug("보드별 라벨 조회 시작: boardId={}, requesterId={}", boardId.getId(), requesterId.getId());

        // 1. 보드 접근 권한 확인
        Either<Failure, Boolean> accessResult = validateBoardAccess(boardId, requesterId);
        if (accessResult.isLeft()) {
            log.warn("보드별 라벨 조회 권한 없음: boardId={}, requesterId={}, 오류={}",
                    boardId.getId(), requesterId.getId(), accessResult.getLeft().getMessage());
            return Either.left(accessResult.getLeft());
        }

        // 2. 보드별 라벨 조회
        List<Label> labels = labelRepository.findByBoardIdOrderByName(boardId);
        log.debug("보드별 라벨 조회 완료: boardId={}, 라벨 개수={}", boardId.getId(), labels.size());

        return Either.right(labels);
    }

    /**
     * 보드 접근 권한 확인
     */
    private Either<Failure, Boolean> validateBoardAccess(BoardId boardId, UserId requesterId) {
        // 1. 보드 존재 확인
        Optional<Board> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", boardId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.not.found")));
        }

        Board board = boardOpt.get();

        // 2. 권한 확인 (보드 소유자만 접근 가능)
        if (!board.getOwnerId().equals(requesterId)) {
            log.warn("보드 접근 권한 없음: boardId={}, requesterId={}, boardOwnerId={}",
                    boardId.getId(), requesterId.getId(), board.getOwnerId().getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("validation.board.access.denied")));
        }

        return Either.right(true);
    }
}
