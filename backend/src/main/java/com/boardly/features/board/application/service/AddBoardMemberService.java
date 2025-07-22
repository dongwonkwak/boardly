package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.AddBoardMemberCommand;
import com.boardly.features.board.application.usecase.AddBoardMemberUseCase;
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
 * 보드 멤버 추가 서비스
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AddBoardMemberService implements AddBoardMemberUseCase {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardPermissionService boardPermissionService;
    private final ValidationMessageResolver messageResolver;

    @Override
    public Either<Failure, BoardMember> addBoardMember(AddBoardMemberCommand command) {
        log.info("보드 멤버 추가 시작: boardId={}, userId={}, role={}, requestedBy={}",
                command.boardId().getId(), command.userId().getId(), command.role(), command.requestedBy().getId());

        return validateInput(command)
                .flatMap(this::findExistingBoard)
                .flatMap(this::verifyManagementPermission)
                .flatMap(this::checkMemberAlreadyExists)
                .flatMap(this::createBoardMember)
                .flatMap(this::saveBoardMember);
    }

    /**
     * 1단계: 입력 검증
     */
    private Either<Failure, AddBoardMemberCommand> validateInput(AddBoardMemberCommand command) {
        if (command.boardId() == null) {
            log.warn("보드 ID가 null입니다");
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.board.id.required")));
        }

        if (command.userId() == null) {
            log.warn("사용자 ID가 null입니다");
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.user.id.required")));
        }

        if (command.role() == null) {
            log.warn("멤버 역할이 null입니다");
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.board.member.role.invalid")));
        }

        if (command.requestedBy() == null) {
            log.warn("요청자 ID가 null입니다");
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.user.id.required")));
        }

        return Either.right(command);
    }

    /**
     * 2단계: 보드 존재 확인
     */
    private Either<Failure, Board> findExistingBoard(AddBoardMemberCommand command) {
        Optional<Board> boardOpt = boardRepository.findById(command.boardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.not.found")));
        }

        Board board = boardOpt.get();
        if (board.isArchived()) {
            log.warn("아카이브된 보드에 멤버 추가 시도: boardId={}", command.boardId().getId());
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
     * 4단계: 멤버가 이미 존재하는지 확인
     */
    private Either<Failure, Board> checkMemberAlreadyExists(Board board) {
        boolean exists = boardMemberRepository.existsByBoardIdAndUserId(
                board.getBoardId(), board.getOwnerId());

        if (exists) {
            log.warn("이미 보드 멤버로 등록된 사용자: boardId={}, userId={}",
                    board.getBoardId().getId(), board.getOwnerId().getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.board.member.already.exists")));
        }

        return Either.right(board);
    }

    /**
     * 5단계: 보드 멤버 생성
     */
    private Either<Failure, BoardMember> createBoardMember(Board board) {
        try {
            BoardMember boardMember = BoardMember.create(
                    board.getBoardId(),
                    board.getOwnerId(),
                    BoardRole.EDITOR); // 기본 역할은 EDITOR로 설정

            log.debug("보드 멤버 생성 완료: boardId={}, userId={}, role={}",
                    board.getBoardId().getId(), board.getOwnerId().getId(), BoardRole.EDITOR);

            return Either.right(boardMember);
        } catch (Exception e) {
            log.error("보드 멤버 생성 중 오류 발생: boardId={}, error={}",
                    board.getBoardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalError(e.getMessage(), "BOARD_MEMBER_CREATION_ERROR", null));
        }
    }

    /**
     * 6단계: 보드 멤버 저장
     */
    private Either<Failure, BoardMember> saveBoardMember(BoardMember boardMember) {
        return boardMemberRepository.save(boardMember)
                .peek(savedMember -> {
                    log.info("보드 멤버 추가 완료: memberId={}, boardId={}, userId={}, role={}",
                            savedMember.getMemberId().getId(),
                            savedMember.getBoardId().getId(),
                            savedMember.getUserId().getId(),
                            savedMember.getRole());
                });
    }
}