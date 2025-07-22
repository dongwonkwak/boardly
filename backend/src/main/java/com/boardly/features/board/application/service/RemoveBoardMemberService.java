package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.features.board.application.usecase.RemoveBoardMemberUseCase;
import com.boardly.features.board.application.validation.RemoveBoardMemberValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.model.BoardMemberId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RemoveBoardMemberService implements RemoveBoardMemberUseCase {

    private final RemoveBoardMemberValidator removeBoardMemberValidator;
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardPermissionService boardPermissionService;
    private final ValidationMessageResolver validationMessageResolver;

    @Override
    public Either<Failure, Void> removeBoardMember(RemoveBoardMemberCommand command) {
        log.info("보드 멤버 삭제 시작: boardId={}, targetUserId={}, requestedBy={}",
                command.boardId().getId(), command.targetUserId().getId(), command.requestedBy().getId());

        // 1. 입력 데이터 검증
        ValidationResult<RemoveBoardMemberCommand> validationResult = removeBoardMemberValidator.validate(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 멤버 삭제 검증 실패: boardId={}, targetUserId={}, violations={}",
                    command.boardId().getId(), command.targetUserId().getId(),
                    validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        return validateInput(command)
                .flatMap(this::findExistingBoard)
                .flatMap(board -> verifyManagementPermission(command, board))
                .flatMap(board -> findExistingMember(command))
                .flatMap(this::validateRemoval)
                .flatMap(this::removeMember);
    }

    /**
     * 1단계: 입력 검증
     */
    private Either<Failure, RemoveBoardMemberCommand> validateInput(RemoveBoardMemberCommand command) {
        if (command.boardId() == null) {
            log.warn("보드 ID가 null입니다");
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.board.id.required")));
        }

        if (command.targetUserId() == null) {
            log.warn("대상 사용자 ID가 null입니다");
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.user.id.required")));
        }

        if (command.requestedBy() == null) {
            log.warn("요청자 ID가 null입니다");
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.user.id.required")));
        }

        // 자기 자신을 삭제하려는 경우 체크
        if (command.targetUserId().equals(command.requestedBy())) {
            log.warn("자기 자신을 삭제하려고 시도: userId={}", command.targetUserId().getId());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.board.member.self.removal.denied")));
        }

        return Either.right(command);
    }

    /**
     * 2단계: 보드 존재 확인
     */
    private Either<Failure, Board> findExistingBoard(RemoveBoardMemberCommand command) {
        Optional<Board> boardOpt = boardRepository.findById(command.boardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.not.found")));
        }

        Board board = boardOpt.get();
        if (board.isArchived()) {
            log.warn("아카이브된 보드에서 멤버 삭제 시도: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofConflict(
                    validationMessageResolver.getMessage("validation.board.archived.modification.denied")));
        }

        return Either.right(board);
    }

    /**
     * 3단계: 멤버 관리 권한 확인
     */
    private Either<Failure, Board> verifyManagementPermission(RemoveBoardMemberCommand command, Board board) {
        return boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy())
                .flatMap(canManage -> {
                    if (!canManage) {
                        log.warn("보드 멤버 관리 권한 없음: boardId={}, requestedBy={}",
                                command.boardId().getId(), command.requestedBy().getId());
                        return Either.left(Failure.ofPermissionDenied(
                                validationMessageResolver.getMessage("validation.board.member.management.denied")));
                    }
                    return Either.right(board);
                });
    }

    /**
     * 4단계: 대상 멤버 존재 확인
     */
    private Either<Failure, BoardMember> findExistingMember(RemoveBoardMemberCommand command) {
        Optional<BoardMember> memberOpt = boardMemberRepository.findByBoardIdAndUserId(
                command.boardId(), command.targetUserId());

        if (memberOpt.isEmpty()) {
            log.warn("보드 멤버를 찾을 수 없음: boardId={}, userId={}",
                    command.boardId().getId(), command.targetUserId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.member.not.found")));
        }

        BoardMember member = memberOpt.get();
        if (!member.isActive()) {
            log.warn("이미 비활성화된 보드 멤버: boardId={}, userId={}",
                    command.boardId().getId(), command.targetUserId().getId());
            return Either.left(Failure.ofConflict(
                    validationMessageResolver.getMessage("validation.board.member.already.inactive")));
        }

        return Either.right(member);
    }

    /**
     * 5단계: 삭제 유효성 검증
     */
    private Either<Failure, BoardMember> validateRemoval(BoardMember member) {
        // OWNER 역할을 가진 멤버는 삭제할 수 없음
        if (member.getRole() == BoardRole.OWNER) {
            log.warn("OWNER 역할을 가진 멤버 삭제 시도: memberId={}", member.getMemberId().getId());
            return Either.left(Failure.ofBusinessRuleViolation(
                    validationMessageResolver.getMessage("validation.board.member.owner.removal.denied"),
                    "OWNER_REMOVAL_DENIED",
                    null));
        }

        // 보드의 마지막 멤버인지 확인
        long activeMemberCount = boardMemberRepository.countActiveByBoardId(member.getBoardId());
        if (activeMemberCount <= 1) {
            log.warn("보드의 마지막 멤버 삭제 시도: boardId={}, memberId={}",
                    member.getBoardId().getId(), member.getMemberId().getId());
            return Either.left(Failure.ofBusinessRuleViolation(
                    validationMessageResolver.getMessage("validation.board.member.last.member.removal.denied"),
                    "LAST_MEMBER_REMOVAL_DENIED",
                    null));
        }

        return Either.right(member);
    }

    /**
     * 6단계: 멤버 삭제
     */
    private Either<Failure, Void> removeMember(BoardMember member) {
        try {
            // 멤버를 비활성화하는 방식으로 삭제 처리
            member.deactivate();

            // 업데이트된 멤버 정보 저장
            Either<Failure, BoardMember> saveResult = boardMemberRepository.save(member);
            if (saveResult.isLeft()) {
                log.error("보드 멤버 비활성화 저장 실패: memberId={}, error={}",
                        member.getMemberId().getId(), saveResult.getLeft().getMessage());
                return Either.left(saveResult.getLeft());
            }

            log.info("보드 멤버 삭제 완료: memberId={}, boardId={}, userId={}, role={}",
                    member.getMemberId().getId(),
                    member.getBoardId().getId(),
                    member.getUserId().getId(),
                    member.getRole());

            return Either.right(null);

        } catch (Exception e) {
            log.error("보드 멤버 삭제 중 오류 발생: memberId={}, error={}",
                    member.getMemberId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalError(e.getMessage(), "BOARD_MEMBER_REMOVAL_ERROR", null));
        }
    }
}