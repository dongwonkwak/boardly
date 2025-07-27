package com.boardly.features.label.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.label.application.port.input.CreateLabelCommand;
import com.boardly.features.label.application.usecase.CreateLabelUseCase;
import com.boardly.features.label.application.validation.LabelValidator;
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
public class LabelCreateService implements CreateLabelUseCase {

    private final LabelRepository labelRepository;
    private final LabelValidator labelValidator;
    private final ValidationMessageResolver validationMessageResolver;
    private final BoardRepository boardRepository;

    @Override
    public Either<Failure, Label> createLabel(CreateLabelCommand command) {
        log.info("LabelCreateService.createLabel() called with command: boardId={}, name={}, color={}, requesterId={}",
                command.boardId().getId(), command.name(), command.color(), command.requesterId().getId());

        return validateCommand(command)
                .flatMap(this::validateBoardAccess)
                .flatMap(this::validateLabelNameUniqueness)
                .flatMap(this::createLabelEntity)
                .flatMap(this::saveLabel);
    }

    /**
     * 명령 유효성 검사
     */
    private Either<Failure, CreateLabelCommand> validateCommand(CreateLabelCommand command) {
        var validationResult = labelValidator.validateCreateLabel(command);
        if (validationResult.isInvalid()) {
            log.warn("라벨 생성 실패 - 검증 오류: {}", validationResult.getErrors());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.label.creation.failed"),
                    "VALIDATION_ERROR",
                    validationResult.getErrorsAsCollection().stream().toList()));
        }
        return Either.right(command);
    }

    /**
     * 보드 접근 권한 확인
     */
    private Either<Failure, CreateLabelCommand> validateBoardAccess(CreateLabelCommand command) {
        var boardOpt = boardRepository.findById(command.boardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.not.found")));
        }

        var board = boardOpt.get();
        if (!board.getOwnerId().equals(command.requesterId())) {
            log.warn("보드 접근 권한 없음: boardId={}, requesterId={}",
                    command.boardId().getId(), command.requesterId().getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("validation.board.modification.access.denied")));
        }

        return Either.right(command);
    }

    /**
     * 라벨명 중복 확인
     */
    private Either<Failure, CreateLabelCommand> validateLabelNameUniqueness(CreateLabelCommand command) {
        var existingLabelOpt = labelRepository.findByBoardIdAndName(command.boardId(), command.name());
        if (existingLabelOpt.isPresent()) {
            log.warn("라벨명 중복: boardId={}, name={}", command.boardId().getId(), command.name());
            return Either.left(Failure.ofConflict(
                    validationMessageResolver.getMessage("validation.label.name.duplicate")));
        }

        return Either.right(command);
    }

    /**
     * 라벨 생성
     */
    private Either<Failure, Label> createLabelEntity(CreateLabelCommand command) {
        try {
            var label = Label.create(command.boardId(), command.name(), command.color());
            log.debug("라벨 생성 완료: labelId={}, name={}, color={}",
                    label.getLabelId().getId(), label.getName(), label.getColor());
            return Either.right(label);
        } catch (Exception e) {
            log.error("라벨 생성 실패: name={}, color={}, 예외={}",
                    command.name(), command.color(), e.getMessage());
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("validation.label.creation.failed")));
        }
    }

    /**
     * 라벨 저장
     */
    private Either<Failure, Label> saveLabel(Label label) {
        return labelRepository.save(label)
                .peek(savedLabel -> log.info("라벨 저장 완료: labelId={}, name={}",
                        savedLabel.getLabelId().getId(), savedLabel.getName()))
                .peekLeft(failure -> log.error("라벨 저장 실패: name={}, 오류={}",
                        label.getName(), failure.getMessage()));
    }
}
