package com.boardly.features.board.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 보드 권한을 관리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardPermissionService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final ValidationMessageResolver messageResolver;

    /**
     * 사용자의 보드 권한을 확인합니다.
     * 
     * @param boardId 보드 ID
     * @param userId  사용자 ID
     * @return 사용자의 보드 권한 (소유자인 경우 null, 멤버인 경우 해당 역할)
     */
    public Either<Failure, BoardRole> getUserBoardRole(BoardId boardId, UserId userId) {
        // 1. 보드 존재 확인
        Optional<Board> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", boardId.getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.not.found")));
        }

        Board board = boardOpt.get();

        // 2. 소유자인지 확인
        if (board.getOwnerId().equals(userId)) {
            return Either.right(null); // 소유자는 null 반환 (기존 로직과 호환)
        }

        // 3. 멤버인지 확인
        Optional<BoardMember> memberOpt = boardMemberRepository.findByBoardIdAndUserId(boardId, userId);
        if (memberOpt.isEmpty()) {
            log.warn("보드 멤버가 아님: boardId={}, userId={}", boardId.getId(), userId.getId());
            return Either.left(Failure.ofPermissionDenied(
                    messageResolver.getMessage("validation.board.access.denied")));
        }

        BoardMember member = memberOpt.get();
        if (!member.isActive()) {
            log.warn("비활성화된 보드 멤버: boardId={}, userId={}", boardId.getId(), userId.getId());
            return Either.left(Failure.ofPermissionDenied(
                    messageResolver.getMessage("validation.board.member.inactive")));
        }

        return Either.right(member.getRole());
    }

    /**
     * 사용자가 보드에 읽기 권한이 있는지 확인합니다.
     */
    public Either<Failure, Boolean> canReadBoard(BoardId boardId, UserId userId) {
        return getUserBoardRole(boardId, userId)
                .map(role -> {
                    Board board = boardRepository.findById(boardId).orElse(null);
                    if (board == null) {
                        return false;
                    }
                    return board.canAccessWithRole(userId, role);
                });
    }

    /**
     * 사용자가 보드에 쓰기 권한이 있는지 확인합니다.
     */
    public Either<Failure, Boolean> canWriteBoard(BoardId boardId, UserId userId) {
        return getUserBoardRole(boardId, userId)
                .map(role -> {
                    Board board = boardRepository.findById(boardId).orElse(null);
                    if (board == null) {
                        return false;
                    }
                    return board.canModifyWithRole(userId, role);
                });
    }

    /**
     * 사용자가 보드에 관리 권한이 있는지 확인합니다.
     */
    public Either<Failure, Boolean> canAdminBoard(BoardId boardId, UserId userId) {
        return getUserBoardRole(boardId, userId)
                .map(role -> {
                    Board board = boardRepository.findById(boardId).orElse(null);
                    if (board == null) {
                        return false;
                    }
                    return board.canManageBoardSettingsWithRole(userId, role);
                });
    }

    /**
     * 사용자가 보드 멤버를 관리할 수 있는지 확인합니다.
     */
    public Either<Failure, Boolean> canManageBoardMembers(BoardId boardId, UserId userId) {
        return getUserBoardRole(boardId, userId)
                .map(role -> {
                    Board board = boardRepository.findById(boardId).orElse(null);
                    if (board == null) {
                        return false;
                    }
                    return board.canManageMembersWithRole(userId, role);
                });
    }

    /**
     * 사용자가 보드를 아카이브할 수 있는지 확인합니다.
     */
    public Either<Failure, Boolean> canArchiveBoard(BoardId boardId, UserId userId) {
        return getUserBoardRole(boardId, userId)
                .map(role -> {
                    Board board = boardRepository.findById(boardId).orElse(null);
                    if (board == null) {
                        return false;
                    }
                    return board.canArchiveWithRole(userId, role);
                });
    }

    /**
     * 사용자가 보드의 즐겨찾기를 변경할 수 있는지 확인합니다.
     */
    public Either<Failure, Boolean> canToggleStarBoard(BoardId boardId, UserId userId) {
        return getUserBoardRole(boardId, userId)
                .map(role -> {
                    Board board = boardRepository.findById(boardId).orElse(null);
                    if (board == null) {
                        return false;
                    }
                    return board.canToggleStarWithRole(userId, role);
                });
    }
}