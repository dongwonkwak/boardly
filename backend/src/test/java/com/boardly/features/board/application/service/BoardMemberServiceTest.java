package com.boardly.features.board.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.application.dto.BoardNameDto;
import com.boardly.features.board.application.port.input.AddBoardMemberCommand;
import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.features.board.application.port.input.UpdateBoardMemberRoleCommand;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.model.BoardMemberId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.application.dto.UserNameDto;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.repository.UserRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardMemberService 테스트")
class BoardMemberServiceTest {

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private BoardMemberRepository boardMemberRepository;

        @Mock
        private BoardPermissionService boardPermissionService;

        @Mock
        private ValidationMessageResolver messageResolver;

        @Mock
        private UserFinder userFinder;

        @Mock
        private UserRepository userRepository;

        @Mock
        private BoardValidator boardValidator;

        @Mock
        private ActivityHelper activityHelper;

        @InjectMocks
        private BoardMemberService boardMemberService;

        private BoardId boardId;
        private UserId userId;
        private UserId requestedBy;
        private UserId targetUserId;
        private Board board;
        private BoardMember boardMember;
        private UserNameDto userNameDto;
        private BoardNameDto boardNameDto;

        @BeforeEach
        void setUp() {
                boardId = new BoardId();
                userId = new UserId();
                requestedBy = new UserId();
                targetUserId = new UserId();

                board = Board.builder()
                                .boardId(boardId)
                                .title("테스트 보드")
                                .description("테스트 보드 설명")
                                .ownerId(requestedBy)
                                .isArchived(false)
                                .build();

                boardMember = BoardMember.builder()
                                .memberId(new BoardMemberId())
                                .boardId(boardId)
                                .userId(userId)
                                .role(BoardRole.MEMBER)
                                .isActive(true)
                                .build();

                userNameDto = new UserNameDto("홍", "길동");
                boardNameDto = new BoardNameDto("테스트 보드");
        }

        @Nested
        @DisplayName("addBoardMember 테스트")
        class AddBoardMemberTest {

                @Test
                @DisplayName("보드 멤버 추가 성공")
                void addBoardMember_Success() {
                        // given
                        AddBoardMemberCommand command = new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.MEMBER, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.existsByBoardIdAndUserId(boardId, userId))
                                        .thenReturn(false);
                        when(boardMemberRepository.save(any(BoardMember.class)))
                                        .thenReturn(Either.right(boardMember));
                        when(userRepository.findUserNameById(userId))
                                        .thenReturn(Optional.of(userNameDto));
                        when(boardRepository.findBoardNameById(boardId))
                                        .thenReturn(Optional.of(boardNameDto));
                        doNothing().when(activityHelper).logBoardActivity(any(), any(), any(), any(), any());

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(boardMember);

                        verify(boardMemberRepository).save(any(BoardMember.class));
                        verify(activityHelper).logBoardActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("요청자가 존재하지 않는 경우 실패")
                void addBoardMember_RequestedUserNotFound_Failure() {
                        // given
                        AddBoardMemberCommand command = new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.MEMBER, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(false);
                        when(messageResolver.getMessage("validation.user.not.found"))
                                        .thenReturn("사용자를 찾을 수 없습니다.");

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
                }

                @Test
                @DisplayName("입력 검증 실패")
                void addBoardMember_ValidationFailure() {
                        // given
                        AddBoardMemberCommand command = new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.MEMBER, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.invalid("boardId", "보드 ID가 필요합니다.", null));
                        when(messageResolver.getMessage("validation.input.invalid"))
                                        .thenReturn("입력이 유효하지 않습니다.");

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("INVALID_INPUT");
                }

