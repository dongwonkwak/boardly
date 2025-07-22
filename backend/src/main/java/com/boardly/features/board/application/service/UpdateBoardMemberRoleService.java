package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.UpdateBoardMemberRoleCommand;
import com.boardly.features.board.application.usecase.UpdateBoardMemberRoleUseCase;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 보드 멤버 역할 수정 서비스
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateBoardMemberRoleService implements UpdateBoardMemberRoleUseCase {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardPermissionService boardPermissionService;
    private final ValidationMessageResolver messageResolver;

    @Override
    public Either<Failure, BoardMember> updateBoardMemberRole(UpdateBoardMemberRoleCommand command) {
        log.info("보드 멤버 역할 수정 시작: boardId={}, targetUserId={}, newRole={}, requestedBy={}",
                command.boardId().getId(), command.targetUserId().getId(), command.newRole(),
                command.requestedBy().getId());

        return validateInput(command)
                .flatMap(this::findExistingBoard)
                .flatMap(this::verifyManagementPermission)
                .flatMap(this::findExistingMember)
                .flatMap(this::validateRoleChange)
                .flatMap(this::updateMemberRole)
                .flatMap(this::saveUpdatedMember);
    }

    /**
     * 1단계: 입력 검증
     */
    private Either<Failure, UpdateBoardMemberRoleCommand> validateInput(UpdateBoardMemberRoleCommand command) {
        if (command.boardId() == null) {
            log.warn("보드 ID가 null입니다");
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.board.id.required")));
        }

        if (command.targetUserId() == null) {
            log.warn("대상 사용자 ID가 null입니다");
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.user.id.required")));
        }

        if (command.newRole() == null) {
            log.warn("새로운 역할이 null입니다");
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.board.member.role.invalid")));
        }

        if (command.requestedBy() == null) {
            log.warn("요청자 ID가 null입니다");
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.user.id.required")));
        }

        // 자기 자신의 역할을 변경하려는 경우 체크
        if (command.targetUserId().equals(command.requestedBy())) {
            log.warn("자기 자신의 역할을 변경하려고 시도: userId={}", command.targetUserId().getId());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.board.member.self.role.change.denied")));
        }

        return Either.right(command);
    }

    /**
     * 2단계: 보드 존재 확인
     */
    private Either<Failure, Board> findExistingBoard(UpdateBoardMemberRoleCommand command) {
        Optional<Board> boardOpt = boardRepository.findById(command.boardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.not.found")));
        }

        Board board = boardOpt.get();
        if (board.isArchived()) {
            log.warn("아카이브된 보드에서 멤버 역할 수정 시도: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.board.archived.modification.denied")));
        }

        return Either.right(board);
    }

    /**
     * 3단계: 멤버 관리 권한 확인
     */
    private Either<Failure, Board> verifyManagementPermission(Board board) {
        return boardPermissionService.canManageBoardMembers(board.getBoardId(), board.getOwnerId())
                .flatMap(canManage -> {
                    if (!canManage) {
                        log.warn("보드 멤버 관리 권한 없음: boardId={}, requestedBy={}",
                                board.getBoardId().getId(), board.getOwnerId().getId());
                        return Either.left(Failure.ofPermissionDenied(
                                messageResolver.getMessage("validation.board.member.management.denied")));
                    }
                    return Either.right(board);
                });
    }

    /**
     * 4단계: 대상 멤버 존재 확인
     */
    private Either<Failure, BoardMember> findExistingMember(Board board) {
        Optional<BoardMember> memberOpt = boardMemberRepository.findByBoardIdAndUserId(
                board.getBoardId(), board.getOwnerId());

        if (memberOpt.isEmpty()) {
            log.warn("보드 멤버를 찾을 수 없음: boardId={}, userId={}",
                    board.getBoardId().getId(), board.getOwnerId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.member.not.found")));
        }

        BoardMember member = memberOpt.get();
        if (!member.isActive()) {
            log.warn("비활성화된 보드 멤버: boardId={}, userId={}",
                    board.getBoardId().getId(), board.getOwnerId().getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.board.member.inactive")));
        }

        return Either.right(member);
    }

    /**
     * 5단계: 역할 변경 유효성 검증
     */
    private Either<Failure, BoardMember> validateRoleChange(BoardMember member) {
        // OWNER 역할로 변경하려는 경우 체크 (OWNER는 한 명만 존재해야 함)
        if (member.getRole() == BoardRole.OWNER) {
            log.warn("OWNER 역할을 다른 역할로 변경하려고 시도: memberId={}", member.getMemberId().getId());
            return Either.left(Failure.ofBusinessRuleViolation(
                    messageResolver.getMessage("validation.board.member.owner.role.change.denied"),
                    "OWNER_ROLE_CHANGE_DENIED",
                    null));
        }

        // 이미 같은 역할인 경우 체크
        if (member.getRole() == member.getRole()) {
            log.info("이미 같은 역할입니다: memberId={}, role={}",
                    member.getMemberId().getId(), member.getRole());
            return Either.right(member); // 변경 사항이 없어도 성공으로 처리
        }

        return Either.right(member);
    }

    /**
     * 6단계: 멤버 역할 업데이트
     */
    private Either<Failure, BoardMember> updateMemberRole(BoardMember member) {
        try {
            member.changeRole(member.getRole());
            log.debug("보드 멤버 역할 변경 완료: memberId={}, newRole={}",
                    member.getMemberId().getId(), member.getRole());
            return Either.right(member);
        } catch (Exception e) {
            log.error("보드 멤버 역할 변경 중 오류 발생: memberId={}, error={}",
                    member.getMemberId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalError(e.getMessage(), "BOARD_MEMBER_ROLE_UPDATE_ERROR", null));
        }
    }

    /**
     * 7단계: 업데이트된 멤버 저장
     */
    private Either<Failure, BoardMember> saveUpdatedMember(BoardMember member) {
        return boardMemberRepository.save(member)
                .peek(savedMember -> {
                    log.info("보드 멤버 역할 수정 완료: memberId={}, boardId={}, userId={}, newRole={}",
                            savedMember.getMemberId().getId(),
                            savedMember.getBoardId().getId(),
                            savedMember.getUserId().getId(),
                            savedMember.getRole());
                });
    }
}