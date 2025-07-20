package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.application.validation.ToggleStarBoardValidator;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToggleStarBoardService 테스트")
class ToggleStarBoardServiceTest {

        @Mock
        private ToggleStarBoardValidator boardValidator;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private ValidationMessageResolver messageResolver;

        private ToggleStarBoardService toggleStarBoardService;

        private BoardId boardId;
        private UserId ownerId;
        private ToggleStarBoardCommand command;

        @BeforeEach
        void setUp() {
                boardId = new BoardId();
                ownerId = new UserId();
                command = new ToggleStarBoardCommand(boardId, ownerId);

                toggleStarBoardService = new ToggleStarBoardService(
                                boardValidator, boardRepository, messageResolver);

                // 기본 메시지 모킹 - lenient로 설정하여 사용되지 않는 스텁도 허용
                lenient().when(messageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 올바르지 않습니다");
                lenient().when(messageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다");
                lenient().when(messageResolver.getMessage("validation.board.modification.access.denied"))
                                .thenReturn("보드 수정 권한이 없습니다");
                lenient().when(messageResolver.getMessage("validation.board.star.toggle.error"))
                                .thenReturn("보드 즐겨찾기 상태 변경 중 오류가 발생했습니다");
                lenient().when(messageResolver.getMessage("validation.board.star.save.error"))
                                .thenReturn("보드 즐겨찾기 상태 저장 중 오류가 발생했습니다");
        }

        private Board createBoard(boolean isStarred) {
                return Board.builder()
                                .boardId(boardId)
                                .title("Test Board")
                                .description("Test Description")
                                .isArchived(false)
                                .ownerId(ownerId)
                                .isStarred(isStarred)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        @Nested
        @DisplayName("starringBoard 메서드 테스트")
        class StarringBoardTest {

                @Test
                @DisplayName("즐겨찾기가 아닌 보드를 즐겨찾기로 추가하면 성공해야 한다")
                void shouldStarUnstarredBoard() {
                        // given
                        Board unstarredBoard = createBoard(false);
                        Board starredBoard = createBoard(true);

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(unstarredBoard));
                        when(boardRepository.save(any(Board.class)))
                                        .thenReturn(Either.right(starredBoard));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get().isStarred()).isTrue();

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).save(unstarredBoard);
                }

                @Test
                @DisplayName("이미 즐겨찾기인 보드를 즐겨찾기 추가하려면 변경 없이 성공해야 한다")
                void shouldReturnBoardWithoutChangesWhenAlreadyStarred() {
                        // given
                        Board starredBoard = createBoard(true);

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(starredBoard));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get().isStarred()).isTrue();

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository, never()).save(any(Board.class));
                }

                @Test
                @DisplayName("검증 실패 시 InputError를 반환해야 한다")
                void shouldReturnInputErrorWhenValidationFails() {
                        // given
                        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                        .field("boardId")
                                        .message("보드 ID는 필수입니다")
                                        .rejectedValue(null)
                                        .build();
                        ValidationResult<ToggleStarBoardCommand> invalidResult = ValidationResult.invalid(violation);

                        when(boardValidator.validate(command))
                                        .thenReturn(invalidResult);

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");

                        verify(boardValidator).validate(command);
                        verify(boardRepository, never()).findById(any());
                        verify(boardRepository, never()).save(any());
                }

                @Test
                @DisplayName("존재하지 않는 보드인 경우 NotFound를 반환해야 한다")
                void shouldReturnNotFoundWhenBoardDoesNotExist() {
                        // given
                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository, never()).save(any());
                }

                @Test
                @DisplayName("다른 사용자의 보드인 경우 PermissionDenied를 반환해야 한다")
                void shouldReturnPermissionDeniedWhenNotOwner() {
                        // given
                        UserId differentOwnerId = new UserId();
                        Board otherUsersBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("Other User's Board")
                                        .description("Other User's Description")
                                        .isArchived(false)
                                        .ownerId(differentOwnerId)
                                        .isStarred(false)
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(otherUsersBoard));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("접근이 거부되었습니다.");

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository, never()).save(any());
                }

                @Test
                @DisplayName("보드 저장 실패 시 InternalError를 반환해야 한다")
                void shouldReturnInternalErrorWhenSaveFails() {
                        // given
                        Board unstarredBoard = createBoard(false);
                        Failure saveFailure = Failure.ofInternalError("저장 실패", "SAVE_ERROR", null);

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(unstarredBoard));
                        when(boardRepository.save(any(Board.class)))
                                        .thenReturn(Either.left(saveFailure));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).save(unstarredBoard);
                }

                @Test
                @DisplayName("보드 저장 중 예외 발생 시 InternalError를 반환해야 한다")
                void shouldReturnInternalErrorWhenSaveThrowsException() {
                        // given
                        Board unstarredBoard = createBoard(false);

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(unstarredBoard));
                        when(boardRepository.save(any(Board.class)))
                                        .thenThrow(new RuntimeException("Database error"));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 즐겨찾기 상태 저장 중 오류가 발생했습니다");

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).save(unstarredBoard);
                }
        }

        @Nested
        @DisplayName("unstarringBoard 메서드 테스트")
        class UnstarringBoardTest {

                @Test
                @DisplayName("즐겨찾기인 보드를 즐겨찾기에서 제거하면 성공해야 한다")
                void shouldUnstarStarredBoard() {
                        // given
                        Board starredBoard = createBoard(true);
                        Board unstarredBoard = createBoard(false);

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(starredBoard));
                        when(boardRepository.save(any(Board.class)))
                                        .thenReturn(Either.right(unstarredBoard));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get().isStarred()).isFalse();

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).save(starredBoard);
                }

                @Test
                @DisplayName("즐겨찾기가 아닌 보드를 즐겨찾기 제거하려면 변경 없이 성공해야 한다")
                void shouldReturnBoardWithoutChangesWhenAlreadyUnstarred() {
                        // given
                        Board unstarredBoard = createBoard(false);

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(unstarredBoard));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get().isStarred()).isFalse();

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository, never()).save(any(Board.class));
                }

                @Test
                @DisplayName("검증 실패 시 InputError를 반환해야 한다")
                void shouldReturnInputErrorWhenValidationFails() {
                        // given
                        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                        .field("requestedBy")
                                        .message("요청자 ID는 필수입니다")
                                        .rejectedValue(null)
                                        .build();
                        ValidationResult<ToggleStarBoardCommand> invalidResult = ValidationResult.invalid(violation);

                        when(boardValidator.validate(command))
                                        .thenReturn(invalidResult);

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");

                        verify(boardValidator).validate(command);
                        verify(boardRepository, never()).findById(any());
                        verify(boardRepository, never()).save(any());
                }

                @Test
                @DisplayName("존재하지 않는 보드인 경우 NotFound를 반환해야 한다")
                void shouldReturnNotFoundWhenBoardDoesNotExist() {
                        // given
                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드를 찾을 수 없습니다");

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository, never()).save(any());
                }

                @Test
                @DisplayName("다른 사용자의 보드인 경우 PermissionDenied를 반환해야 한다")
                void shouldReturnPermissionDeniedWhenNotOwner() {
                        // given
                        UserId differentOwnerId = new UserId();
                        Board otherUsersBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("Other User's Board")
                                        .description("Other User's Description")
                                        .isArchived(false)
                                        .ownerId(differentOwnerId)
                                        .isStarred(true)
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(otherUsersBoard));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("접근이 거부되었습니다.");

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository, never()).save(any());
                }

                @Test
                @DisplayName("보드 저장 실패 시 InternalError를 반환해야 한다")
                void shouldReturnInternalErrorWhenSaveFails() {
                        // given
                        Board starredBoard = createBoard(true);
                        Failure saveFailure = Failure.ofInternalError("저장 실패", "SAVE_ERROR", null);

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(starredBoard));
                        when(boardRepository.save(any(Board.class)))
                                        .thenReturn(Either.left(saveFailure));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).save(starredBoard);
                }

                @Test
                @DisplayName("보드 저장 중 예외 발생 시 InternalError를 반환해야 한다")
                void shouldReturnInternalErrorWhenSaveThrowsException() {
                        // given
                        Board starredBoard = createBoard(true);

                        when(boardValidator.validate(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(starredBoard));
                        when(boardRepository.save(any(Board.class)))
                                        .thenThrow(new RuntimeException("Database error"));

                        // when
                        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                        assertThat(result.getLeft().getMessage()).isEqualTo("보드 즐겨찾기 상태 저장 중 오류가 발생했습니다");

                        verify(boardValidator).validate(command);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).save(starredBoard);
                }
        }
}