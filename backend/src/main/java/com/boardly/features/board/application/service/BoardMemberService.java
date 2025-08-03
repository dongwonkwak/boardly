package com.boardly.features.board.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.application.dto.BoardNameDto;
import com.boardly.features.board.application.port.input.AddBoardMemberCommand;
import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.features.board.application.port.input.UpdateBoardMemberRoleCommand;
import com.boardly.features.board.application.usecase.AddBoardMemberUseCase;
import com.boardly.features.board.application.usecase.RemoveBoardMemberUseCase;
import com.boardly.features.board.application.usecase.UpdateBoardMemberRoleUseCase;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.application.dto.UserNameDto;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.repository.UserRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보드 멤버 관리 서비스
 * 
 * <p>
 * 보드 멤버의 추가, 제거, 역할 변경 등 멤버 관리 작업을 담당하는 통합 서비스입니다.
 * </p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardMemberService implements
        AddBoardMemberUseCase,
        RemoveBoardMemberUseCase,
        UpdateBoardMemberRoleUseCase {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardPermissionService boardPermissionService;
    private final ValidationMessageResolver messageResolver;
    private final UserFinder userFinder;
    private final UserRepository userRepository;
    private final BoardValidator boardValidator;
    private final ActivityHelper activityHelper;

    // ==================== CACHE METHODS ====================

    /**
     * 사용자 이름 정보를 캐시하여 조회
     */
    @Cacheable(value = "userNames", key = "#userId.id")
    private UserNameDto getUserName(UserId userId) {
        return userRepository.findUserNameById(userId)
                .orElse(UserNameDto.defaultUser());
    }

    /**
     * 보드 이름 정보를 캐시하여 조회
     */
    @Cacheable(value = "boardNames", key = "#boardId.id")
    private String getBoardName(BoardId boardId) {
        return boardRepository.findBoardNameById(boardId)
                .orElse(BoardNameDto.defaultBoard())
                .title();
    }

    // ==================== ADD BOARD MEMBER ====================

    @Override
    public Either<Failure, BoardMember> addBoardMember(AddBoardMemberCommand command) {
        log.info("보드 멤버 추가 시작: boardId={}, userId={}, role={}, requestedBy={}",
                command.boardId() != null ? command.boardId().getId() : "null",
                command.userId() != null ? command.userId().getId() : "null",
                command.role(),
                command.requestedBy() != null ? command.requestedBy().getId() : "null");

        return validateRequestedUser(command.requestedBy())
                .flatMap(ignored -> validateAddMemberInput(command))
                .flatMap(this::findExistingBoard)
                .flatMap(board -> verifyManagementPermission(command, board))
                .flatMap(board -> checkMemberAlreadyExists(command, board))
                .flatMap(cmd -> createBoardMember(cmd))
                .flatMap(boardMember -> saveBoardMemberWithActivity(boardMember, command));
    }

    private Either<Failure, AddBoardMemberCommand> validateAddMemberInput(AddBoardMemberCommand command) {
        var validationResult = boardValidator.validateAddMember(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 멤버 추가 검증 실패: boardId={}, violations={}",
                    command.boardId() != null ? command.boardId().getId() : "null",
                    validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

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

    private Either<Failure, Board> verifyManagementPermission(AddBoardMemberCommand command, Board board) {
        return boardPermissionService.canManageBoardMembers(board.getBoardId(), command.requestedBy())
                .flatMap(canManage -> {
                    if (!canManage) {
                        log.warn("보드 멤버 관리 권한 없음: boardId={}, requestedBy={}",
                                board.getBoardId().getId(),
                                command.requestedBy().getId());
                        return Either.left(Failure.ofPermissionDenied(
                                messageResolver.getMessage(
                                        "validation.board.member.management.denied")));
                    }
                    return Either.right(board);
                });
    }

    private Either<Failure, AddBoardMemberCommand> checkMemberAlreadyExists(AddBoardMemberCommand command,
            Board board) {
        boolean exists = boardMemberRepository.existsByBoardIdAndUserId(
                board.getBoardId(), command.userId());

        if (exists) {
            log.warn("이미 보드 멤버로 등록된 사용자: boardId={}, userId={}",
                    board.getBoardId().getId(), command.userId().getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.board.member.already.exists")));
        }

        return Either.right(command);
    }

    private Either<Failure, BoardMember> createBoardMember(AddBoardMemberCommand command) {
        try {
            BoardMember boardMember = BoardMember.create(
                    command.boardId(),
                    command.userId(),
                    command.role());

            log.debug("보드 멤버 생성 완료: boardId={}, userId={}, role={}",
                    command.boardId().getId(), command.userId().getId(), command.role());

            return Either.right(boardMember);
        } catch (Exception e) {
            log.error("보드 멤버 생성 중 오류 발생: boardId={}, error={}",
                    command.boardId().getId(), e.getMessage(), e);
            return Either.left(
                    Failure.ofInternalError(e.getMessage(), "BOARD_MEMBER_CREATION_ERROR", null));
        }
    }

    private Either<Failure, BoardMember> saveBoardMemberWithActivity(BoardMember boardMember,
            AddBoardMemberCommand command) {
        return saveBoardMember(boardMember)
                .peek(savedMember -> logBoardMemberAddedActivity(savedMember, command));
    }

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

    private void logBoardMemberAddedActivity(BoardMember savedMember, AddBoardMemberCommand command) {
        // 활동 로그 기록 - 캐시된 DTO 사용으로 쿼리 최소화
        UserNameDto userName = getUserName(savedMember.getUserId());
        String boardName = getBoardName(savedMember.getBoardId());

        activityHelper.logBoardActivity(
                ActivityType.BOARD_ADD_MEMBER,
                command.requestedBy(),
                Map.of(
                        "memberId", savedMember.getUserId().getId(),
                        "memberFirstName", userName.firstName(),
                        "memberLastName", userName.lastName(),
                        "boardName", boardName),
                boardName,
                savedMember.getBoardId());
    }

    private Either<Failure, Void> validateRequestedUser(UserId requestedBy) {
        if (requestedBy != null && !userFinder.checkUserExists(requestedBy)) {
            return Either.left(Failure.ofNotFound(messageResolver.getMessage("validation.user.not.found")));
        }
        return Either.right((Void) null);
    }

    // ==================== REMOVE BOARD MEMBER ====================

    @Override
    public Either<Failure, Void> removeBoardMember(RemoveBoardMemberCommand command) {
        log.info("보드 멤버 제거 시작: boardId={}, targetUserId={}, requestedBy={}",
                command.boardId() != null ? command.boardId().getId() : "null",
                command.targetUserId() != null ? command.targetUserId().getId() : "null",
                command.requestedBy() != null ? command.requestedBy().getId() : "null");

        return validateRequestedUser(command.requestedBy())
                .flatMap(ignored -> validateRemoveMemberInput(command))
                .flatMap(this::findBoardForMemberRemoval)
                .flatMap(board -> verifyRemovePermission(command, board))
                .flatMap(board -> findExistingMember(command, board))
                .flatMap(member -> performMemberRemovalWithActivity(member, command));
    }

    private Either<Failure, RemoveBoardMemberCommand> validateRemoveMemberInput(RemoveBoardMemberCommand command) {
        var validationResult = boardValidator.validateRemoveMember(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 멤버 제거 검증 실패: boardId={}, violations={}",
                    command.boardId() != null ? command.boardId().getId() : "null",
                    validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    private Either<Failure, Board> findBoardForMemberRemoval(RemoveBoardMemberCommand command) {
        Optional<Board> boardOpt = boardRepository.findById(command.boardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.not.found")));
        }

        Board board = boardOpt.get();
        if (board.isArchived()) {
            log.warn("아카이브된 보드에서 멤버 제거 시도: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.board.archived.modification.denied")));
        }

        return Either.right(board);
    }

    private Either<Failure, Board> verifyRemovePermission(RemoveBoardMemberCommand command, Board board) {
        return boardPermissionService.canManageBoardMembers(board.getBoardId(), command.requestedBy())
                .flatMap(canManage -> {
                    if (!canManage) {
                        log.warn("보드 멤버 관리 권한 없음: boardId={}, requestedBy={}",
                                board.getBoardId().getId(),
                                command.requestedBy().getId());
                        return Either.left(Failure.ofPermissionDenied(
                                messageResolver.getMessage(
                                        "validation.board.member.management.denied")));
                    }
                    return Either.right(board);
                });
    }

    private Either<Failure, BoardMember> findExistingMember(RemoveBoardMemberCommand command, Board board) {
        Optional<BoardMember> memberOpt = boardMemberRepository.findByBoardIdAndUserId(
                board.getBoardId(), command.targetUserId());

        if (memberOpt.isEmpty()) {
            log.warn("보드 멤버를 찾을 수 없음: boardId={}, targetUserId={}",
                    board.getBoardId().getId(), command.targetUserId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.member.not.found")));
        }

        BoardMember member = memberOpt.get();

        // 보드 소유자는 제거할 수 없음
        if (board.getOwnerId().equals(command.targetUserId())) {
            log.warn("보드 소유자 제거 시도: boardId={}, targetUserId={}",
                    board.getBoardId().getId(), command.targetUserId().getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.board.owner.removal.denied")));
        }

        return Either.right(member);
    }

    private Either<Failure, Void> performMemberRemovalWithActivity(BoardMember member,
            RemoveBoardMemberCommand command) {
        return logBoardMemberRemovedActivity(member, command)
                .flatMap(ignored -> deleteBoardMember(member));
    }

    private Either<Failure, Void> logBoardMemberRemovedActivity(BoardMember member,
            RemoveBoardMemberCommand command) {
        // 활동 로그 기록 (삭제 전에 기록) - 캐시된 DTO 사용으로 쿼리 최소화
        UserNameDto userName = getUserName(member.getUserId());
        String boardName = getBoardName(member.getBoardId());

        activityHelper.logBoardActivity(
                ActivityType.BOARD_REMOVE_MEMBER,
                command.requestedBy(),
                Map.of(
                        "memberId", member.getUserId().getId(),
                        "memberFirstName", userName.firstName(),
                        "memberLastName", userName.lastName(),
                        "boardName", boardName),
                boardName,
                member.getBoardId());

        return Either.right((Void) null);
    }

    private Either<Failure, Void> deleteBoardMember(BoardMember member) {
        return boardMemberRepository.delete(member.getMemberId())
                .peek(deletedMember -> {
                    log.info("보드 멤버 제거 완료: memberId={}, boardId={}, userId={}",
                            member.getMemberId().getId(),
                            member.getBoardId().getId(),
                            member.getUserId().getId());
                })
                .map(deletedMember -> (Void) null);
    }

    // ==================== UPDATE BOARD MEMBER ROLE ====================

    @Override
    public Either<Failure, BoardMember> updateBoardMemberRole(UpdateBoardMemberRoleCommand command) {
        log.info("보드 멤버 역할 변경 시작: boardId={}, targetUserId={}, newRole={}, requestedBy={}",
                command.boardId() != null ? command.boardId().getId() : "null",
                command.targetUserId() != null ? command.targetUserId().getId() : "null",
                command.newRole(),
                command.requestedBy() != null ? command.requestedBy().getId() : "null");

        return validateRequestedUser(command.requestedBy())
                .flatMap(ignored -> validateUpdateRoleInput(command))
                .flatMap(this::findBoardForRoleUpdate)
                .flatMap(board -> verifyRoleUpdatePermission(command, board))
                .flatMap(board -> findMemberForRoleUpdate(command, board))
                .flatMap(member -> performRoleUpdate(member, command));
    }

    private Either<Failure, UpdateBoardMemberRoleCommand> validateUpdateRoleInput(
            UpdateBoardMemberRoleCommand command) {
        var validationResult = boardValidator.validateUpdateMemberRole(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 멤버 역할 변경 검증 실패: boardId={}, violations={}",
                    command.boardId() != null ? command.boardId().getId() : "null",
                    validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    private Either<Failure, Board> findBoardForRoleUpdate(UpdateBoardMemberRoleCommand command) {
        Optional<Board> boardOpt = boardRepository.findById(command.boardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.not.found")));
        }

        Board board = boardOpt.get();
        if (board.isArchived()) {
            log.warn("아카이브된 보드에서 멤버 역할 변경 시도: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.board.archived.modification.denied")));
        }

        return Either.right(board);
    }

    private Either<Failure, Board> verifyRoleUpdatePermission(UpdateBoardMemberRoleCommand command, Board board) {
        return boardPermissionService.canManageBoardMembers(board.getBoardId(), command.requestedBy())
                .flatMap(canManage -> {
                    if (!canManage) {
                        log.warn("보드 멤버 관리 권한 없음: boardId={}, requestedBy={}",
                                board.getBoardId().getId(),
                                command.requestedBy().getId());
                        return Either.left(Failure.ofPermissionDenied(
                                messageResolver.getMessage(
                                        "validation.board.member.management.denied")));
                    }
                    return Either.right(board);
                });
    }

    private Either<Failure, BoardMember> findMemberForRoleUpdate(UpdateBoardMemberRoleCommand command,
            Board board) {
        return findMemberByBoardAndUser(command, board)
                .flatMap(member -> validateRoleChangeAllowed(command, board, member))
                .flatMap(member -> checkRoleChangeNeeded(command, member));
    }

    private Either<Failure, BoardMember> findMemberByBoardAndUser(UpdateBoardMemberRoleCommand command,
            Board board) {
        Optional<BoardMember> memberOpt = boardMemberRepository.findByBoardIdAndUserId(
                board.getBoardId(), command.targetUserId());

        if (memberOpt.isEmpty()) {
            log.warn("보드 멤버를 찾을 수 없음: boardId={}, targetUserId={}",
                    board.getBoardId().getId(), command.targetUserId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.member.not.found")));
        }

        return Either.right(memberOpt.get());
    }

    private Either<Failure, BoardMember> validateRoleChangeAllowed(UpdateBoardMemberRoleCommand command,
            Board board, BoardMember member) {
        // 보드 소유자의 역할은 변경할 수 없음
        if (board.getOwnerId().equals(command.targetUserId())) {
            log.warn("보드 소유자 역할 변경 시도: boardId={}, targetUserId={}",
                    board.getBoardId().getId(), command.targetUserId().getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.board.owner.role.change.denied")));
        }

        return Either.right(member);
    }

    private Either<Failure, BoardMember> checkRoleChangeNeeded(UpdateBoardMemberRoleCommand command,
            BoardMember member) {
        // 같은 역할로 변경하려는 경우 - 활동 로그만 기록하고 멤버 반환
        if (member.getRole().equals(command.newRole())) {
            log.info("멤버가 이미 해당 역할을 가지고 있음: boardId={}, targetUserId={}, role={}",
                    member.getBoardId().getId(), command.targetUserId().getId(), command.newRole());

            // 활동 로그 기록 (변경 없이도 기록)
            UserNameDto userName = getUserName(member.getUserId());
            String boardName = getBoardName(member.getBoardId());

            activityHelper.logBoardActivity(
                    ActivityType.BOARD_UPDATE_MEMBER_ROLE,
                    command.requestedBy(),
                    Map.of(
                            "memberId", member.getUserId().getId(),
                            "memberFirstName", userName.firstName(),
                            "memberLastName", userName.lastName(),
                            "oldRole", member.getRole().toString(),
                            "newRole", member.getRole().toString(),
                            "boardName", boardName,
                            "noChange", "true"),
                    boardName,
                    member.getBoardId());

            return Either.right(member);
        }

        // 다른 역할로 변경하는 경우 - performRoleUpdate에서 처리
        return Either.right(member);
    }

    private Either<Failure, BoardMember> performRoleUpdate(BoardMember member,
            UpdateBoardMemberRoleCommand command) {
        return changeMemberRole(member, command)
                .flatMap(updatedMember -> saveUpdatedMember(updatedMember, command));
    }

    private Either<Failure, BoardMember> changeMemberRole(BoardMember member,
            UpdateBoardMemberRoleCommand command) {
        try {
            // 역할 변경 전에 기존 역할 저장
            var oldRole = member.getRole();

            // 새로운 역할로 변경
            member.changeRole(command.newRole());
            log.debug("보드 멤버 역할 변경 처리 완료: memberId={}, oldRole={}, newRole={}",
                    member.getMemberId().getId(), oldRole, command.newRole());

            return Either.right(member);
        } catch (Exception e) {
            log.error("보드 멤버 역할 변경 중 오류 발생: memberId={}, error={}",
                    member.getMemberId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalError(e.getMessage(), "BOARD_MEMBER_ROLE_UPDATE_ERROR",
                    null));
        }
    }

    private Either<Failure, BoardMember> saveUpdatedMember(BoardMember member,
            UpdateBoardMemberRoleCommand command) {
        return boardMemberRepository.save(member)
                .peek(savedMember -> {
                    log.info("보드 멤버 역할 변경 완료: memberId={}, boardId={}, userId={}, oldRole={}, newRole={}",
                            savedMember.getMemberId().getId(),
                            savedMember.getBoardId().getId(),
                            savedMember.getUserId().getId(),
                            member.getRole(), // 변경 전 역할
                            savedMember.getRole());

                    // 활동 로그 기록 - 캐시된 DTO 사용으로 쿼리 최소화
                    UserNameDto userName = getUserName(savedMember.getUserId());
                    String boardName = getBoardName(savedMember.getBoardId());

                    activityHelper.logBoardActivity(
                            ActivityType.BOARD_UPDATE_MEMBER_ROLE,
                            command.requestedBy(),
                            Map.of(
                                    "memberId",
                                    savedMember.getUserId().getId(),
                                    "memberFirstName", userName.firstName(),
                                    "memberLastName", userName.lastName(),
                                    "oldRole", member.getRole().toString(), // 변경 전 역할
                                    "newRole",
                                    savedMember.getRole().toString(),
                                    "boardName", boardName),
                            boardName,
                            savedMember.getBoardId());
                });
    }
}