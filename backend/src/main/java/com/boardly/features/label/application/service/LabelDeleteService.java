package com.boardly.features.label.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.card.domain.repository.CardLabelRepository;
import com.boardly.features.label.application.port.input.DeleteLabelCommand;
import com.boardly.features.label.application.usecase.DeleteLabelUseCase;
import com.boardly.features.label.application.validation.LabelValidator;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LabelDeleteService implements DeleteLabelUseCase {
    private final LabelValidator validator;
    private final ValidationMessageResolver messageResolver;
    private final LabelRepository labelRepository;
    private final BoardRepository boardRepository;
    private final CardLabelRepository cardLabelRepository;
    private final BoardPermissionService boardPermissionService;

    @Override
    public Either<Failure, Void> deleteLabel(DeleteLabelCommand command) {
        log.info("LabelDeleteService.deleteLabel() called with command: labelId={}, userId={}",
                command.labelId().getId(), command.userId().getId());

        return validateCommand(command)
                .flatMap(this::findLabel)
                .flatMap(this::validateBoardExists)
                .flatMap(label -> validatePermission(label, command.userId()))
                .flatMap(this::deleteCardConnections)
                .flatMap(this::deleteLabelEntity)
                .peek(result -> log.info("라벨 삭제 완료: labelId={}", command.labelId().getId()))
                .peekLeft(failure -> log.error("라벨 삭제 실패: labelId={}, 오류={}",
                        command.labelId().getId(), failure.getMessage()));
    }

    /**
     * 1단계: 명령어 검증
     */
    private Either<Failure, DeleteLabelCommand> validateCommand(DeleteLabelCommand command) {
        log.debug("라벨 삭제 명령어 검증 시작: {}", command.labelId());

        ValidationResult<DeleteLabelCommand> validationResult = validator.validateDeleteLabel(command);

        if (validationResult.isValid()) {
            log.debug("라벨 삭제 명령어 검증 성공");
            return Either.right(command);
        }

        log.warn("라벨 삭제 명령어 검증 실패: {}", validationResult.getErrors());
        return Either.left(Failure.ofValidation(
                messageResolver.getMessage("error.service.label.delete.validation"),
                List.copyOf(validationResult.getErrorsAsCollection())));
    }

    /**
     * 2단계: 라벨 조회
     */
    private Either<Failure, Label> findLabel(DeleteLabelCommand command) {
        log.debug("라벨 조회 시작: labelId={}", command.labelId());

        Optional<Label> labelOpt = labelRepository.findById(command.labelId());
        if (labelOpt.isEmpty()) {
            log.warn("삭제할 라벨을 찾을 수 없음: labelId={}", command.labelId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("error.service.label.delete.not.found")));
        }

        Label label = labelOpt.get();
        log.debug("라벨 조회 완료: labelId={}, name={}, boardId={}",
                label.getLabelId().getId(), label.getName(), label.getBoardId().getId());
        return Either.right(label);
    }

    /**
     * 3단계: 보드 존재 확인
     */
    private Either<Failure, Label> validateBoardExists(Label label) {
        log.debug("보드 존재 확인 시작: boardId={}", label.getBoardId());

        Optional<Board> boardOpt = boardRepository.findById(label.getBoardId());
        if (boardOpt.isEmpty()) {
            log.warn("라벨이 속한 보드를 찾을 수 없음: boardId={}", label.getBoardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("error.service.label.delete.board.not.found")));
        }

        log.debug("보드 존재 확인 완료: boardId={}", label.getBoardId().getId());
        return Either.right(label);
    }

    /**
     * 4단계: 권한 검증
     */
    private Either<Failure, Label> validatePermission(Label label, UserId userId) {
        log.debug("라벨 삭제 권한 검증 시작: labelId={}, userId={}",
                label.getLabelId().getId(), userId.getId());

        return boardPermissionService.canWriteBoard(label.getBoardId(), userId)
                .flatMap(canWrite -> {
                    if (!canWrite) {
                        log.warn("라벨 삭제 권한 없음: labelId={}, userId={}, boardId={}",
                                label.getLabelId().getId(), userId.getId(), label.getBoardId().getId());
                        return Either.left(Failure.ofPermissionDenied(
                                messageResolver.getMessage("error.service.label.delete.permission.denied")));
                    }
                    log.debug("라벨 삭제 권한 검증 완료: labelId={}, userId={}",
                            label.getLabelId().getId(), userId.getId());
                    return Either.right(label);
                });
    }

    /**
     * 5단계: 카드-라벨 연결 삭제
     */
    private Either<Failure, Label> deleteCardConnections(Label label) {
        log.debug("카드-라벨 연결 삭제 시작: labelId={}", label.getLabelId());

        return cardLabelRepository.deleteByLabelId(label.getLabelId())
                .peek(result -> log.debug("카드-라벨 연결 삭제 완료: labelId={}", label.getLabelId().getId()))
                .peekLeft(failure -> log.error("카드-라벨 연결 삭제 실패: labelId={}, 오류={}",
                        label.getLabelId().getId(), failure.getMessage()))
                .map(result -> label)
                .mapLeft(failure -> Failure.ofInternalServerError(
                        messageResolver.getMessage("error.service.label.delete.card.connections")));
    }

    /**
     * 6단계: 라벨 엔티티 삭제
     */
    private Either<Failure, Void> deleteLabelEntity(Label label) {
        log.debug("라벨 엔티티 삭제 시작: labelId={}", label.getLabelId());

        return labelRepository.delete(label.getLabelId())
                .peek(result -> log.debug("라벨 엔티티 삭제 완료: labelId={}", label.getLabelId().getId()))
                .peekLeft(failure -> log.error("라벨 엔티티 삭제 실패: labelId={}, 오류={}",
                        label.getLabelId().getId(), failure.getMessage()))
                .mapLeft(failure -> Failure.ofInternalServerError(
                        messageResolver.getMessage("error.service.label.delete.internal")));
    }
}
