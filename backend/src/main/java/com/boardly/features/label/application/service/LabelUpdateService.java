package com.boardly.features.label.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.label.application.port.input.UpdateLabelCommand;
import com.boardly.features.label.application.validation.LabelValidator;
import com.boardly.features.label.application.usecase.UpdateLabelUseCase;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LabelUpdateService implements UpdateLabelUseCase {

    private final LabelValidator labelValidator;
    private final ValidationMessageResolver messageResolver;
    private final LabelRepository labelRepository;
    private final BoardRepository boardRepository;
    private final BoardPermissionService boardPermissionService;

    @Override
    public Either<Failure, Label> updateLabel(UpdateLabelCommand command) {
        return validateCommand(command)
                .flatMap(this::findLabel)
                .flatMap(this::validateBoardExists)
                .flatMap(label -> validatePermission(label, command))
                .flatMap(this::updateLabelFields)
                .flatMap(this::saveLabel);
    }

    /**
     * 1단계: 명령어 검증
     */
    private Either<Failure, UpdateLabelCommand> validateCommand(UpdateLabelCommand command) {
        log.debug("라벨 수정 명령어 검증 시작: {}", command.labelId());

        var validationResult = labelValidator.validateUpdateLabel(command);

        if (validationResult.isValid()) {
            log.debug("라벨 수정 명령어 검증 성공");
            return Either.right(command);
        }

        log.warn("라벨 수정 명령어 검증 실패: {}", validationResult.getErrors());
        return Either.left(Failure.ofValidation(
                messageResolver.getMessage("error.service.label.update.validation"),
                List.copyOf(validationResult.getErrorsAsCollection())));
    }

    /**
     * 2단계: 라벨 조회
     */
    private Either<Failure, Label> findLabel(UpdateLabelCommand command) {
        log.debug("라벨 조회 시작: {}", command.labelId());

        return labelRepository.findById(command.labelId())
                .map(label -> {
                    log.debug("라벨 조회 성공: {}", label.getLabelId());
                    return Either.<Failure, Label>right(label);
                })
                .orElseGet(() -> {
                    log.warn("라벨을 찾을 수 없음: {}", command.labelId());
                    return Either.left(Failure.ofNotFound(
                            messageResolver.getMessage("error.service.label.update.not.found"),
                            "LABEL_NOT_FOUND",
                            command.labelId()));
                });
    }

    /**
     * 3단계: 보드 존재 확인
     */
    private Either<Failure, Label> validateBoardExists(Label label) {
        log.debug("보드 존재 확인 시작: {}", label.getBoardId());

        return boardRepository.findById(label.getBoardId())
                .map(board -> {
                    log.debug("보드 조회 성공: {}", board.getBoardId());
                    return Either.<Failure, Label>right(label);
                })
                .orElseGet(() -> {
                    log.warn("보드를 찾을 수 없음: {}", label.getBoardId());
                    return Either.left(Failure.ofNotFound(
                            messageResolver.getMessage("error.service.label.update.board.not.found"),
                            "BOARD_NOT_FOUND",
                            label.getBoardId()));
                });
    }

    /**
     * 4단계: 라벨 수정 권한 검증
     */
    private Either<Failure, Label> validatePermission(Label label, UpdateLabelCommand command) {
        log.debug("라벨 수정 권한 검증 시작: boardId={}, requesterId={}",
                label.getBoardId(), command.requesterId());

        return boardPermissionService.canWriteBoard(label.getBoardId(), command.requesterId())
                .flatMap(canModify -> {
                    if (!canModify) {
                        log.warn("라벨 수정 권한 없음: boardId={}, requesterId={}",
                                label.getBoardId(), command.requesterId());
                        return Either.left(Failure.ofPermissionDenied(
                                messageResolver.getMessage("error.service.label.update.permission.denied"),
                                "LABEL_UPDATE_PERMISSION_DENIED",
                                Map.of("boardId", label.getBoardId(), "requesterId", command.requesterId())));
                    }
                    log.debug("라벨 수정 권한 검증 성공: boardId={}, requesterId={}",
                            label.getBoardId(), command.requesterId());
                    return Either.right(label);
                });
    }

    /**
     * 5단계: 라벨 필드 업데이트
     */
    private Either<Failure, Label> updateLabelFields(Label label) {
        log.debug("라벨 필드 업데이트 시작: {}", label.getLabelId());

        try {
            // 이름이 제공된 경우에만 업데이트
            if (label.getName() != null && !label.getName().trim().isEmpty()) {
                label.updateName(label.getName());
            }

            // 색상이 제공된 경우에만 업데이트
            if (label.getColor() != null && !label.getColor().trim().isEmpty()) {
                label.updateColor(label.getColor());
            }

            log.debug("라벨 필드 업데이트 성공: {}", label.getLabelId());
            return Either.right(label);

        } catch (Exception e) {
            log.error("라벨 필드 업데이트 중 오류 발생: {}", label.getLabelId(), e);
            return Either.left(Failure.ofInternalError(
                    messageResolver.getMessage("error.service.label.update.internal"),
                    "LABEL_UPDATE_ERROR",
                    label.getLabelId()));
        }
    }

    /**
     * 6단계: 라벨 저장
     */
    private Either<Failure, Label> saveLabel(Label label) {
        log.debug("라벨 저장 시작: {}", label.getLabelId());

        return labelRepository.save(label)
                .peek(savedLabel -> log.info("라벨 수정 완료: {}", savedLabel.getLabelId()))
                .peekLeft(failure -> log.error("라벨 저장 실패: {}", failure.getMessage()));
    }
}
