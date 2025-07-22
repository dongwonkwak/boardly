package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.*;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BoardManagementServiceTest {

        private BoardManagementService boardManagementService;

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
        private BoardPermissionService boardPermissionService;

        @Mock
        private UserFinder userFinder;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @BeforeEach
        void setUp() {
                boardManagementService = new BoardManagementService(
                                boardValidator,
                                boardRepository,
                                boardListRepository,
                                cardRepository,
                                boardMemberRepository,
                                boardPermissionService,
                                userFinder,
                                validationMessageResolver);
        }

        // ==================== HELPER METHODS ====================

        private CreateBoardCommand createValidCreateCommand() {
                return CreateBoardCommand.of(
                                "테스트 보드",
                                "테스트 보드 설명",
                                new UserId());
        }

        private UpdateBoardCommand createValidUpdateCommand() {
                return UpdateBoardCommand.of(
                                new BoardId(),
                                "업데이트된 제목",
                                "업데이트된 설명",
                                new UserId());
        }

        private DeleteBoardCommand createValidDeleteCommand() {
                return DeleteBoardCommand.of(
                                new BoardId(),
                                new UserId());
        }

        private ArchiveBoardCommand createValidArchiveCommand() {
                return ArchiveBoardCommand.of(
                                new BoardId(),
                                new UserId());
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

        private ValidationResult<CreateBoardCommand> createInvalidValidationResult() {
                return ValidationResult.invalid("title", "제목은 필수입니다", null);
        }

        private ValidationResult<UpdateBoardCommand> createInvalidUpdateValidationResult() {
                return ValidationResult.invalid("title", "제목은 필수입니다", null);
        }

        private ValidationResult<DeleteBoardCommand> createInvalidDeleteValidationResult() {
                return ValidationResult.invalid("boardId", "보드 ID는 필수입니다", null);
        }

        private ValidationResult<ArchiveBoardCommand> createInvalidArchiveValidationResult() {
                return ValidationResult.invalid("boardId", "보드 ID는 필수입니다", null);
        }

        // ==================== CREATE BOARD TESTS ====================

        @Test
        @DisplayName("유효한 정보로 보드 생성이 성공해야 한다")
        void createBoard_withValidData_shouldReturnCreatedBoard() {
                // given
                CreateBoardCommand command = createValidCreateCommand();
                Board createdBoard = createValidBoard(new BoardId(), command.ownerId(), false);

                when(boardValidator.validateCreate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.ownerId())).thenReturn(true);
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(createdBoard));

                // when
                Either<Failure, Board> result = boardManagementService.createBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(createdBoard.getTitle());
                assertThat(result.get().getDescription()).isEqualTo(createdBoard.getDescription());
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void createBoard_withInvalidData_shouldReturnInputError() {
                // given
                CreateBoardCommand command = createValidCreateCommand();

                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력이 유효하지 않습니다");
                when(boardValidator.validateCreate(command))
                                .thenReturn(createInvalidValidationResult());

                // when
                Either<Failure, Board> result = boardManagementService.createBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다");
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 보드 생성 시도 시 NotFound 오류를 반환해야 한다")
        void createBoard_withNonExistentUser_shouldReturnNotFoundFailure() {
                // given
                CreateBoardCommand command = createValidCreateCommand();

                when(validationMessageResolver.getMessage("validation.user.not.found"))
                                .thenReturn("사용자를 찾을 수 없습니다");
                when(boardValidator.validateCreate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.ownerId())).thenReturn(false);

                // when
                Either<Failure, Board> result = boardManagementService.createBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("보드 저장 중 예외 발생 시 InternalServerError를 반환해야 한다")
        void createBoard_withSaveException_shouldReturnInternalServerError() {
                // given
                CreateBoardCommand command = createValidCreateCommand();

                when(boardValidator.validateCreate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.ownerId())).thenReturn(true);
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.left(Failure.ofInternalError("저장 실패", "SAVE_ERROR", null)));

                // when
                Either<Failure, Board> result = boardManagementService.createBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
        }

        // ==================== UPDATE BOARD TESTS ====================

        @Test
        @DisplayName("유효한 정보로 보드 업데이트가 성공해야 한다")
        void updateBoard_withValidData_shouldReturnUpdatedBoard() {
                // given
                UpdateBoardCommand command = createValidUpdateCommand();
                Board existingBoard = createValidBoard(command.boardId(), command.requestedBy(), false);
                Board updatedBoard = createValidBoard(command.boardId(), command.requestedBy(), false);
                updatedBoard.updateTitle(command.title());
                updatedBoard.updateDescription(command.description());

                when(boardValidator.validateUpdate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardPermissionService.canWriteBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(true));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(updatedBoard));

                // when
                Either<Failure, Board> result = boardManagementService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(command.title());
                assertThat(result.get().getDescription()).isEqualTo(command.description());
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void updateBoard_withInvalidData_shouldReturnInputError() {
                // given
                UpdateBoardCommand command = createValidUpdateCommand();

                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력이 유효하지 않습니다");
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardValidator.validateUpdate(command))
                                .thenReturn(createInvalidUpdateValidationResult());

                // when
                Either<Failure, Board> result = boardManagementService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 보드 업데이트 시도 시 NotFound 오류를 반환해야 한다")
        void updateBoard_withNonExistentUser_shouldReturnNotFoundFailure() {
                // given
                UpdateBoardCommand command = createValidUpdateCommand();

                when(validationMessageResolver.getMessage("validation.user.not.found"))
                                .thenReturn("사용자를 찾을 수 없습니다");
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(false);

                // when
                Either<Failure, Board> result = boardManagementService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("존재하지 않는 보드 업데이트 시도 시 NotFound 오류를 반환해야 한다")
        void updateBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
                // given
                UpdateBoardCommand command = createValidUpdateCommand();

                when(validationMessageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다");
                when(boardValidator.validateUpdate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.empty());

                // when
                Either<Failure, Board> result = boardManagementService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("권한이 없는 사용자가 보드 업데이트 시도 시 PermissionDenied 오류를 반환해야 한다")
        void updateBoard_withoutPermission_shouldReturnPermissionDeniedFailure() {
                // given
                UpdateBoardCommand command = createValidUpdateCommand();
                Board existingBoard = createValidBoard(command.boardId(), command.requestedBy(), false);

                when(validationMessageResolver.getMessage("validation.board.modification.access.denied"))
                                .thenReturn("보드 수정 권한이 없습니다");
                when(boardValidator.validateUpdate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardPermissionService.canWriteBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(false));

                // when
                Either<Failure, Board> result = boardManagementService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("보드 수정 권한이 없습니다");
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("아카이브된 보드 업데이트 시도 시 Conflict 오류를 반환해야 한다")
        void updateBoard_withArchivedBoard_shouldReturnConflictFailure() {
                // given
                UpdateBoardCommand command = createValidUpdateCommand();
                Board archivedBoard = createValidBoard(command.boardId(), command.requestedBy(), true);

                when(validationMessageResolver.getMessage("validation.board.archived.modification.denied"))
                                .thenReturn("아카이브된 보드는 수정할 수 없습니다");
                when(boardValidator.validateUpdate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(archivedBoard));
                when(boardPermissionService.canWriteBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(true));

                // when
                Either<Failure, Board> result = boardManagementService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("아카이브된 보드는 수정할 수 없습니다");
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("변경 사항이 없는 보드 업데이트 시 기존 보드를 반환해야 한다")
        void updateBoard_withNoChanges_shouldReturnExistingBoard() {
                // given
                BoardId boardId = new BoardId();
                UserId userId = new UserId();
                UpdateBoardCommand command = UpdateBoardCommand.of(boardId, "테스트 보드", "테스트 보드 설명", userId);
                Board existingBoard = createValidBoard(boardId, userId, false);

                when(boardValidator.validateUpdate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardPermissionService.canWriteBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(true));

                // when
                Either<Failure, Board> result = boardManagementService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).isEqualTo(existingBoard);
                verify(boardRepository, never()).save(any(Board.class));
        }

        // ==================== DELETE BOARD TESTS ====================

        @Test
        @DisplayName("유효한 정보로 보드 삭제가 성공해야 한다")
        void deleteBoard_withValidData_shouldReturnSuccess() {
                // given
                DeleteBoardCommand command = createValidDeleteCommand();
                Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                when(boardValidator.validateDelete(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(board));
                when(boardPermissionService.canDeleteBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(true));
                when(cardRepository.deleteByBoardId(command.boardId()))
                                .thenReturn(Either.right(null));
                when(boardMemberRepository.deleteByBoardId(command.boardId()))
                                .thenReturn(Either.right(null));
                when(boardRepository.delete(command.boardId()))
                                .thenReturn(Either.right(null));

                // when
                Either<Failure, Void> result = boardManagementService.deleteBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                verify(cardRepository).deleteByBoardId(command.boardId());
                verify(boardListRepository).deleteByBoardId(command.boardId());
                verify(boardMemberRepository).deleteByBoardId(command.boardId());
                verify(boardRepository).delete(command.boardId());
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void deleteBoard_withInvalidData_shouldReturnInputError() {
                // given
                DeleteBoardCommand command = createValidDeleteCommand();

                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력이 유효하지 않습니다");
                when(boardValidator.validateDelete(command))
                                .thenReturn(createInvalidDeleteValidationResult());

                // when
                Either<Failure, Void> result = boardManagementService.deleteBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                verify(boardRepository, never()).delete(any(BoardId.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 보드 삭제 시도 시 NotFound 오류를 반환해야 한다")
        void deleteBoard_withNonExistentUser_shouldReturnNotFoundFailure() {
                // given
                DeleteBoardCommand command = createValidDeleteCommand();

                when(validationMessageResolver.getMessage("validation.user.not.found"))
                                .thenReturn("사용자를 찾을 수 없습니다");
                when(boardValidator.validateDelete(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(false);

                // when
                Either<Failure, Void> result = boardManagementService.deleteBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
                verify(boardRepository, never()).delete(any(BoardId.class));
        }

        @Test
        @DisplayName("존재하지 않는 보드 삭제 시도 시 NotFound 오류를 반환해야 한다")
        void deleteBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
                // given
                DeleteBoardCommand command = createValidDeleteCommand();

                when(validationMessageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다");
                when(boardValidator.validateDelete(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.empty());

                // when
                Either<Failure, Void> result = boardManagementService.deleteBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");
                verify(boardRepository, never()).delete(any(BoardId.class));
        }

        @Test
        @DisplayName("권한이 없는 사용자가 보드 삭제 시도 시 PermissionDenied 오류를 반환해야 한다")
        void deleteBoard_withoutPermission_shouldReturnPermissionDeniedFailure() {
                // given
                DeleteBoardCommand command = createValidDeleteCommand();
                Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                when(validationMessageResolver.getMessage("validation.board.delete.access.denied"))
                                .thenReturn("보드 삭제 권한이 없습니다");
                when(boardValidator.validateDelete(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(board));
                when(boardPermissionService.canDeleteBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(false));

                // when
                Either<Failure, Void> result = boardManagementService.deleteBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("보드 삭제 권한이 없습니다");
                verify(boardRepository, never()).delete(any(BoardId.class));
        }

        // ==================== ARCHIVE BOARD TESTS ====================

        @Test
        @DisplayName("유효한 정보로 보드 아카이브가 성공해야 한다")
        void archiveBoard_withValidData_shouldReturnArchivedBoard() {
                // given
                ArchiveBoardCommand command = createValidArchiveCommand();
                Board unarchivedBoard = createValidBoard(command.boardId(), command.requestedBy(), false);
                Board archivedBoard = createValidBoard(command.boardId(), command.requestedBy(), true);

                when(boardValidator.validateArchive(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(unarchivedBoard));
                when(boardPermissionService.canArchiveBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(true));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(archivedBoard));

                // when
                Either<Failure, Board> result = boardManagementService.archiveBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().isArchived()).isTrue();
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("이미 아카이브된 보드에 아카이브 시도 시 기존 보드를 반환해야 한다")
        void archiveBoard_withAlreadyArchivedBoard_shouldReturnExistingBoard() {
                // given
                ArchiveBoardCommand command = createValidArchiveCommand();
                Board archivedBoard = createValidBoard(command.boardId(), command.requestedBy(), true);

                when(boardValidator.validateArchive(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(archivedBoard));
                when(boardPermissionService.canArchiveBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(true));

                // when
                Either<Failure, Board> result = boardManagementService.archiveBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).isEqualTo(archivedBoard);
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void archiveBoard_withInvalidData_shouldReturnInputError() {
                // given
                ArchiveBoardCommand command = createValidArchiveCommand();

                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력이 유효하지 않습니다");
                when(boardValidator.validateArchive(command))
                                .thenReturn(createInvalidArchiveValidationResult());

                // when
                Either<Failure, Board> result = boardManagementService.archiveBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 보드 아카이브 시도 시 NotFound 오류를 반환해야 한다")
        void archiveBoard_withNonExistentUser_shouldReturnNotFoundFailure() {
                // given
                ArchiveBoardCommand command = createValidArchiveCommand();

                when(validationMessageResolver.getMessage("validation.user.not.found"))
                                .thenReturn("사용자를 찾을 수 없습니다");
                when(boardValidator.validateArchive(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(false);

                // when
                Either<Failure, Board> result = boardManagementService.archiveBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("존재하지 않는 보드 아카이브 시도 시 NotFound 오류를 반환해야 한다")
        void archiveBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
                // given
                ArchiveBoardCommand command = createValidArchiveCommand();

                when(validationMessageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다");
                when(boardValidator.validateArchive(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.empty());

                // when
                Either<Failure, Board> result = boardManagementService.archiveBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");
                verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("권한이 없는 사용자가 보드 아카이브 시도 시 PermissionDenied 오류를 반환해야 한다")
        void archiveBoard_withoutPermission_shouldReturnPermissionDeniedFailure() {
                // given
                ArchiveBoardCommand command = createValidArchiveCommand();
                Board board = createValidBoard(command.boardId(), command.requestedBy(), false);

                when(validationMessageResolver.getMessage("validation.board.archive.access.denied"))
                                .thenReturn("보드 아카이브 권한이 없습니다");
                when(boardValidator.validateArchive(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(board));
                when(boardPermissionService.canArchiveBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(false));

                // when
                Either<Failure, Board> result = boardManagementService.archiveBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft().getMessage()).isEqualTo("보드 아카이브 권한이 없습니다");
                verify(boardRepository, never()).save(any(Board.class));
        }

        // ==================== UNARCHIVE BOARD TESTS ====================

        @Test
        @DisplayName("유효한 정보로 보드 언아카이브가 성공해야 한다")
        void unarchiveBoard_withValidData_shouldReturnUnarchivedBoard() {
                // given
                ArchiveBoardCommand command = createValidArchiveCommand();
                Board archivedBoard = createValidBoard(command.boardId(), command.requestedBy(), true);
                Board unarchivedBoard = createValidBoard(command.boardId(), command.requestedBy(), false);

                when(boardValidator.validateArchive(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(archivedBoard));
                when(boardPermissionService.canArchiveBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(true));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(unarchivedBoard));

                // when
                Either<Failure, Board> result = boardManagementService.unarchiveBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().isArchived()).isFalse();
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("이미 활성 상태인 보드에 언아카이브 시도 시 기존 보드를 반환해야 한다")
        void unarchiveBoard_withAlreadyActiveBoard_shouldReturnExistingBoard() {
                // given
                ArchiveBoardCommand command = createValidArchiveCommand();
                Board activeBoard = createValidBoard(command.boardId(), command.requestedBy(), false);

                when(boardValidator.validateArchive(command))
                                .thenReturn(ValidationResult.valid(command));
                when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(java.util.Optional.of(activeBoard));
                when(boardPermissionService.canArchiveBoard(command.boardId(), command.requestedBy()))
                                .thenReturn(Either.right(true));

                // when
                Either<Failure, Board> result = boardManagementService.unarchiveBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).isEqualTo(activeBoard);
                verify(boardRepository, never()).save(any(Board.class));
        }
}