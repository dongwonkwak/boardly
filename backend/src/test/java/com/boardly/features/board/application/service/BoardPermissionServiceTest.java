package com.boardly.features.board.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.model.BoardMemberId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardPermissionServiceTest {

    private BoardPermissionService boardPermissionService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private ValidationMessageResolver messageResolver;

    private BoardId testBoardId;
    private UserId testUserId;
    private UserId differentUserId;
    private Board testBoard;
    private BoardMember testBoardMember;

    @BeforeEach
    void setUp() {
        boardPermissionService = new BoardPermissionService(
                boardRepository,
                boardMemberRepository,
                messageResolver);

        testBoardId = new BoardId();
        testUserId = new UserId();
        differentUserId = new UserId();

        testBoard = createTestBoard(testBoardId, testUserId);
        testBoardMember = createTestBoardMember(testBoardId, testUserId, BoardRole.EDITOR);
    }

    // ==================== HELPER METHODS ====================

    private Board createTestBoard(BoardId boardId, UserId ownerId) {
        return Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .isArchived(false)
                .ownerId(ownerId)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private BoardMember createTestBoardMember(BoardId boardId, UserId userId, BoardRole role) {
        return BoardMember.builder()
                .memberId(new BoardMemberId())
                .boardId(boardId)
                .userId(userId)
                .role(role)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ==================== GET USER BOARD ROLE TESTS ====================

    @Nested
    @DisplayName("getUserBoardRole 메서드 테스트")
    class GetUserBoardRoleTests {

        @Test
        @DisplayName("보드가 존재하지 않는 경우 NotFound 오류를 반환해야 한다")
        void getUserBoardRole_BoardNotFound_ShouldReturnNotFoundFailure() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());
            when(messageResolver.getMessage("validation.board.not.found"))
                    .thenReturn("보드를 찾을 수 없습니다");

            // when
            Either<Failure, BoardRole> result = boardPermissionService.getUserBoardRole(testBoardId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");

            verify(boardRepository).findById(testBoardId);
            verify(messageResolver).getMessage("validation.board.not.found");
            verifyNoInteractions(boardMemberRepository);
        }

        @Test
        @DisplayName("보드 소유자인 경우 null을 반환해야 한다")
        void getUserBoardRole_BoardOwner_ShouldReturnNull() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, BoardRole> result = boardPermissionService.getUserBoardRole(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isNull();

            verify(boardRepository).findById(testBoardId);
            verifyNoInteractions(boardMemberRepository);
        }

        @Test
        @DisplayName("보드 멤버가 아닌 경우 PermissionDenied 오류를 반환해야 한다")
        void getUserBoardRole_NotBoardMember_ShouldReturnPermissionDeniedFailure() {
            // given
            Board board = createTestBoard(testBoardId, differentUserId);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(board));
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, testUserId))
                    .thenReturn(Optional.empty());
            when(messageResolver.getMessage("validation.board.access.denied"))
                    .thenReturn("보드 접근 권한이 없습니다");

            // when
            Either<Failure, BoardRole> result = boardPermissionService.getUserBoardRole(testBoardId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("보드 접근 권한이 없습니다");

            verify(boardRepository).findById(testBoardId);
            verify(boardMemberRepository).findByBoardIdAndUserId(testBoardId, testUserId);
            verify(messageResolver).getMessage("validation.board.access.denied");
        }

        @Test
        @DisplayName("비활성화된 보드 멤버인 경우 PermissionDenied 오류를 반환해야 한다")
        void getUserBoardRole_InactiveBoardMember_ShouldReturnPermissionDeniedFailure() {
            // given
            Board board = createTestBoard(testBoardId, differentUserId);
            BoardMember inactiveMember = createTestBoardMember(testBoardId, testUserId, BoardRole.EDITOR);
            inactiveMember.deactivate();

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(board));
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, testUserId))
                    .thenReturn(Optional.of(inactiveMember));
            when(messageResolver.getMessage("validation.board.member.inactive"))
                    .thenReturn("비활성화된 보드 멤버입니다");

            // when
            Either<Failure, BoardRole> result = boardPermissionService.getUserBoardRole(testBoardId, testUserId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(result.getLeft().getMessage()).isEqualTo("비활성화된 보드 멤버입니다");

            verify(boardRepository).findById(testBoardId);
            verify(boardMemberRepository).findByBoardIdAndUserId(testBoardId, testUserId);
            verify(messageResolver).getMessage("validation.board.member.inactive");
        }

        @Test
        @DisplayName("활성화된 보드 멤버인 경우 해당 역할을 반환해야 한다")
        void getUserBoardRole_ActiveBoardMember_ShouldReturnRole() {
            // given
            Board board = createTestBoard(testBoardId, differentUserId);
            BoardMember activeMember = createTestBoardMember(testBoardId, testUserId, BoardRole.ADMIN);

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(board));
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, testUserId))
                    .thenReturn(Optional.of(activeMember));

            // when
            Either<Failure, BoardRole> result = boardPermissionService.getUserBoardRole(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(BoardRole.ADMIN);

            verify(boardRepository).findById(testBoardId);
            verify(boardMemberRepository).findByBoardIdAndUserId(testBoardId, testUserId);
        }
    }

    // ==================== PERMISSION CHECK TESTS ====================

    @Nested
    @DisplayName("권한 확인 메서드 테스트")
    class PermissionCheckTests {

        @Test
        @DisplayName("canReadBoard - 보드 소유자는 읽기 권한이 있어야 한다")
        void canReadBoard_BoardOwner_ShouldReturnTrue() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canReadBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
        }

        @Test
        @DisplayName("canReadBoard - 보드 멤버는 읽기 권한이 있어야 한다")
        void canReadBoard_BoardMember_ShouldReturnTrue() {
            // given
            Board board = createTestBoard(testBoardId, differentUserId);
            BoardMember member = createTestBoardMember(testBoardId, testUserId, BoardRole.VIEWER);

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(board));
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, testUserId))
                    .thenReturn(Optional.of(member));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canReadBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
            verify(boardMemberRepository).findByBoardIdAndUserId(testBoardId, testUserId);
        }

        @Test
        @DisplayName("canWriteBoard - 보드 소유자는 쓰기 권한이 있어야 한다")
        void canWriteBoard_BoardOwner_ShouldReturnTrue() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canWriteBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
        }

        @Test
        @DisplayName("canWriteBoard - 편집자 역할 멤버는 쓰기 권한이 있어야 한다")
        void canWriteBoard_EditorMember_ShouldReturnTrue() {
            // given
            Board board = createTestBoard(testBoardId, differentUserId);
            BoardMember member = createTestBoardMember(testBoardId, testUserId, BoardRole.EDITOR);

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(board));
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, testUserId))
                    .thenReturn(Optional.of(member));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canWriteBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
            verify(boardMemberRepository).findByBoardIdAndUserId(testBoardId, testUserId);
        }

        @Test
        @DisplayName("canWriteBoard - 뷰어 역할 멤버는 쓰기 권한이 없어야 한다")
        void canWriteBoard_ViewerMember_ShouldReturnFalse() {
            // given
            Board board = createTestBoard(testBoardId, differentUserId);
            BoardMember member = createTestBoardMember(testBoardId, testUserId, BoardRole.VIEWER);

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(board));
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, testUserId))
                    .thenReturn(Optional.of(member));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canWriteBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isFalse();

            verify(boardRepository, times(2)).findById(testBoardId);
            verify(boardMemberRepository).findByBoardIdAndUserId(testBoardId, testUserId);
        }

        @Test
        @DisplayName("canAdminBoard - 보드 소유자는 관리 권한이 있어야 한다")
        void canAdminBoard_BoardOwner_ShouldReturnTrue() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canAdminBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
        }

        @Test
        @DisplayName("canAdminBoard - 관리자 역할 멤버는 관리 권한이 있어야 한다")
        void canAdminBoard_AdminMember_ShouldReturnTrue() {
            // given
            Board board = createTestBoard(testBoardId, differentUserId);
            BoardMember member = createTestBoardMember(testBoardId, testUserId, BoardRole.ADMIN);

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(board));
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, testUserId))
                    .thenReturn(Optional.of(member));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canAdminBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
            verify(boardMemberRepository).findByBoardIdAndUserId(testBoardId, testUserId);
        }

        @Test
        @DisplayName("canAdminBoard - 편집자 역할 멤버는 관리 권한이 없어야 한다")
        void canAdminBoard_EditorMember_ShouldReturnFalse() {
            // given
            Board board = createTestBoard(testBoardId, differentUserId);
            BoardMember member = createTestBoardMember(testBoardId, testUserId, BoardRole.EDITOR);

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(board));
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, testUserId))
                    .thenReturn(Optional.of(member));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canAdminBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isFalse();

            verify(boardRepository, times(2)).findById(testBoardId);
            verify(boardMemberRepository).findByBoardIdAndUserId(testBoardId, testUserId);
        }

        @Test
        @DisplayName("canManageBoardMembers - 보드 소유자는 멤버 관리 권한이 있어야 한다")
        void canManageBoardMembers_BoardOwner_ShouldReturnTrue() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canManageBoardMembers(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
        }

        @Test
        @DisplayName("canArchiveBoard - 보드 소유자는 아카이브 권한이 있어야 한다")
        void canArchiveBoard_BoardOwner_ShouldReturnTrue() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canArchiveBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
        }

        @Test
        @DisplayName("canToggleStarBoard - 보드 소유자는 즐겨찾기 변경 권한이 있어야 한다")
        void canToggleStarBoard_BoardOwner_ShouldReturnTrue() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canToggleStarBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
        }

        @Test
        @DisplayName("canDeleteBoard - 보드 소유자는 삭제 권한이 있어야 한다")
        void canDeleteBoard_BoardOwner_ShouldReturnTrue() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canDeleteBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
        }

        @Test
        @DisplayName("권한 확인 메서드들이 보드가 존재하지 않는 경우 false를 반환해야 한다")
        void permissionCheckMethods_BoardNotFound_ShouldReturnFalse() {
            // given
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());
            when(messageResolver.getMessage("validation.board.not.found"))
                    .thenReturn("보드를 찾을 수 없습니다");

            // when & then
            Either<Failure, Boolean> readResult = boardPermissionService.canReadBoard(testBoardId, testUserId);
            assertThat(readResult.isLeft()).isTrue();
            assertThat(readResult.getLeft()).isInstanceOf(Failure.NotFound.class);

            Either<Failure, Boolean> writeResult = boardPermissionService.canWriteBoard(testBoardId, testUserId);
            assertThat(writeResult.isLeft()).isTrue();
            assertThat(writeResult.getLeft()).isInstanceOf(Failure.NotFound.class);

            Either<Failure, Boolean> adminResult = boardPermissionService.canAdminBoard(testBoardId, testUserId);
            assertThat(adminResult.isLeft()).isTrue();
            assertThat(adminResult.getLeft()).isInstanceOf(Failure.NotFound.class);

            Either<Failure, Boolean> manageMembersResult = boardPermissionService.canManageBoardMembers(testBoardId,
                    testUserId);
            assertThat(manageMembersResult.isLeft()).isTrue();
            assertThat(manageMembersResult.getLeft()).isInstanceOf(Failure.NotFound.class);

            Either<Failure, Boolean> archiveResult = boardPermissionService.canArchiveBoard(testBoardId, testUserId);
            assertThat(archiveResult.isLeft()).isTrue();
            assertThat(archiveResult.getLeft()).isInstanceOf(Failure.NotFound.class);

            Either<Failure, Boolean> toggleStarResult = boardPermissionService.canToggleStarBoard(testBoardId,
                    testUserId);
            assertThat(toggleStarResult.isLeft()).isTrue();
            assertThat(toggleStarResult.getLeft()).isInstanceOf(Failure.NotFound.class);

            Either<Failure, Boolean> deleteResult = boardPermissionService.canDeleteBoard(testBoardId, testUserId);
            assertThat(deleteResult.isLeft()).isTrue();
            assertThat(deleteResult.getLeft()).isInstanceOf(Failure.NotFound.class);

            verify(boardRepository, times(7)).findById(testBoardId);
            verify(messageResolver, times(7)).getMessage("validation.board.not.found");
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("보드가 아카이브된 상태에서도 권한 확인이 정상적으로 동작해야 한다")
        void permissionCheck_ArchivedBoard_ShouldWorkNormally() {
            // given
            Board archivedBoard = createTestBoard(testBoardId, testUserId);
            archivedBoard.archive();

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(archivedBoard));

            // when
            Either<Failure, Boolean> result = boardPermissionService.canReadBoard(testBoardId, testUserId);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isTrue();

            verify(boardRepository, times(2)).findById(testBoardId);
        }

        @Test
        @DisplayName("다양한 BoardRole에 대한 권한 확인이 정상적으로 동작해야 한다")
        void permissionCheck_VariousBoardRoles_ShouldWorkCorrectly() {
            // given
            Board board = createTestBoard(testBoardId, differentUserId);
            UserId editorUserId = new UserId();
            UserId viewerUserId = new UserId();

            BoardMember editorMember = createTestBoardMember(testBoardId, editorUserId, BoardRole.EDITOR);
            BoardMember viewerMember = createTestBoardMember(testBoardId, viewerUserId, BoardRole.VIEWER);

            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(board));

            // when & then - EDITOR
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, editorUserId))
                    .thenReturn(Optional.of(editorMember));
            Either<Failure, Boolean> editorResult = boardPermissionService.canWriteBoard(testBoardId, editorUserId);
            assertThat(editorResult.isRight()).isTrue();
            assertThat(editorResult.get()).isTrue();

            // when & then - VIEWER
            when(boardMemberRepository.findByBoardIdAndUserId(testBoardId, viewerUserId))
                    .thenReturn(Optional.of(viewerMember));
            Either<Failure, Boolean> viewerResult = boardPermissionService.canWriteBoard(testBoardId, viewerUserId);
            assertThat(viewerResult.isRight()).isTrue();
            assertThat(viewerResult.get()).isFalse();

            verify(boardRepository, atLeastOnce()).findById(testBoardId);
            verify(boardMemberRepository, atLeastOnce()).findByBoardIdAndUserId(any(), any());
        }
    }
}