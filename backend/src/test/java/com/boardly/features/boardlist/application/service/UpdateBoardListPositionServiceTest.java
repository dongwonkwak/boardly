package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.application.validation.UpdateBoardListPositionValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.policy.BoardListMovePolicy;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
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
class UpdateBoardListPositionServiceTest {

        private UpdateBoardListPositionService updateBoardListPositionService;

        @Mock
        private UpdateBoardListPositionValidator updateBoardListPositionValidator;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        @Mock
        private BoardListMovePolicy boardListMovePolicy;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @BeforeEach
        void setUp() {
                updateBoardListPositionService = new UpdateBoardListPositionService(
                                updateBoardListPositionValidator,
                                boardRepository,
                                boardListRepository,
                                boardListMovePolicy,
                                validationMessageResolver);
        }

        private UpdateBoardListPositionCommand createValidCommand() {
                return new UpdateBoardListPositionCommand(
                                new ListId(),
                                new UserId(),
                                2);
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
                                "테스트 리스트 " + position,
                                "테스트 리스트 설명 " + position,
                                position,
                                ListColor.of("#0079BF"),
                                boardId);
        }

        @Test
        @DisplayName("유효한 정보로 리스트 위치 변경이 성공해야 한다")
        void updateBoardListPosition_withValidData_shouldReturnUpdatedBoardLists() {
                // given
                UpdateBoardListPositionCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList list1 = createValidBoardList(new ListId(), boardId, 0);
                BoardList targetList = createValidBoardList(command.listId(), boardId, 1);
                BoardList list3 = createValidBoardList(new ListId(), boardId, 2);
                List<BoardList> originalLists = List.of(list1, targetList, list3);

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(targetList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                                .thenReturn(originalLists);
                when(boardListMovePolicy.canMoveWithinSameBoard(targetList, command.newPosition()))
                                .thenReturn(Either.right(null));
                when(boardListMovePolicy.hasPositionChanged(targetList, command.newPosition()))
                                .thenReturn(true);
                when(boardListRepository.saveAll(any()))
                                .thenReturn(originalLists);

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).hasSize(3);

                verify(updateBoardListPositionValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
                verify(boardListRepository).saveAll(any());
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void updateBoardListPosition_withInvalidData_shouldReturnInputError() {
                // given
                UpdateBoardListPositionCommand command = createValidCommand();
                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("newPosition")
                                .message("새로운 위치는 0 이상이어야 합니다")
                                .rejectedValue(command.newPosition())
                                .build();
                ValidationResult<UpdateBoardListPositionCommand> invalidResult = ValidationResult.invalid(violation);

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getViolations()).hasSize(1);
                assertThat(inputError.getViolations().get(0).field()).isEqualTo("newPosition");
                assertThat(inputError.getViolations().get(0).message()).isEqualTo("새로운 위치는 0 이상이어야 합니다");

                verify(updateBoardListPositionValidator).validate(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("리스트를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
        void updateBoardListPosition_withNonExistentList_shouldReturnNotFoundFailure() {
                // given
                UpdateBoardListPositionCommand command = createValidCommand();

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.empty());

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");

                verify(updateBoardListPositionValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
        }

        @Test
        @DisplayName("보드를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
        void updateBoardListPosition_withNonExistentBoard_shouldReturnNotFoundFailure() {
                // given
                UpdateBoardListPositionCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                BoardList targetList = createValidBoardList(command.listId(), boardId, 1);

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(targetList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.empty());

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");

                verify(updateBoardListPositionValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
        }

        @Test
        @DisplayName("보드 소유자가 아닌 경우 PermissionDenied 오류를 반환해야 한다")
        void updateBoardListPosition_withUnauthorizedUser_shouldReturnPermissionDeniedFailure() {
                // given
                UpdateBoardListPositionCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                UserId differentUserId = new UserId();
                Board board = createValidBoard(boardId, differentUserId);
                BoardList targetList = createValidBoardList(command.listId(), boardId, 1);

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(targetList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) result.getLeft();
                assertThat(permissionDenied.getErrorCode()).isEqualTo("UNAUTHORIZED_ACCESS");

                verify(updateBoardListPositionValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
        }

        @Test
        @DisplayName("새로운 위치가 리스트 개수를 초과하는 경우 ResourceConflict 오류를 반환해야 한다")
        void updateBoardListPosition_withInvalidPosition_shouldReturnResourceConflictFailure() {
                // given
                UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                                new ListId(),
                                new UserId(),
                                5);
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList targetList = createValidBoardList(command.listId(), boardId, 1);
                List<BoardList> existingLists = List.of(
                                createValidBoardList(new ListId(), boardId, 0),
                                targetList,
                                createValidBoardList(new ListId(), boardId, 2));

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(targetList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                                .thenReturn(existingLists);
                when(boardListMovePolicy.canMoveWithinSameBoard(targetList, command.newPosition()))
                                .thenReturn(Either.left(Failure.ofBusinessRuleViolation("새로운 위치가 리스트 개수를 초과합니다")));

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
                Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) result.getLeft();
                assertThat(businessRuleViolation.getErrorCode()).isEqualTo("LIST_MOVE_POLICY_VIOLATION");

                verify(updateBoardListPositionValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
        }

        @Test
        @DisplayName("위치가 변경되지 않는 경우 기존 리스트를 반환해야 한다")
        void updateBoardListPosition_withSamePosition_shouldReturnOriginalLists() {
                // given
                UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                                new ListId(),
                                new UserId(),
                                1);
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList list1 = createValidBoardList(new ListId(), boardId, 0);
                BoardList targetList = createValidBoardList(command.listId(), boardId, 1);
                BoardList list3 = createValidBoardList(new ListId(), boardId, 2);
                List<BoardList> originalLists = List.of(list1, targetList, list3);

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(targetList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                                .thenReturn(originalLists);
                when(boardListMovePolicy.canMoveWithinSameBoard(targetList, command.newPosition()))
                                .thenReturn(Either.right(null));
                when(boardListMovePolicy.hasPositionChanged(targetList, command.newPosition()))
                                .thenReturn(false);

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).hasSize(3);
                assertThat(result.get().get(1).getPosition()).isEqualTo(1);

                verify(updateBoardListPositionValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
                verify(boardListRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("리스트 위치 변경 중 예외 발생 시 InternalError를 반환해야 한다")
        void updateBoardListPosition_withSaveException_shouldReturnInternalError() {
                // given
                UpdateBoardListPositionCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList targetList = createValidBoardList(command.listId(), boardId, 1);
                List<BoardList> existingLists = List.of(
                                createValidBoardList(new ListId(), boardId, 0),
                                targetList,
                                createValidBoardList(new ListId(), boardId, 2));

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(targetList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                                .thenReturn(existingLists);
                when(boardListMovePolicy.canMoveWithinSameBoard(targetList, command.newPosition()))
                                .thenReturn(Either.right(null));
                when(boardListMovePolicy.hasPositionChanged(targetList, command.newPosition()))
                                .thenReturn(true);
                when(boardListRepository.saveAll(any()))
                                .thenThrow(new RuntimeException("데이터베이스 오류"));

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                assertThat(internalError.getErrorCode()).isEqualTo("INTERNAL_ERROR");
                assertThat(internalError.getMessage()).isEqualTo("데이터베이스 오류");

                verify(updateBoardListPositionValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
                verify(boardListRepository).saveAll(any());
        }

        @Test
        @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
        void updateBoardListPosition_withMultipleValidationErrors_shouldReturnAllErrors() {
                // given
                UpdateBoardListPositionCommand command = createValidCommand();
                Failure.FieldViolation violation1 = Failure.FieldViolation.builder()
                                .field("listId")
                                .message("리스트 ID는 필수입니다")
                                .rejectedValue(command.listId())
                                .build();
                Failure.FieldViolation violation2 = Failure.FieldViolation.builder()
                                .field("newPosition")
                                .message("새로운 위치는 0 이상이어야 합니다")
                                .rejectedValue(command.newPosition())
                                .build();
                ValidationResult<UpdateBoardListPositionCommand> invalidResult = ValidationResult
                                .invalid(io.vavr.collection.List.of(violation1, violation2));

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getViolations()).hasSize(2);
                assertThat(inputError.getViolations().get(0).field()).isEqualTo("listId");
                assertThat(inputError.getViolations().get(0).message()).isEqualTo("리스트 ID는 필수입니다");
                assertThat(inputError.getViolations().get(1).field()).isEqualTo("newPosition");
                assertThat(inputError.getViolations().get(1).message()).isEqualTo("새로운 위치는 0 이상이어야 합니다");

                verify(updateBoardListPositionValidator).validate(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("리스트 위치가 올바르게 재정렬되어야 한다")
        void updateBoardListPosition_shouldReorderListsCorrectly() {
                // given
                UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                                new ListId(),
                                new UserId(),
                                0);
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList list1 = createValidBoardList(new ListId(), boardId, 0);
                BoardList targetList = createValidBoardList(command.listId(), boardId, 1);
                BoardList list3 = createValidBoardList(new ListId(), boardId, 2);
                List<BoardList> originalLists = List.of(list1, targetList, list3);

                when(updateBoardListPositionValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(targetList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                                .thenReturn(originalLists);
                when(boardListMovePolicy.canMoveWithinSameBoard(targetList, command.newPosition()))
                                .thenReturn(Either.right(null));
                when(boardListMovePolicy.hasPositionChanged(targetList, command.newPosition()))
                                .thenReturn(true);
                when(boardListRepository.saveAll(any()))
                                .thenAnswer(invocation -> {
                                        List<BoardList> savedLists = invocation.getArgument(0);
                                        return savedLists;
                                });

                // when
                Either<Failure, List<BoardList>> result = updateBoardListPositionService
                                .updateBoardListPosition(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).hasSize(3);
                // saveAll이 호출되었는지 확인 (위치 변경이 발생했음을 의미)
                verify(boardListRepository).saveAll(any());

                verify(updateBoardListPositionValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
        }
}