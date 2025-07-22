package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.validation.DeleteBoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.repository.CardRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteBoardListServiceTest {

        private DeleteBoardListService deleteBoardListService;

        @Mock
        private DeleteBoardListValidator deleteBoardListValidator;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        @Mock
        private CardRepository cardRepository;

        @Mock
        private BoardPermissionService boardPermissionService;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @BeforeEach
        void setUp() {
                deleteBoardListService = new DeleteBoardListService(
                                deleteBoardListValidator,
                                boardRepository,
                                boardListRepository,
                                cardRepository,
                                boardPermissionService,
                                validationMessageResolver);
        }

        private DeleteBoardListCommand createValidCommand() {
                return new DeleteBoardListCommand(
                                new ListId(),
                                new UserId());
        }

        private Board createValidBoard(BoardId boardId, UserId ownerId) {
                return Board.builder()
                                .boardId(boardId)
                                .title("테스트 보드")
                                .description("테스트 보드 설명")
                                .isArchived(false)
                                .ownerId(ownerId)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        private BoardList createValidBoardList(ListId listId, BoardId boardId, int position) {
                return BoardList.create(
                                "테스트 리스트",
                                "테스트 리스트 설명",
                                position,
                                ListColor.of("#0079BF"),
                                boardId);
        }

        @Test
        @DisplayName("유효한 정보로 리스트 삭제가 성공해야 한다")
        void deleteBoardList_withValidData_shouldReturnSuccess() {
                // given
                DeleteBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList listToDelete = createValidBoardList(command.listId(), boardId, 2);

                when(deleteBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(listToDelete));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardPermissionService.canWriteBoard(boardId, command.userId()))
                                .thenReturn(Either.right(true));
                when(cardRepository.deleteByListId(command.listId()))
                                .thenReturn(Either.right(null));
                when(boardListRepository.findByBoardIdAndPositionGreaterThan(boardId, 2))
                                .thenReturn(List.of());

                // when
                Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();

                verify(deleteBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).deleteById(command.listId());
                verify(boardListRepository).findByBoardIdAndPositionGreaterThan(boardId, 2);
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void deleteBoardList_withInvalidData_shouldReturnInputError() {
                // given
                DeleteBoardListCommand command = createValidCommand();
                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("listId")
                                .message("리스트 ID는 필수입니다")
                                .rejectedValue(command.listId())
                                .build();
                ValidationResult<DeleteBoardListCommand> invalidResult = ValidationResult.invalid(violation);

                when(deleteBoardListValidator.validate(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getViolations()).hasSize(1);
                assertThat(inputError.getViolations().get(0).field()).isEqualTo("listId");
                assertThat(inputError.getViolations().get(0).message()).isEqualTo("리스트 ID는 필수입니다");

                verify(deleteBoardListValidator).validate(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("리스트를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
        void deleteBoardList_withNonExistentList_shouldReturnNotFoundFailure() {
                // given
                DeleteBoardListCommand command = createValidCommand();

                when(deleteBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.empty());

                // when
                Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");

                verify(deleteBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
        }

        @Test
        @DisplayName("보드를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
        void deleteBoardList_withNonExistentBoard_shouldReturnNotFoundFailure() {
                // given
                DeleteBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                BoardList listToDelete = createValidBoardList(command.listId(), boardId, 2);

                when(deleteBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(listToDelete));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.empty());

                // when
                Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");

                verify(deleteBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
        }

        @Test
        @DisplayName("보드 소유자가 아닌 경우 PermissionDenied 오류를 반환해야 한다")
        void deleteBoardList_withUnauthorizedUser_shouldReturnPermissionDeniedFailure() {
                // given
                DeleteBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                UserId differentUserId = new UserId();
                Board board = createValidBoard(boardId, differentUserId);
                BoardList listToDelete = createValidBoardList(command.listId(), boardId, 2);

                when(deleteBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(listToDelete));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardPermissionService.canWriteBoard(boardId, command.userId()))
                                .thenReturn(Either.right(false));
                when(validationMessageResolver.getMessage("validation.boardlist.delete.access.denied"))
                                .thenReturn("리스트 삭제 권한이 없습니다");

                // when
                Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) result.getLeft();
                assertThat(permissionDenied.getMessage()).isEqualTo("리스트 삭제 권한이 없습니다");

                verify(deleteBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
        }

        @Test
        @DisplayName("리스트 삭제 후 position 재정렬이 성공해야 한다")
        void deleteBoardList_withRemainingLists_shouldReorderPositions() {
                // given
                DeleteBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList listToDelete = createValidBoardList(command.listId(), boardId, 2);
                BoardList remainingList1 = createValidBoardList(new ListId(), boardId, 3);
                BoardList remainingList2 = createValidBoardList(new ListId(), boardId, 4);

                when(deleteBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(listToDelete));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardPermissionService.canWriteBoard(boardId, command.userId()))
                                .thenReturn(Either.right(true));
                when(cardRepository.deleteByListId(command.listId()))
                                .thenReturn(Either.right(null));
                when(boardListRepository.findByBoardIdAndPositionGreaterThan(boardId, 2))
                                .thenReturn(List.of(remainingList1, remainingList2));

                // when
                Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();

                verify(deleteBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).deleteById(command.listId());
                verify(boardListRepository).findByBoardIdAndPositionGreaterThan(boardId, 2);
                verify(boardListRepository).saveAll(any());
        }

        @Test
        @DisplayName("리스트 삭제 중 예외 발생 시 InternalError를 반환해야 한다")
        void deleteBoardList_withDeleteException_shouldReturnInternalError() {
                // given
                DeleteBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList listToDelete = createValidBoardList(command.listId(), boardId, 2);

                when(deleteBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(listToDelete));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardPermissionService.canWriteBoard(boardId, command.userId()))
                                .thenReturn(Either.right(true));
                when(cardRepository.deleteByListId(command.listId()))
                                .thenReturn(Either.right(null));
                doThrow(new RuntimeException("데이터베이스 오류"))
                                .when(boardListRepository).deleteById(command.listId());

                // when
                Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                assertThat(internalError.getErrorCode()).isEqualTo("INTERNAL_ERROR");
                assertThat(internalError.getMessage()).isEqualTo("데이터베이스 오류");

                verify(deleteBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).deleteById(command.listId());
        }

        @Test
        @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
        void deleteBoardList_withMultipleValidationErrors_shouldReturnAllErrors() {
                // given
                DeleteBoardListCommand command = createValidCommand();
                Failure.FieldViolation violation1 = Failure.FieldViolation.builder()
                                .field("listId")
                                .message("리스트 ID는 필수입니다")
                                .rejectedValue(command.listId())
                                .build();
                Failure.FieldViolation violation2 = Failure.FieldViolation.builder()
                                .field("userId")
                                .message("사용자 ID는 필수입니다")
                                .rejectedValue(command.userId())
                                .build();
                ValidationResult<DeleteBoardListCommand> invalidResult = ValidationResult
                                .invalid(io.vavr.collection.List.of(violation1, violation2));

                when(deleteBoardListValidator.validate(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getViolations()).hasSize(2);
                assertThat(inputError.getViolations().get(0).field()).isEqualTo("listId");
                assertThat(inputError.getViolations().get(0).message()).isEqualTo("리스트 ID는 필수입니다");
                assertThat(inputError.getViolations().get(1).field()).isEqualTo("userId");
                assertThat(inputError.getViolations().get(1).message()).isEqualTo("사용자 ID는 필수입니다");

                verify(deleteBoardListValidator).validate(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("position 재정렬 중 예외가 발생해도 삭제는 성공해야 한다")
        void deleteBoardList_withReorderException_shouldStillSucceed() {
                // given
                DeleteBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList listToDelete = createValidBoardList(command.listId(), boardId, 2);
                BoardList remainingList = createValidBoardList(new ListId(), boardId, 3);

                when(deleteBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(listToDelete));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardPermissionService.canWriteBoard(boardId, command.userId()))
                                .thenReturn(Either.right(true));
                when(cardRepository.deleteByListId(command.listId()))
                                .thenReturn(Either.right(null));
                when(boardListRepository.findByBoardIdAndPositionGreaterThan(boardId, 2))
                                .thenReturn(List.of(remainingList));
                doThrow(new RuntimeException("재정렬 오류"))
                                .when(boardListRepository).saveAll(any());

                // when
                Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();

                verify(deleteBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).deleteById(command.listId());
                verify(boardListRepository).findByBoardIdAndPositionGreaterThan(boardId, 2);
                verify(boardListRepository).saveAll(any());
        }
}