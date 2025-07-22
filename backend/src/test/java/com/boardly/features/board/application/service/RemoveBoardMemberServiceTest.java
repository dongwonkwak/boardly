package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.features.board.application.validation.RemoveBoardMemberValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.model.BoardMemberId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import com.boardly.features.user.domain.model.UserId;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoveBoardMemberService 테스트")
class RemoveBoardMemberServiceTest {

    @Mock
    private RemoveBoardMemberValidator removeBoardMemberValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @InjectMocks
    private RemoveBoardMemberService removeBoardMemberService;

    private BoardId boardId;
    private UserId ownerId;
    private UserId targetUserId;
    private UserId requestedBy;
    private Board board;
    private BoardMember boardMember;
    private RemoveBoardMemberCommand command;

    @BeforeEach
    void setUp() {
        boardId = new BoardId("board-1");
        ownerId = new UserId("owner-1");
        targetUserId = new UserId("target-1");
        requestedBy = new UserId("requested-1");

        board = Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .ownerId(ownerId)
                .isArchived(false)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        boardMember = BoardMember.builder()
                .memberId(new BoardMemberId("member-1"))
                .boardId(boardId)
                .userId(targetUserId)
                .role(BoardRole.EDITOR)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        command = new RemoveBoardMemberCommand(boardId, targetUserId, requestedBy);
    }

