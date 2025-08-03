package com.boardly.features.board.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
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
import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.application.port.input.DeleteBoardCommand;
import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardManagementService 테스트")
class BoardManagementServiceTest {

    @Mock
    private BoardValidator boardValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @Mock
    private ActivityHelper activityHelper;

    @InjectMocks
    private BoardManagementService boardManagementService;

    private BoardId testBoardId;
    private UserId testUserId;
    private Board testBoard;
    private CreateBoardCommand createBoardCommand;
    private UpdateBoardCommand updateBoardCommand;
    private DeleteBoardCommand deleteBoardCommand;
    private ArchiveBoardCommand archiveBoardCommand;

    @BeforeEach
    void setUp() {
        testBoardId = new BoardId("test-board-123");
        testUserId = new UserId("test-user-123");

        Instant now = Instant.now();
        testBoard = Board.builder()
                .boardId(testBoardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .isArchived(false)
                .ownerId(testUserId)
                .isStarred(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        createBoardCommand = CreateBoardCommand.of("테스트 보드", "테스트 보드 설명", testUserId);
        updateBoardCommand = UpdateBoardCommand.of(testBoardId, "수정된 보드", "수정된 설명", testUserId);
        deleteBoardCommand = DeleteBoardCommand.of(testBoardId, testUserId);
        archiveBoardCommand = ArchiveBoardCommand.of(testBoardId, testUserId);
    }

    private Board createArchivedBoard() {
        Instant now = Instant.now();
        return Board.builder()
                .boardId(testBoardId)
                .title("아카이브된 테스트 보드")
                .description("아카이브된 테스트 보드 설명")
                .isArchived(true)
                .ownerId(testUserId)
                .isStarred(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== CREATE BOARD TESTS ====================

    @Nested
    @DisplayName("createBoard 메서드 테스트")
    class CreateBoardTests {

        @Test
        @DisplayName("유효한 요청으로 보드 생성 시 성공해야 한다")
        void createBoard_withValidRequest_shouldSucceed() {
            // given
            ValidationResult<CreateBoardCommand> validResult = ValidationResult.valid(createBoardCommand);
            when(boardValidator.validateCreate(createBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.save(any(Board.class))).thenReturn(Either.right(testBoard));

            // when
            Either<Failure, Board> result = boardManagementService.createBoard(createBoardCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testBoard);
            verify(boardValidator).validateCreate(createBoardCommand);
            verify(userFinder).checkUserExists(testUserId);
            verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("검증 실패 시 실패를 반환해야 한다")
        void createBoard_withValidationFailure_shouldReturnFailure() {
            // given
            ValidationResult<CreateBoardCommand> invalidResult = ValidationResult.invalid("title", "제목은 필수입니다", null);
            when(boardValidator.validateCreate(createBoardCommand)).thenReturn(invalidResult);
            when(validationMessageResolver.getMessage("validation.input.invalid")).thenReturn("입력이 유효하지 않습니다");

            // when
            Either<Failure, Board> result = boardManagementService.createBoard(createBoardCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다");
            verify(boardValidator).validateCreate(createBoardCommand);
            verify(userFinder, never()).checkUserExists(any());
            verify(boardRepository, never()).save(any());
        }
    }

    // ==================== UPDATE BOARD TESTS ====================

    @Nested
    @DisplayName("updateBoard 메서드 테스트")
    class UpdateBoardTests {

        @Test
        @DisplayName("유효한 요청으로 보드 업데이트 시 성공해야 한다")
        void updateBoard_withValidRequest_shouldSucceed() {
            // given
            ValidationResult<UpdateBoardCommand> validResult = ValidationResult.valid(updateBoardCommand);
            when(boardValidator.validateUpdate(updateBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canWriteBoard(testBoardId, testUserId)).thenReturn(Either.right(true));
            when(boardRepository.save(any(Board.class))).thenReturn(Either.right(testBoard));

            // when
            Either<Failure, Board> result = boardManagementService.updateBoard(updateBoardCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testBoard);
            verify(boardValidator).validateUpdate(updateBoardCommand);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canWriteBoard(testBoardId, testUserId);
            verify(boardRepository).save(any(Board.class));
        }
    }

    // ==================== DELETE BOARD TESTS ====================

    @Nested
    @DisplayName("deleteBoard 메서드 테스트")
    class DeleteBoardTests {

        @Test
        @DisplayName("유효한 요청으로 보드 삭제 시 성공해야 한다")
        void deleteBoard_withValidRequest_shouldSucceed() {
            // given
            ValidationResult<DeleteBoardCommand> validResult = ValidationResult.valid(deleteBoardCommand);
            when(boardValidator.validateDelete(deleteBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canDeleteBoard(testBoardId, testUserId)).thenReturn(Either.right(true));
            when(cardRepository.deleteByBoardId(testBoardId)).thenReturn(Either.right(null));
            doNothing().when(boardListRepository).deleteByBoardId(testBoardId);
            when(boardMemberRepository.deleteByBoardId(testBoardId)).thenReturn(Either.right(null));
            when(labelRepository.deleteByBoardId(testBoardId)).thenReturn(Either.right(null));
            when(boardRepository.delete(testBoardId)).thenReturn(Either.right(null));

            // when
            Either<Failure, Void> result = boardManagementService.deleteBoard(deleteBoardCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(boardValidator).validateDelete(deleteBoardCommand);
            verify(userFinder).checkUserExists(testUserId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canDeleteBoard(testBoardId, testUserId);
            verify(cardRepository).deleteByBoardId(testBoardId);
            verify(boardListRepository).deleteByBoardId(testBoardId);
            verify(boardMemberRepository).deleteByBoardId(testBoardId);
            verify(labelRepository).deleteByBoardId(testBoardId);
            verify(boardRepository).delete(testBoardId);
        }

        @Test
        @DisplayName("검증 실패 시 실패를 반환해야 한다")
        void deleteBoard_withValidationFailure_shouldReturnFailure() {
            // given
            ValidationResult<DeleteBoardCommand> invalidResult = ValidationResult.invalid("boardId", "보드 ID는 필수입니다",
                    null);
            when(boardValidator.validateDelete(deleteBoardCommand)).thenReturn(invalidResult);
            when(validationMessageResolver.getMessage("validation.input.invalid")).thenReturn("입력이 유효하지 않습니다");

            // when
            Either<Failure, Void> result = boardManagementService.deleteBoard(deleteBoardCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다");
            verify(boardValidator).validateDelete(deleteBoardCommand);
            verify(userFinder, never()).checkUserExists(any());
            verify(boardRepository, never()).findById(any());
        }

        @Test
        @DisplayName("사용자가 존재하지 않을 때 실패를 반환해야 한다")
        void deleteBoard_withNonExistentUser_shouldReturnFailure() {
            // given
            ValidationResult<DeleteBoardCommand> validResult = ValidationResult.valid(deleteBoardCommand);
            when(boardValidator.validateDelete(deleteBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(false);
            when(validationMessageResolver.getMessage("validation.user.not.found")).thenReturn("사용자를 찾을 수 없습니다");

            // when
            Either<Failure, Void> result = boardManagementService.deleteBoard(deleteBoardCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
            verify(boardValidator).validateDelete(deleteBoardCommand);
            verify(userFinder).checkUserExists(testUserId);
            verify(boardRepository, never()).findById(any());
        }

        @Test
        @DisplayName("보드가 존재하지 않을 때 실패를 반환해야 한다")
        void deleteBoard_withNonExistentBoard_shouldReturnFailure() {
            // given
            ValidationResult<DeleteBoardCommand> validResult = ValidationResult.valid(deleteBoardCommand);
            when(boardValidator.validateDelete(deleteBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("validation.board.not.found")).thenReturn("보드를 찾을 수 없습니다");

            // when
            Either<Failure, Void> result = boardManagementService.deleteBoard(deleteBoardCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");
            verify(boardValidator).validateDelete(deleteBoardCommand);
            verify(userFinder).checkUserExists(testUserId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService, never()).canDeleteBoard(any(), any());
        }

        @Test
        @DisplayName("삭제 권한이 없을 때 실패를 반환해야 한다")
        void deleteBoard_withoutDeletePermission_shouldReturnFailure() {
            // given
            ValidationResult<DeleteBoardCommand> validResult = ValidationResult.valid(deleteBoardCommand);
            when(boardValidator.validateDelete(deleteBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canDeleteBoard(testBoardId, testUserId)).thenReturn(Either.right(false));
            when(validationMessageResolver.getMessage("validation.board.delete.access.denied"))
                    .thenReturn("삭제 권한이 없습니다");

            // when
            Either<Failure, Void> result = boardManagementService.deleteBoard(deleteBoardCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).isEqualTo("삭제 권한이 없습니다");
            verify(boardValidator).validateDelete(deleteBoardCommand);
            verify(userFinder).checkUserExists(testUserId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canDeleteBoard(testBoardId, testUserId);
            verify(cardRepository, never()).deleteByBoardId(any());
        }

        @Test
        @DisplayName("카드 삭제 실패 시 실패를 반환해야 한다")
        void deleteBoard_withCardDeleteFailure_shouldReturnFailure() {
            // given
            ValidationResult<DeleteBoardCommand> validResult = ValidationResult.valid(deleteBoardCommand);
            Failure cardDeleteFailure = Failure.ofInternalServerError("카드 삭제 실패");
            when(boardValidator.validateDelete(deleteBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canDeleteBoard(testBoardId, testUserId)).thenReturn(Either.right(true));
            when(cardRepository.deleteByBoardId(testBoardId)).thenReturn(Either.left(cardDeleteFailure));

            // when
            Either<Failure, Void> result = boardManagementService.deleteBoard(deleteBoardCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(cardDeleteFailure);
            verify(cardRepository).deleteByBoardId(testBoardId);
            verify(boardListRepository, never()).deleteByBoardId(any());
            verify(boardMemberRepository, never()).deleteByBoardId(any());
            verify(labelRepository, never()).deleteByBoardId(any());
            verify(boardRepository, never()).delete(any());
        }

        @Test
        @DisplayName("라벨 삭제 실패 시 실패를 반환해야 한다")
        void deleteBoard_withLabelDeleteFailure_shouldReturnFailure() {
            // given
            ValidationResult<DeleteBoardCommand> validResult = ValidationResult.valid(deleteBoardCommand);
            Failure labelDeleteFailure = Failure.ofInternalServerError("라벨 삭제 실패");
            when(boardValidator.validateDelete(deleteBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canDeleteBoard(testBoardId, testUserId)).thenReturn(Either.right(true));
            when(cardRepository.deleteByBoardId(testBoardId)).thenReturn(Either.right(null));
            doNothing().when(boardListRepository).deleteByBoardId(testBoardId);
            when(boardMemberRepository.deleteByBoardId(testBoardId)).thenReturn(Either.right(null));
            when(labelRepository.deleteByBoardId(testBoardId)).thenReturn(Either.left(labelDeleteFailure));

            // when
            Either<Failure, Void> result = boardManagementService.deleteBoard(deleteBoardCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(labelDeleteFailure);
            verify(cardRepository).deleteByBoardId(testBoardId);
            verify(boardListRepository).deleteByBoardId(testBoardId);
            verify(boardMemberRepository).deleteByBoardId(testBoardId);
            verify(labelRepository).deleteByBoardId(testBoardId);
            verify(boardRepository, never()).delete(any());
        }

        @Test
        @DisplayName("보드 삭제 실패 시 실패를 반환해야 한다")
        void deleteBoard_withBoardDeleteFailure_shouldReturnFailure() {
            // given
            ValidationResult<DeleteBoardCommand> validResult = ValidationResult.valid(deleteBoardCommand);
            Failure boardDeleteFailure = Failure.ofInternalServerError("보드 삭제 실패");
            when(boardValidator.validateDelete(deleteBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canDeleteBoard(testBoardId, testUserId)).thenReturn(Either.right(true));
            when(cardRepository.deleteByBoardId(testBoardId)).thenReturn(Either.right(null));
            doNothing().when(boardListRepository).deleteByBoardId(testBoardId);
            when(boardMemberRepository.deleteByBoardId(testBoardId)).thenReturn(Either.right(null));
            when(labelRepository.deleteByBoardId(testBoardId)).thenReturn(Either.right(null));
            when(boardRepository.delete(testBoardId)).thenReturn(Either.left(boardDeleteFailure));

            // when
            Either<Failure, Void> result = boardManagementService.deleteBoard(deleteBoardCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(boardDeleteFailure);
            verify(cardRepository).deleteByBoardId(testBoardId);
            verify(boardListRepository).deleteByBoardId(testBoardId);
            verify(boardMemberRepository).deleteByBoardId(testBoardId);
            verify(labelRepository).deleteByBoardId(testBoardId);
            verify(boardRepository).delete(testBoardId);
        }
    }

    // ==================== ARCHIVE BOARD TESTS ====================

    @Nested
    @DisplayName("archiveBoard 메서드 테스트")
    class ArchiveBoardTests {

        @Test
        @DisplayName("유효한 요청으로 보드 아카이브 시 성공해야 한다")
        void archiveBoard_withValidRequest_shouldSucceed() {
            // given
            ValidationResult<ArchiveBoardCommand> validResult = ValidationResult.valid(archiveBoardCommand);
            when(boardValidator.validateArchive(archiveBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
            when(boardPermissionService.canArchiveBoard(testBoardId, testUserId)).thenReturn(Either.right(true));
            when(boardRepository.save(any(Board.class))).thenReturn(Either.right(testBoard));

            // when
            Either<Failure, Board> result = boardManagementService.archiveBoard(archiveBoardCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testBoard);
            verify(boardValidator).validateArchive(archiveBoardCommand);
            verify(userFinder).checkUserExists(testUserId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canArchiveBoard(testBoardId, testUserId);
            verify(boardRepository).save(any(Board.class));
        }
    }

    // ==================== UNARCHIVE BOARD TESTS ====================

    @Nested
    @DisplayName("unarchiveBoard 메서드 테스트")
    class UnarchiveBoardTests {

        @Test
        @DisplayName("유효한 요청으로 보드 언아카이브 시 성공해야 한다")
        void unarchiveBoard_withValidRequest_shouldSucceed() {
            // given
            Board archivedBoard = createArchivedBoard();
            ValidationResult<ArchiveBoardCommand> validResult = ValidationResult.valid(archiveBoardCommand);
            when(boardValidator.validateArchive(archiveBoardCommand)).thenReturn(validResult);
            when(userFinder.checkUserExists(testUserId)).thenReturn(true);
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(archivedBoard));
            when(boardPermissionService.canArchiveBoard(testBoardId, testUserId)).thenReturn(Either.right(true));
            when(boardRepository.save(any(Board.class))).thenReturn(Either.right(archivedBoard));

            // when
            Either<Failure, Board> result = boardManagementService.unarchiveBoard(archiveBoardCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(archivedBoard);
            verify(boardValidator).validateArchive(archiveBoardCommand);
            verify(userFinder).checkUserExists(testUserId);
            verify(boardRepository).findById(testBoardId);
            verify(boardPermissionService).canArchiveBoard(testBoardId, testUserId);
            verify(boardRepository).save(any(Board.class));
        }
    }
}