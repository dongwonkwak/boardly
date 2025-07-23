package com.boardly.features.board.application.service;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.application.dto.BoardNameDto;
import com.boardly.features.board.application.port.input.*;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.model.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BoardMemberService 테스트")
class BoardMemberServiceTest {

        private BoardMemberService boardMemberService;

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

        @BeforeEach
        void setUp() {
                boardMemberService = new BoardMemberService(
                                boardRepository,
                                boardMemberRepository,
                                boardPermissionService,
                                messageResolver,
                                userFinder,
                                userRepository,
                                boardValidator,
                                activityHelper);

                // 공통 메시지 모킹 설정
                lenient().when(messageResolver.getMessage("validation.user.not.found"))
                                .thenReturn("사용자를 찾을 수 없습니다");
                lenient().when(messageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");
                lenient().when(messageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다");
                lenient().when(messageResolver.getMessage("validation.board.archived.modification.denied"))
                                .thenReturn("아카이브된 보드는 수정할 수 없습니다");
                lenient().when(messageResolver.getMessage("validation.board.member.management.denied"))
                                .thenReturn("보드 멤버 관리 권한이 없습니다");
                lenient().when(messageResolver.getMessage("validation.board.member.already.exists"))
                                .thenReturn("이미 보드 멤버로 등록된 사용자입니다");
                lenient().when(messageResolver.getMessage("validation.board.member.not.found"))
                                .thenReturn("보드 멤버를 찾을 수 없습니다");
                lenient().when(messageResolver.getMessage("validation.board.owner.removal.denied"))
                                .thenReturn("보드 소유자는 제거할 수 없습니다");
                lenient().when(messageResolver.getMessage("validation.board.owner.role.change.denied"))
                                .thenReturn("보드 소유자의 역할은 변경할 수 없습니다");
        }

        // ==================== HELPER METHODS ====================

        private BoardId createBoardId() {
                return new BoardId("board-123");
        }

        private UserId createUserId() {
                return new UserId("user-456");
        }

        private UserId createAnotherUserId() {
                return new UserId("user-789");
        }

        private Board createValidBoard(BoardId boardId, UserId ownerId, boolean isArchived) {
                return Board.builder()
                                .boardId(boardId)
                                .title("테스트 보드")
                                .description("테스트 보드 설명")
                                .isArchived(isArchived)
                                .ownerId(ownerId)
                                .isStarred(false)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        private BoardMember createValidBoardMember(BoardId boardId, UserId userId, BoardRole role) {
                return BoardMember.builder()
                                .memberId(new BoardMemberId("member-123"))
                                .boardId(boardId)
                                .userId(userId)
                                .role(role)
                                .isActive(true)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        private UserNameDto createUserNameDto() {
                return new UserNameDto("홍", "길동");
        }

        private BoardNameDto createBoardNameDto() {
                return new BoardNameDto("테스트 보드");
        }

        private AddBoardMemberCommand createValidAddCommand() {
                return new AddBoardMemberCommand(
                                createBoardId(),
                                createUserId(),
                                BoardRole.EDITOR,
                                createAnotherUserId());
        }

        private RemoveBoardMemberCommand createValidRemoveCommand() {
                return new RemoveBoardMemberCommand(
                                createBoardId(),
                                createUserId(),
                                createAnotherUserId());
        }

        private UpdateBoardMemberRoleCommand createValidUpdateRoleCommand() {
                return new UpdateBoardMemberRoleCommand(
                                createBoardId(),
                                createUserId(),
                                BoardRole.ADMIN,
                                createAnotherUserId());
        }

        private ValidationResult<AddBoardMemberCommand> createInvalidAddValidationResult() {
                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("boardId")
                                .message("보드 ID는 필수입니다")
                                .rejectedValue(null)
                                .build();
                return ValidationResult.invalid(violation);
        }

        private ValidationResult<RemoveBoardMemberCommand> createInvalidRemoveValidationResult() {
                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("boardId")
                                .message("보드 ID는 필수입니다")
                                .rejectedValue(null)
                                .build();
                return ValidationResult.invalid(violation);
        }

        private ValidationResult<UpdateBoardMemberRoleCommand> createInvalidUpdateRoleValidationResult() {
                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("boardId")
                                .message("보드 ID는 필수입니다")
                                .rejectedValue(null)
                                .build();
                return ValidationResult.invalid(violation);
        }

        // ==================== ADD BOARD MEMBER TESTS ====================

        @Nested
        @DisplayName("addBoardMember 메서드 테스트")
        class AddBoardMemberTest {

                @Test
                @DisplayName("유효한 정보로 보드 멤버 추가가 성공해야 한다")
                void addBoardMember_withValidData_shouldReturnBoardMember() {
                        // given
                        AddBoardMemberCommand command = createValidAddCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);
                        BoardMember boardMember = createValidBoardMember(command.boardId(), command.userId(),
                                        command.role());
                        UserNameDto userName = createUserNameDto();
                        BoardNameDto boardName = createBoardNameDto();

                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.existsByBoardIdAndUserId(command.boardId(), command.userId()))
                                        .thenReturn(false);
                        when(boardMemberRepository.save(any(BoardMember.class)))
                                        .thenReturn(Either.right(boardMember));
                        when(userRepository.findUserNameById(command.userId()))
                                        .thenReturn(Optional.of(userName));
                        when(boardRepository.findBoardNameById(command.boardId()))
                                        .thenReturn(Optional.of(boardName));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        BoardMember savedMember = result.get();
                        assertThat(savedMember.getBoardId()).isEqualTo(command.boardId());
                        assertThat(savedMember.getUserId()).isEqualTo(command.userId());
                        assertThat(savedMember.getRole()).isEqualTo(command.role());

                        verify(boardValidator).validateAddMember(command);
                        verify(userFinder).checkUserExists(command.requestedBy());
                        verify(boardRepository).findById(command.boardId());
                        verify(boardPermissionService).canManageBoardMembers(command.boardId(), command.requestedBy());
                        verify(boardMemberRepository).existsByBoardIdAndUserId(command.boardId(), command.userId());
                        verify(boardMemberRepository).save(any(BoardMember.class));
                        verify(activityHelper).logBoardActivity(
                                        eq(ActivityType.BOARD_ADD_MEMBER),
                                        eq(command.requestedBy()),
                                        any(),
                                        eq(command.boardId()));
                }

                @Test
                @DisplayName("존재하지 않는 사용자로 멤버 추가 시도 시 NotFound 오류를 반환해야 한다")
                void addBoardMember_withNonExistentUser_shouldReturnNotFoundFailure() {
                        // given
                        AddBoardMemberCommand command = createValidAddCommand();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(false);

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
                        verify(userFinder).checkUserExists(command.requestedBy());
                        verifyNoInteractions(boardValidator, boardRepository, boardPermissionService,
                                        boardMemberRepository,
                                        activityHelper);
                }

                @Test
                @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
                void addBoardMember_withInvalidData_shouldReturnInputError() {
                        // given
                        AddBoardMemberCommand command = createValidAddCommand();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(createInvalidAddValidationResult());

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) result.getLeft();
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다");

                        verify(boardValidator).validateAddMember(command);
                        verifyNoInteractions(boardRepository, boardPermissionService, boardMemberRepository,
                                        activityHelper);
                }

                @Test
                @DisplayName("존재하지 않는 보드에 멤버 추가 시도 시 NotFound 오류를 반환해야 한다")
                void addBoardMember_withNonExistentBoard_shouldReturnNotFoundFailure() {
                        // given
                        AddBoardMemberCommand command = createValidAddCommand();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");
                        verify(boardRepository).findById(command.boardId());
                        verifyNoInteractions(boardPermissionService, boardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("아카이브된 보드에 멤버 추가 시도 시 Conflict 오류를 반환해야 한다")
                void addBoardMember_withArchivedBoard_shouldReturnConflictFailure() {
                        // given
                        AddBoardMemberCommand command = createValidAddCommand();
                        Board archivedBoard = createValidBoard(command.boardId(), command.requestedBy(), true);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("아카이브된 보드는 수정할 수 없습니다");
                        verify(boardRepository).findById(command.boardId());
                        verifyNoInteractions(boardPermissionService, boardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("멤버 관리 권한이 없는 경우 PermissionDenied 오류를 반환해야 한다")
                void addBoardMember_withoutManagementPermission_shouldReturnPermissionDeniedFailure() {
                        // given
                        AddBoardMemberCommand command = createValidAddCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(false));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 멤버 관리 권한이 없습니다");
                        verify(boardPermissionService).canManageBoardMembers(command.boardId(), command.requestedBy());
                        verifyNoInteractions(boardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("이미 보드 멤버로 등록된 사용자 추가 시도 시 Conflict 오류를 반환해야 한다")
                void addBoardMember_withExistingMember_shouldReturnConflictFailure() {
                        // given
                        AddBoardMemberCommand command = createValidAddCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.existsByBoardIdAndUserId(command.boardId(), command.userId()))
                                        .thenReturn(true);

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("이미 보드 멤버로 등록된 사용자입니다");
                        verify(boardMemberRepository).existsByBoardIdAndUserId(command.boardId(), command.userId());
                        verifyNoMoreInteractions(boardMemberRepository);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("보드 멤버 생성 중 예외 발생 시 InternalError를 반환해야 한다")
                void addBoardMember_withCreationException_shouldReturnInternalError() {
                        // given
                        AddBoardMemberCommand command = createValidAddCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateAddMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.existsByBoardIdAndUserId(command.boardId(), command.userId()))
                                        .thenReturn(false);
                        when(boardMemberRepository.save(any(BoardMember.class)))
                                        .thenReturn(Either.left(Failure.ofInternalError("데이터베이스 오류",
                                                        "BOARD_MEMBER_CREATION_ERROR", null)));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.addBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                        Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                        assertThat(internalError.getErrorCode()).isEqualTo("BOARD_MEMBER_CREATION_ERROR");
                        verify(boardMemberRepository).save(any(BoardMember.class));
                        verifyNoInteractions(activityHelper);
                }
        }

        // ==================== REMOVE BOARD MEMBER TESTS ====================

        @Nested
        @DisplayName("removeBoardMember 메서드 테스트")
        class RemoveBoardMemberTest {

                @Test
                @DisplayName("유효한 정보로 보드 멤버 제거가 성공해야 한다")
                void removeBoardMember_withValidData_shouldReturnSuccess() {
                        // given
                        RemoveBoardMemberCommand command = createValidRemoveCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);
                        BoardMember boardMember = createValidBoardMember(command.boardId(), command.targetUserId(),
                                        BoardRole.EDITOR);
                        UserNameDto userName = createUserNameDto();
                        BoardNameDto boardName = createBoardNameDto();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(command.boardId(), command.targetUserId()))
                                        .thenReturn(Optional.of(boardMember));
                        when(boardMemberRepository.delete(boardMember.getMemberId()))
                                        .thenReturn(Either.right((Void) null));
                        when(userRepository.findUserNameById(command.targetUserId()))
                                        .thenReturn(Optional.of(userName));
                        when(boardRepository.findBoardNameById(command.boardId()))
                                        .thenReturn(Optional.of(boardName));

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isNull();

                        verify(boardValidator).validateRemoveMember(command);
                        verify(boardRepository).findById(command.boardId());
                        verify(boardPermissionService).canManageBoardMembers(command.boardId(), command.requestedBy());
                        verify(boardMemberRepository).findByBoardIdAndUserId(command.boardId(), command.targetUserId());
                        verify(boardMemberRepository).delete(boardMember.getMemberId());
                        verify(activityHelper).logBoardActivity(
                                        eq(ActivityType.BOARD_REMOVE_MEMBER),
                                        eq(command.requestedBy()),
                                        any(),
                                        eq(command.boardId()));
                }

                @Test
                @DisplayName("존재하지 않는 사용자로 멤버 제거 시도 시 NotFound 오류를 반환해야 한다")
                void removeBoardMember_withNonExistentUser_shouldReturnNotFoundFailure() {
                        // given
                        RemoveBoardMemberCommand command = createValidRemoveCommand();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(false);

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
                        verify(userFinder).checkUserExists(command.requestedBy());
                        verifyNoInteractions(boardValidator, boardRepository, boardPermissionService,
                                        boardMemberRepository,
                                        activityHelper);
                }

                @Test
                @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
                void removeBoardMember_withInvalidData_shouldReturnInputError() {
                        // given
                        RemoveBoardMemberCommand command = createValidRemoveCommand();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(createInvalidRemoveValidationResult());

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) result.getLeft();
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다");

                        verify(boardValidator).validateRemoveMember(command);
                        verifyNoInteractions(boardRepository, boardPermissionService, boardMemberRepository,
                                        activityHelper);
                }

                @Test
                @DisplayName("존재하지 않는 보드에서 멤버 제거 시도 시 NotFound 오류를 반환해야 한다")
                void removeBoardMember_withNonExistentBoard_shouldReturnNotFoundFailure() {
                        // given
                        RemoveBoardMemberCommand command = createValidRemoveCommand();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");
                        verify(boardRepository).findById(command.boardId());
                        verifyNoInteractions(boardPermissionService, boardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("아카이브된 보드에서 멤버 제거 시도 시 Conflict 오류를 반환해야 한다")
                void removeBoardMember_withArchivedBoard_shouldReturnConflictFailure() {
                        // given
                        RemoveBoardMemberCommand command = createValidRemoveCommand();
                        Board archivedBoard = createValidBoard(command.boardId(), command.requestedBy(), true);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("아카이브된 보드는 수정할 수 없습니다");
                        verify(boardRepository).findById(command.boardId());
                        verifyNoInteractions(boardPermissionService, boardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("멤버 관리 권한이 없는 경우 PermissionDenied 오류를 반환해야 한다")
                void removeBoardMember_withoutManagementPermission_shouldReturnPermissionDeniedFailure() {
                        // given
                        RemoveBoardMemberCommand command = createValidRemoveCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(false));

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 멤버 관리 권한이 없습니다");
                        verify(boardPermissionService).canManageBoardMembers(command.boardId(), command.requestedBy());
                        verifyNoInteractions(boardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("존재하지 않는 보드 멤버 제거 시도 시 NotFound 오류를 반환해야 한다")
                void removeBoardMember_withNonExistentMember_shouldReturnNotFoundFailure() {
                        // given
                        RemoveBoardMemberCommand command = createValidRemoveCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(command.boardId(), command.targetUserId()))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 멤버를 찾을 수 없습니다");
                        verify(boardMemberRepository).findByBoardIdAndUserId(command.boardId(), command.targetUserId());
                        verifyNoMoreInteractions(boardMemberRepository);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("보드 소유자 제거 시도 시 Conflict 오류를 반환해야 한다")
                void removeBoardMember_withBoardOwner_shouldReturnConflictFailure() {
                        // given
                        RemoveBoardMemberCommand command = createValidRemoveCommand();
                        Board board = createValidBoard(command.boardId(), command.targetUserId(), false); // 소유자가 제거 대상
                        BoardMember boardMember = createValidBoardMember(command.boardId(), command.targetUserId(),
                                        BoardRole.OWNER);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateRemoveMember(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(command.boardId(), command.targetUserId()))
                                        .thenReturn(Optional.of(boardMember));

                        // when
                        Either<Failure, Void> result = boardMemberService.removeBoardMember(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 소유자는 제거할 수 없습니다");
                        verify(boardMemberRepository).findByBoardIdAndUserId(command.boardId(), command.targetUserId());
                        verifyNoMoreInteractions(boardMemberRepository);
                        verifyNoInteractions(activityHelper);
                }
        }

        // ==================== UPDATE BOARD MEMBER ROLE TESTS ====================

        @Nested
        @DisplayName("updateBoardMemberRole 메서드 테스트")
        class UpdateBoardMemberRoleTest {

                @Test
                @DisplayName("유효한 정보로 보드 멤버 역할 변경이 성공해야 한다")
                void updateBoardMemberRole_withValidData_shouldReturnUpdatedBoardMember() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);
                        BoardMember boardMember = createValidBoardMember(command.boardId(), command.targetUserId(),
                                        BoardRole.EDITOR);
                        BoardMember updatedMember = createValidBoardMember(command.boardId(), command.targetUserId(),
                                        command.newRole());
                        UserNameDto userName = createUserNameDto();
                        BoardNameDto boardName = createBoardNameDto();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(command.boardId(), command.targetUserId()))
                                        .thenReturn(Optional.of(boardMember));
                        when(boardMemberRepository.save(any(BoardMember.class)))
                                        .thenReturn(Either.right(updatedMember));
                        when(userRepository.findUserNameById(command.targetUserId()))
                                        .thenReturn(Optional.of(userName));
                        when(boardRepository.findBoardNameById(command.boardId()))
                                        .thenReturn(Optional.of(boardName));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        BoardMember savedMember = result.get();
                        assertThat(savedMember.getBoardId()).isEqualTo(command.boardId());
                        assertThat(savedMember.getUserId()).isEqualTo(command.targetUserId());
                        assertThat(savedMember.getRole()).isEqualTo(command.newRole());

                        verify(boardValidator).validateUpdateMemberRole(command);
                        verify(boardRepository).findById(command.boardId());
                        verify(boardPermissionService).canManageBoardMembers(command.boardId(), command.requestedBy());
                        verify(boardMemberRepository).findByBoardIdAndUserId(command.boardId(), command.targetUserId());
                        verify(boardMemberRepository).save(any(BoardMember.class));
                        verify(activityHelper).logBoardActivity(
                                        eq(ActivityType.BOARD_UPDATE_MEMBER_ROLE),
                                        eq(command.requestedBy()),
                                        any(),
                                        eq(command.boardId()));
                }

                @Test
                @DisplayName("존재하지 않는 사용자로 역할 변경 시도 시 NotFound 오류를 반환해야 한다")
                void updateBoardMemberRole_withNonExistentUser_shouldReturnNotFoundFailure() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(false);

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
                        verify(userFinder).checkUserExists(command.requestedBy());
                        verifyNoInteractions(boardValidator, boardRepository, boardPermissionService,
                                        boardMemberRepository,
                                        activityHelper);
                }

                @Test
                @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
                void updateBoardMemberRole_withInvalidData_shouldReturnInputError() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(createInvalidUpdateRoleValidationResult());

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) result.getLeft();
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다");

                        verify(boardValidator).validateUpdateMemberRole(command);
                        verifyNoInteractions(boardRepository, boardPermissionService, boardMemberRepository,
                                        activityHelper);
                }

                @Test
                @DisplayName("존재하지 않는 보드에서 역할 변경 시도 시 NotFound 오류를 반환해야 한다")
                void updateBoardMemberRole_withNonExistentBoard_shouldReturnNotFoundFailure() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");
                        verify(boardRepository).findById(command.boardId());
                        verifyNoInteractions(boardPermissionService, boardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("아카이브된 보드에서 역할 변경 시도 시 Conflict 오류를 반환해야 한다")
                void updateBoardMemberRole_withArchivedBoard_shouldReturnConflictFailure() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();
                        Board archivedBoard = createValidBoard(command.boardId(), command.requestedBy(), true);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("아카이브된 보드는 수정할 수 없습니다");
                        verify(boardRepository).findById(command.boardId());
                        verifyNoInteractions(boardPermissionService, boardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("멤버 관리 권한이 없는 경우 PermissionDenied 오류를 반환해야 한다")
                void updateBoardMemberRole_withoutManagementPermission_shouldReturnPermissionDeniedFailure() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(false));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 멤버 관리 권한이 없습니다");
                        verify(boardPermissionService).canManageBoardMembers(command.boardId(), command.requestedBy());
                        verifyNoInteractions(boardMemberRepository, activityHelper);
                }

                @Test
                @DisplayName("존재하지 않는 보드 멤버 역할 변경 시도 시 NotFound 오류를 반환해야 한다")
                void updateBoardMemberRole_withNonExistentMember_shouldReturnNotFoundFailure() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(command.boardId(), command.targetUserId()))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 멤버를 찾을 수 없습니다");
                        verify(boardMemberRepository).findByBoardIdAndUserId(command.boardId(), command.targetUserId());
                        verifyNoMoreInteractions(boardMemberRepository);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("보드 소유자 역할 변경 시도 시 Conflict 오류를 반환해야 한다")
                void updateBoardMemberRole_withBoardOwner_shouldReturnConflictFailure() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();
                        Board board = createValidBoard(command.boardId(), command.targetUserId(), false); // 소유자가 변경 대상
                        BoardMember boardMember = createValidBoardMember(command.boardId(), command.targetUserId(),
                                        BoardRole.OWNER);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(command.boardId(), command.targetUserId()))
                                        .thenReturn(Optional.of(boardMember));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 소유자의 역할은 변경할 수 없습니다");
                        verify(boardMemberRepository).findByBoardIdAndUserId(command.boardId(), command.targetUserId());
                        verifyNoMoreInteractions(boardMemberRepository);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("같은 역할로 변경하려는 경우 기존 멤버를 반환해야 한다")
                void updateBoardMemberRole_withSameRole_shouldReturnExistingMember() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);
                        BoardMember boardMember = createValidBoardMember(command.boardId(), command.targetUserId(),
                                        command.newRole());
                        UserNameDto userName = createUserNameDto();
                        BoardNameDto boardName = createBoardNameDto();

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(command.boardId(), command.targetUserId()))
                                        .thenReturn(Optional.of(boardMember));
                        when(userRepository.findUserNameById(command.targetUserId()))
                                        .thenReturn(Optional.of(userName));
                        when(boardRepository.findBoardNameById(command.boardId()))
                                        .thenReturn(Optional.of(boardName));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        BoardMember savedMember = result.get();
                        assertThat(savedMember.getBoardId()).isEqualTo(command.boardId());
                        assertThat(savedMember.getUserId()).isEqualTo(command.targetUserId());
                        assertThat(savedMember.getRole()).isEqualTo(command.newRole());

                        verify(boardValidator).validateUpdateMemberRole(command);
                        verify(boardRepository).findById(command.boardId());
                        verify(boardPermissionService).canManageBoardMembers(command.boardId(), command.requestedBy());
                        verify(boardMemberRepository).findByBoardIdAndUserId(command.boardId(), command.targetUserId());
                        verify(boardMemberRepository, never()).save(any(BoardMember.class));
                        verify(activityHelper).logBoardActivity(
                                        eq(ActivityType.BOARD_UPDATE_MEMBER_ROLE),
                                        eq(command.requestedBy()),
                                        any(),
                                        eq(command.boardId()));
                }

                @Test
                @DisplayName("보드 멤버 역할 변경 중 예외 발생 시 InternalError를 반환해야 한다")
                void updateBoardMemberRole_withUpdateException_shouldReturnInternalError() {
                        // given
                        UpdateBoardMemberRoleCommand command = createValidUpdateRoleCommand();
                        Board board = createValidBoard(command.boardId(), command.requestedBy(), false);
                        BoardMember boardMember = createValidBoardMember(command.boardId(), command.targetUserId(),
                                        BoardRole.EDITOR);

                        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                        when(boardValidator.validateUpdateMemberRole(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(command.boardId()))
                                        .thenReturn(Optional.of(board));
                        when(boardPermissionService.canManageBoardMembers(command.boardId(), command.requestedBy()))
                                        .thenReturn(Either.right(true));
                        when(boardMemberRepository.findByBoardIdAndUserId(command.boardId(), command.targetUserId()))
                                        .thenReturn(Optional.of(boardMember));
                        when(boardMemberRepository.save(any(BoardMember.class)))
                                        .thenThrow(new RuntimeException("데이터베이스 오류"));

                        // when
                        Either<Failure, BoardMember> result = boardMemberService.updateBoardMemberRole(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                        Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                        assertThat(internalError.getErrorCode()).isEqualTo("BOARD_MEMBER_ROLE_UPDATE_ERROR");
                        verify(boardMemberRepository).save(any(BoardMember.class));
                        verifyNoInteractions(activityHelper);
                }
        }
}