    @Test
    @DisplayName("보드 멤버 삭제 성공")
    void removeBoardMember_Success() {
        // given
        ValidationResult<RemoveBoardMemberCommand> validationResult = ValidationResult.valid(command);
        when(removeBoardMemberValidator.validate(command)).thenReturn(validationResult);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                .thenReturn(Either.right(true));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId))
                .thenReturn(Optional.of(boardMember));
        when(boardMemberRepository.countActiveByBoardId(boardId)).thenReturn(2L);
        when(boardMemberRepository.save(any(BoardMember.class)))
                .thenReturn(Either.right(boardMember));

        // when
        Either<Failure, Void> result = removeBoardMemberService.removeBoardMember(command);

        // then
        assertThat(result.isRight()).isTrue();
        verify(boardMemberRepository).save(any(BoardMember.class));
        // boardMember는 실제 객체이므로 verify할 수 없음
    }

    @Test
    @DisplayName("입력 검증 실패")
    void removeBoardMember_ValidationFailure() {
        // given
        ValidationResult<RemoveBoardMemberCommand> validationResult = ValidationResult.invalid("validation error",
                "INVALID_INPUT", null);
        when(removeBoardMemberValidator.validate(command)).thenReturn(validationResult);
        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력 데이터가 올바르지 않습니다");

        // when
        Either<Failure, Void> result = removeBoardMemberService.removeBoardMember(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("입력 데이터가 올바르지 않습니다");
    }

    @Test
    @DisplayName("보드를 찾을 수 없음")
    void removeBoardMember_BoardNotFound() {
        // given
        ValidationResult<RemoveBoardMemberCommand> validationResult = ValidationResult.valid(command);
        when(removeBoardMemberValidator.validate(command)).thenReturn(validationResult);
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("validation.board.not.found"))
                .thenReturn("보드를 찾을 수 없습니다");

        // when
        Either<Failure, Void> result = removeBoardMemberService.removeBoardMember(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("보드를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("아카이브된 보드에서 멤버 삭제 시도")
    void removeBoardMember_ArchivedBoard() {
        // given
        Board archivedBoard = Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .ownerId(ownerId)
                .isArchived(true)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ValidationResult<RemoveBoardMemberCommand> validationResult = ValidationResult.valid(command);
        when(removeBoardMemberValidator.validate(command)).thenReturn(validationResult);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(archivedBoard));
        when(validationMessageResolver.getMessage("validation.board.archived.modification.denied"))
                .thenReturn("아카이브된 보드는 수정할 수 없습니다");

        // when
        Either<Failure, Void> result = removeBoardMemberService.removeBoardMember(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("아카이브된 보드는 수정할 수 없습니다");
    }

    @Test
    @DisplayName("멤버 관리 권한 없음")
    void removeBoardMember_NoManagementPermission() {
        // given
        ValidationResult<RemoveBoardMemberCommand> validationResult = ValidationResult.valid(command);
        when(removeBoardMemberValidator.validate(command)).thenReturn(validationResult);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                .thenReturn(Either.right(false));
        when(validationMessageResolver.getMessage("validation.board.member.management.denied"))
                .thenReturn("보드 멤버 관리 권한이 없습니다");

        // when
        Either<Failure, Void> result = removeBoardMemberService.removeBoardMember(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("보드 멤버 관리 권한이 없습니다");
    }

    @Test
    @DisplayName("보드 멤버를 찾을 수 없음")
    void removeBoardMember_MemberNotFound() {
        // given
        ValidationResult<RemoveBoardMemberCommand> validationResult = ValidationResult.valid(command);
        when(removeBoardMemberValidator.validate(command)).thenReturn(validationResult);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                .thenReturn(Either.right(true));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId))
                .thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("validation.board.member.not.found"))
                .thenReturn("보드 멤버를 찾을 수 없습니다");

        // when
        Either<Failure, Void> result = removeBoardMemberService.removeBoardMember(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("보드 멤버를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("OWNER 역할 멤버 삭제 시도")
    void removeBoardMember_OwnerRoleRemoval() {
        // given
        BoardMember ownerMember = BoardMember.builder()
                .memberId(new BoardMemberId("member-1"))
                .boardId(boardId)
                .userId(targetUserId)
                .role(BoardRole.OWNER)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ValidationResult<RemoveBoardMemberCommand> validationResult = ValidationResult.valid(command);
        when(removeBoardMemberValidator.validate(command)).thenReturn(validationResult);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                .thenReturn(Either.right(true));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId))
                .thenReturn(Optional.of(ownerMember));
        when(validationMessageResolver.getMessage("validation.board.member.owner.removal.denied"))
                .thenReturn("OWNER 역할을 가진 멤버는 삭제할 수 없습니다");

        // when
        Either<Failure, Void> result = removeBoardMemberService.removeBoardMember(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("OWNER 역할을 가진 멤버는 삭제할 수 없습니다");
    }

    @Test
    @DisplayName("마지막 멤버 삭제 시도")
    void removeBoardMember_LastMemberRemoval() {
        // given
        ValidationResult<RemoveBoardMemberCommand> validationResult = ValidationResult.valid(command);
        when(removeBoardMemberValidator.validate(command)).thenReturn(validationResult);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                .thenReturn(Either.right(true));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId))
                .thenReturn(Optional.of(boardMember));
        when(boardMemberRepository.countActiveByBoardId(boardId)).thenReturn(1L);
        when(validationMessageResolver.getMessage("validation.board.member.last.member.removal.denied"))
                .thenReturn("보드의 마지막 멤버는 삭제할 수 없습니다");

        // when
        Either<Failure, Void> result = removeBoardMemberService.removeBoardMember(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("보드의 마지막 멤버는 삭제할 수 없습니다");
    }

    @Test
    @DisplayName("자기 자신 삭제 시도")
    void removeBoardMember_SelfRemoval() {
        // given
        RemoveBoardMemberCommand selfRemovalCommand = new RemoveBoardMemberCommand(boardId, requestedBy, requestedBy);
        ValidationResult<RemoveBoardMemberCommand> validationResult = ValidationResult.valid(selfRemovalCommand);
        when(removeBoardMemberValidator.validate(selfRemovalCommand)).thenReturn(validationResult);
        when(validationMessageResolver.getMessage("validation.board.member.self.removal.denied"))
                .thenReturn("자기 자신을 삭제할 수 없습니다");

        // when
        Either<Failure, Void> result = removeBoardMemberService.removeBoardMember(selfRemovalCommand);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("자기 자신을 삭제할 수 없습니다");
    }
}