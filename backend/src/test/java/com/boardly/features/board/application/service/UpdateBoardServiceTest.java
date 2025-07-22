package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.validation.UpdateBoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
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

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateBoardServiceTest {

        private UpdateBoardService updateBoardService;

        @Mock
        private UpdateBoardValidator updateBoardValidator;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private ValidationMessageResolver messageResolver;

        @Mock
        private BoardPermissionService boardPermissionService;

        private final UserId ownerId = new UserId();
        private final UserId otherUserId = new UserId();
        private final BoardId boardId = new BoardId();

        @BeforeEach
        void setUp() {
                updateBoardService = new UpdateBoardService(updateBoardValidator, boardRepository, messageResolver,
                                boardPermissionService);
        }

        private UpdateBoardCommand createValidCommand() {
                return UpdateBoardCommand.of(boardId, "새로운 제목", "새로운 설명", ownerId);
        }

        private UpdateBoardCommand createCommandWithNullValues() {
                return UpdateBoardCommand.of(boardId, null, null, ownerId);
        }

        private UpdateBoardCommand createCommandWithOnlyTitle() {
                return UpdateBoardCommand.of(boardId, "새로운 제목", null, ownerId);
        }

        private UpdateBoardCommand createCommandWithOnlyDescription() {
                return UpdateBoardCommand.of(boardId, null, "새로운 설명", ownerId);
        }

        private UpdateBoardCommand createCommandWithUnauthorizedUser() {
                return UpdateBoardCommand.of(boardId, "새로운 제목", "새로운 설명", otherUserId);
        }

        private Board createValidBoard(String title, String description, UserId ownerId) {
                return Board.builder()
                                .boardId(boardId)
                                .title(title)
                                .description(description)
                                .isArchived(false)
                                .ownerId(ownerId)
                                .isStarred(false)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        private Board createArchivedBoard(String title, String description, UserId ownerId) {
                return Board.builder()
                                .boardId(boardId)
                                .title(title)
                                .description(description)
                                .isArchived(true)
                                .ownerId(ownerId)
                                .isStarred(false)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        @Test
        @DisplayName("유효한 정보로 보드 업데이트가 성공해야 한다")
        void updateBoard_withValidData_shouldReturnUpdatedBoard() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);
                Board updatedBoard = createValidBoard(command.title(), command.description(), ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(updatedBoard));

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(command.title());
                assertThat(result.get().getDescription()).isEqualTo(command.description());
                assertThat(result.get().getOwnerId()).isEqualTo(ownerId);

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("제목만 변경하는 경우 성공해야 한다")
        void updateBoard_withOnlyTitleChange_shouldReturnUpdatedBoard() {
                // given
                UpdateBoardCommand command = createCommandWithOnlyTitle();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);
                Board updatedBoard = createValidBoard(command.title(), existingBoard.getDescription(), ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(updatedBoard));

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(command.title());
                assertThat(result.get().getDescription()).isEqualTo(existingBoard.getDescription());

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("설명만 변경하는 경우 성공해야 한다")
        void updateBoard_withOnlyDescriptionChange_shouldReturnUpdatedBoard() {
                // given
                UpdateBoardCommand command = createCommandWithOnlyDescription();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);
                Board updatedBoard = createValidBoard(existingBoard.getTitle(), command.description(), ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(updatedBoard));

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(existingBoard.getTitle());
                assertThat(result.get().getDescription()).isEqualTo(command.description());

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("변경사항이 없는 경우 기존 보드를 반환해야 한다")
        void updateBoard_withNoChanges_shouldReturnExistingBoard() {
                // given
                UpdateBoardCommand command = createCommandWithNullValues();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(existingBoard.getTitle());
                assertThat(result.get().getDescription()).isEqualTo(existingBoard.getDescription());

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository, never()).save(any());
        }

        @Test
        @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
        void updateBoard_withInvalidData_shouldReturnValidationFailure() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("boardId")
                                .message("validation.board.id.required")
                                .rejectedValue(command.boardId())
                                .build();
                ValidationResult<UpdateBoardCommand> invalidResult = ValidationResult.invalid(violation);

                when(messageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 올바르지 않습니다");
                when(updateBoardValidator.validate(command))
                                .thenReturn(invalidResult);

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");
                assertThat(inputError.getViolations()).hasSize(1);

                verify(updateBoardValidator).validate(command);
                verify(messageResolver).getMessage("validation.input.invalid");
                verify(boardRepository, never()).findById(any());
        }

        @Test
        @DisplayName("보드를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
        void updateBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
                // given
                UpdateBoardCommand command = createValidCommand();

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.empty());
                when(messageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다");

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(messageResolver).getMessage("validation.board.not.found");
                verify(boardRepository, never()).save(any());
        }

        @Test
        @DisplayName("권한이 없는 사용자가 수정 시도 시 Forbidden 오류를 반환해야 한다")
        void updateBoard_withUnauthorizedUser_shouldReturnForbiddenFailure() {
                // given
                UpdateBoardCommand command = createCommandWithUnauthorizedUser();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(messageResolver.getMessage("validation.board.modification.access.denied"))
                                .thenReturn("보드 수정 권한이 없습니다");

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                assertThat(result.getLeft().getMessage()).isEqualTo("보드 수정 권한이 없습니다");

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(messageResolver).getMessage("validation.board.modification.access.denied");
                verify(boardRepository, never()).save(any());
        }

        @Test
        @DisplayName("아카이브된 보드 수정 시도 시 Conflict 오류를 반환해야 한다")
        void updateBoard_withArchivedBoard_shouldReturnConflictFailure() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Board archivedBoard = createArchivedBoard("기존 제목", "기존 설명", ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(archivedBoard));
                when(messageResolver.getMessage("validation.board.archived.modification.denied"))
                                .thenReturn("아카이브된 보드는 수정할 수 없습니다");

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
                assertThat(result.getLeft().getMessage()).isEqualTo("아카이브된 보드는 수정할 수 없습니다");

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(messageResolver).getMessage("validation.board.archived.modification.denied");
                verify(boardRepository, never()).save(any());
        }

        @Test
        @DisplayName("보드 저장 실패 시 저장소 오류를 반환해야 한다")
        void updateBoard_withSaveFailure_shouldReturnRepositoryFailure() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);
                Failure saveFailure = Failure.ofInternalError("데이터베이스 연결 오류", "DB_CONNECTION_ERROR", null);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.left(saveFailure));

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isEqualTo(saveFailure);

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("보드 저장 중 예외 발생 시 InternalServerError를 반환해야 한다")
        void updateBoard_withSaveException_shouldReturnInternalServerError() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardRepository.save(any(Board.class)))
                                .thenThrow(new RuntimeException("데이터베이스 오류"));
                when(messageResolver.getMessage("validation.board.update.error"))
                                .thenReturn("보드 업데이트 중 오류가 발생했습니다");

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                assertThat(result.getLeft().getMessage()).isEqualTo("보드 업데이트 중 오류가 발생했습니다");

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository).save(any(Board.class));
                verify(messageResolver).getMessage("validation.board.update.error");
        }

        @Test
        @DisplayName("보드 변경 중 예외 발생 시 InternalServerError를 반환해야 한다")
        void updateBoard_withModificationException_shouldReturnInternalServerError() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);

                // Board.updateTitle 메서드에서 예외가 발생하도록 모킹
                Board spyBoard = spy(existingBoard);
                doThrow(new RuntimeException("변경 중 오류")).when(spyBoard).updateTitle(any());

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(spyBoard));
                when(messageResolver.getMessage("validation.board.modification.error"))
                                .thenReturn("보드 변경 중 오류가 발생했습니다");

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                assertThat(result.getLeft().getMessage()).isEqualTo("보드 변경 중 오류가 발생했습니다");

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(messageResolver).getMessage("validation.board.modification.error");
                verify(boardRepository, never()).save(any());
        }

        @Test
        @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
        void updateBoard_withMultipleValidationErrors_shouldReturnAllErrors() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Failure.FieldViolation boardIdViolation = Failure.FieldViolation.builder()
                                .field("boardId")
                                .message("validation.board.id.required")
                                .rejectedValue(command.boardId())
                                .build();
                Failure.FieldViolation requestedByViolation = Failure.FieldViolation.builder()
                                .field("requestedBy")
                                .message("validation.board.requestedBy.required")
                                .rejectedValue(command.requestedBy())
                                .build();
                ValidationResult<UpdateBoardCommand> invalidResult = ValidationResult.invalid(
                                io.vavr.collection.List.of(boardIdViolation, requestedByViolation));

                when(messageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 올바르지 않습니다");
                when(updateBoardValidator.validate(command))
                                .thenReturn(invalidResult);

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");
                assertThat(inputError.getViolations()).hasSize(2);

                verify(updateBoardValidator).validate(command);
                verify(messageResolver).getMessage("validation.input.invalid");
                verify(boardRepository, never()).findById(any());
        }

        @Test
        @DisplayName("보드 변경사항이 올바르게 적용되어야 한다")
        void updateBoard_shouldApplyChangesCorrectly() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);
                Board updatedBoard = createValidBoard(command.title(), command.description(), ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(updatedBoard));

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                Board resultBoard = result.get();
                assertThat(resultBoard.getTitle()).isEqualTo(command.title());
                assertThat(resultBoard.getDescription()).isEqualTo(command.description());
                assertThat(resultBoard.getBoardId()).isEqualTo(boardId);
                assertThat(resultBoard.getOwnerId()).isEqualTo(ownerId);

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("변경사항이 없는 경우 저장소를 호출하지 않아야 한다")
        void updateBoard_withNoChanges_shouldNotCallRepositorySave() {
                // given
                UpdateBoardCommand command = createCommandWithNullValues();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).isEqualTo(existingBoard);

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository, never()).save(any());
        }

        @Test
        @DisplayName("보드 ID가 동일한지 확인해야 한다")
        void updateBoard_shouldMaintainSameBoardId() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);
                Board updatedBoard = createValidBoard(command.title(), command.description(), ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(updatedBoard));

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getBoardId()).isEqualTo(boardId);

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository).save(any(Board.class));
        }

        @Test
        @DisplayName("소유자 ID가 변경되지 않아야 한다")
        void updateBoard_shouldMaintainSameOwnerId() {
                // given
                UpdateBoardCommand command = createValidCommand();
                Board existingBoard = createValidBoard("기존 제목", "기존 설명", ownerId);
                Board updatedBoard = createValidBoard(command.title(), command.description(), ownerId);

                when(updateBoardValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(boardId))
                                .thenReturn(java.util.Optional.of(existingBoard));
                when(boardRepository.save(any(Board.class)))
                                .thenReturn(Either.right(updatedBoard));

                // when
                Either<Failure, Board> result = updateBoardService.updateBoard(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getOwnerId()).isEqualTo(ownerId);

                verify(updateBoardValidator).validate(command);
                verify(boardRepository).findById(boardId);
                verify(boardRepository).save(any(Board.class));
        }
}