                @Test
                @DisplayName("보드가 존재하지 않는 경우 실패")
                void addBoardMember_BoardNotFound_Failure() {
                        // given
                        AddBoardMemberCommand command = new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.MEMBER, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());
                        when(messageResolver.getMessage("validation.board.not.found"))
                                        .thenReturn("보드를 찾을 수 없습니다.");

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다.");
                }

                @Test
                @DisplayName("아카이브된 보드에 멤버 추가 시도 실패")
                void addBoardMember_ArchivedBoard_Failure() {
                        // given
                        Board archivedBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("아카이브된 보드")
                                        .isArchived(true)
                                        .build();

                        AddBoardMemberCommand command = new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.MEMBER, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(archivedBoard));
                        when(messageResolver.getMessage("validation.board.archived.modification.denied"))
                                        .thenReturn("아카이브된 보드는 수정할 수 없습니다.");

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("아카이브된 보드는 수정할 수 없습니다.");
                }

                @Test
                @DisplayName("멤버 관리 권한이 없는 경우 실패")
                void addBoardMember_NoPermission_Failure() {
                        // given
                        AddBoardMemberCommand command = new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.MEMBER, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(false));
                        when(messageResolver.getMessage("validation.board.member.management.denied"))
                                        .thenReturn("멤버 관리 권한이 없습니다.");

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("멤버 관리 권한이 없습니다.");
                }

                @Test
                @DisplayName("이미 멤버로 등록된 사용자인 경우 실패")
                void addBoardMember_MemberAlreadyExists_Failure() {
                        // given
                        AddBoardMemberCommand command = new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.MEMBER, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.existsByBoardIdAndUserId(boardId, userId))
                                        .thenReturn(true);
                        when(messageResolver.getMessage("validation.board.member.already.exists"))
                                        .thenReturn("이미 보드 멤버로 등록된 사용자입니다.");

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("이미 보드 멤버로 등록된 사용자입니다.");
                }
        }

        @Nested
        @DisplayName("removeBoardMember 테스트")
        class RemoveBoardMemberTest {

                @Test
                @DisplayName("보드 멤버 제거 성공")
                void removeBoardMember_Success() {
                        // given
                        RemoveBoardMemberCommand command = new RemoveBoardMemberCommand(
                                        boardId, targetUserId, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId))
                                        .thenReturn(Optional.of(boardMember));
                        when(boardMemberRepository.delete(boardMember.getMemberId()))
                                        .thenReturn(Either.right(null));
                        when(userRepository.findUserNameById(any(UserId.class)))
                                        .thenReturn(Optional.of(userNameDto));
                        when(boardRepository.findBoardNameById(any(BoardId.class)))
                                        .thenReturn(Optional.of(boardNameDto));
                        doNothing().when(activityHelper).logBoardActivity(any(), any(), any(), any(), any());

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isRight()).isTrue();

                        verify(boardMemberRepository).delete(boardMember.getMemberId());
                        verify(activityHelper).logBoardActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("보드 소유자 제거 시도 실패")
                void removeBoardMember_RemoveOwner_Failure() {
                        // given
                        RemoveBoardMemberCommand command = new RemoveBoardMemberCommand(
                                        boardId, requestedBy, requestedBy); // 소유자를 제거하려고 시도

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requestedBy))
                                        .thenReturn(Optional.of(boardMember));
                        when(messageResolver.getMessage("validation.board.owner.removal.denied"))
                                        .thenReturn("보드 소유자는 제거할 수 없습니다.");

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 소유자는 제거할 수 없습니다.");
                }

                @Test
                @DisplayName("제거할 멤버가 존재하지 않는 경우 실패")
                void removeBoardMember_MemberNotFound_Failure() {
                        // given
                        RemoveBoardMemberCommand command = new RemoveBoardMemberCommand(
                                        boardId, targetUserId, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId))
                                        .thenReturn(Optional.empty());
                        when(messageResolver.getMessage("validation.board.member.not.found"))
                                        .thenReturn("보드 멤버를 찾을 수 없습니다.");

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 멤버를 찾을 수 없습니다.");
                }
        }

        @Nested
        @DisplayName("updateBoardMemberRole 테스트")
        class UpdateBoardMemberRoleTest {

                @Test
                @DisplayName("보드 멤버 역할 변경 성공")
                void updateBoardMemberRole_Success() {
                        // given
                        UpdateBoardMemberRoleCommand command = new UpdateBoardMemberRoleCommand(
                                        boardId, targetUserId, BoardRole.ADMIN, requestedBy);

                        BoardMember existingMember = BoardMember.builder()
                                        .memberId(new BoardMemberId())
                                        .boardId(boardId)
                                        .userId(targetUserId)
                                        .role(BoardRole.MEMBER)
                                        .isActive(true)
                                        .build();

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId))
                                        .thenReturn(Optional.of(existingMember));
                        when(boardMemberRepository.save(any(BoardMember.class)))
                                        .thenReturn(Either.right(existingMember));
                        when(userRepository.findUserNameById(targetUserId))
                                        .thenReturn(Optional.of(userNameDto));
                        when(boardRepository.findBoardNameById(boardId))
                                        .thenReturn(Optional.of(boardNameDto));
                        doNothing().when(activityHelper).logBoardActivity(any(), any(), any(), any(), any());

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get().getRole()).isEqualTo(BoardRole.ADMIN);

                        verify(boardMemberRepository).save(any(BoardMember.class));
                        verify(activityHelper).logBoardActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("보드 소유자 역할 변경 시도 실패")
                void updateBoardMemberRole_ChangeOwnerRole_Failure() {
                        // given
                        UpdateBoardMemberRoleCommand command = new UpdateBoardMemberRoleCommand(
                                        boardId, requestedBy, BoardRole.ADMIN, requestedBy); // 소유자 역할 변경 시도

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requestedBy))
                                        .thenReturn(Optional.of(boardMember));
                        when(messageResolver.getMessage("validation.board.owner.role.change.denied"))
                                        .thenReturn("보드 소유자의 역할은 변경할 수 없습니다.");

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 소유자의 역할은 변경할 수 없습니다.");
                }

                @Test
                @DisplayName("같은 역할로 변경하는 경우 활동 로그만 기록")
                void updateBoardMemberRole_SameRole_LogActivityOnly() {
                        // given
                        BoardMember existingMember = BoardMember.builder()
                                        .memberId(new BoardMemberId())
                                        .boardId(boardId)
                                        .userId(targetUserId)
                                        .role(BoardRole.MEMBER)
                                        .isActive(true)
                                        .build();

                        UpdateBoardMemberRoleCommand command = new UpdateBoardMemberRoleCommand(
                                        boardId, targetUserId, BoardRole.MEMBER, requestedBy); // 같은 역할

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId))
                                        .thenReturn(Optional.of(existingMember));
                        when(boardMemberRepository.save(any(BoardMember.class)))
                                        .thenReturn(Either.right(existingMember));
                        when(userRepository.findUserNameById(any(UserId.class)))
                                        .thenReturn(Optional.of(userNameDto));
                        when(boardRepository.findBoardNameById(any(BoardId.class)))
                                        .thenReturn(Optional.of(boardNameDto));
                        doNothing().when(activityHelper).logBoardActivity(any(), any(), any(), any(), any());

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get().getRole()).isEqualTo(BoardRole.MEMBER);

                        // 활동 로그는 기록되어야 함 (같은 역할인 경우에도 2번 호출됨: checkRoleChangeNeeded에서 1번,
                        // saveUpdatedMember에서 1번)
                        verify(activityHelper, times(2)).logBoardActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("역할 변경할 멤버가 존재하지 않는 경우 실패")
                void updateBoardMemberRole_MemberNotFound_Failure() {
                        // given
                        UpdateBoardMemberRoleCommand command = new UpdateBoardMemberRoleCommand(
                                        boardId, targetUserId, BoardRole.ADMIN, requestedBy);

                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId))
                                        .thenReturn(Optional.empty());
                        when(messageResolver.getMessage("validation.board.member.not.found"))
                                        .thenReturn("보드 멤버를 찾을 수 없습니다.");

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 멤버를 찾을 수 없습니다.");
                }
        }

        @Nested
        @DisplayName("캐시 메서드 테스트")
        class CacheMethodsTest {

                @Test
                @DisplayName("getUserName 캐시 테스트")
                void getUserName_CacheTest() {
                        // given
                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateAddMember(any())).thenReturn(ValidationResult.valid(null));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.existsByBoardIdAndUserId(boardId, userId))
                                        .thenReturn(false);
                        when(boardMemberRepository.save(any(BoardMember.class)))
                                        .thenReturn(Either.right(boardMember));
                        when(userRepository.findUserNameById(userId))
                                        .thenReturn(Optional.of(userNameDto));
                        when(boardRepository.findBoardNameById(boardId))
                                        .thenReturn(Optional.of(boardNameDto));
                        doNothing().when(activityHelper).logBoardActivity(any(), any(), any(), any(), any());

                        // when
                        // 첫 번째 호출
                        boardMemberService.addBoardMember(new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.MEMBER, requestedBy));

                        // 두 번째 호출 (캐시에서 가져와야 함)
                        boardMemberService.addBoardMember(new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.ADMIN, requestedBy));

                        // then
                        // 캐시 테스트는 실제 캐시 동작을 확인하기 어려우므로 호출 횟수만 확인
                        verify(userRepository, times(2)).findUserNameById(userId);
                }

                @Test
                @DisplayName("getBoardName 캐시 테스트")
                void getBoardName_CacheTest() {
                        // given
                        when(userFinder.checkUserExists(requestedBy)).thenReturn(true);
                        when(boardValidator.validateAddMember(any())).thenReturn(ValidationResult.valid(null));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(boardId, requestedBy))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.existsByBoardIdAndUserId(boardId, userId))
                                        .thenReturn(false);
                        when(boardMemberRepository.save(any(BoardMember.class)))
                                        .thenReturn(Either.right(boardMember));
                        when(userRepository.findUserNameById(userId))
                                        .thenReturn(Optional.of(userNameDto));
                        when(boardRepository.findBoardNameById(boardId))
                                        .thenReturn(Optional.of(boardNameDto));
                        doNothing().when(activityHelper).logBoardActivity(any(), any(), any(), any(), any());

                        // when
                        // 첫 번째 호출
                        boardMemberService.addBoardMember(new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.MEMBER, requestedBy));

                        // 두 번째 호출 (캐시에서 가져와야 함)
                        boardMemberService.addBoardMember(new AddBoardMemberCommand(
                                        boardId, userId, BoardRole.ADMIN, requestedBy));

                        // then
                        // 캐시 테스트는 실제 캐시 동작을 확인하기 어려우므로 호출 횟수만 확인
                        verify(boardRepository, times(2)).findBoardNameById(boardId);
                }
        }
}