package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.validation.UpdateBoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateBoardListServiceTest {

        private UpdateBoardListService updateBoardListService;

        @Mock
        private UpdateBoardListValidator updateBoardListValidator;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @BeforeEach
        void setUp() {
                updateBoardListService = new UpdateBoardListService(
                                updateBoardListValidator,
                                boardRepository,
                                boardListRepository,
                                validationMessageResolver);
        }

        private UpdateBoardListCommand createValidCommand() {
                return new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "수정된 리스트",
                                "수정된 리스트 설명",
                                ListColor.of("#FF6B6B"));
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

        private BoardList createValidBoardList(ListId listId, BoardId boardId) {
                return BoardList.create(
                                "기존 리스트",
                                "기존 리스트 설명",
                                0,
                                ListColor.of("#0079BF"),
                                boardId);
        }

        @Test
        @DisplayName("유효한 정보로 리스트 수정이 성공해야 한다")
        void updateBoardList_withValidData_shouldReturnUpdatedBoardList() {
                // given
                UpdateBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList existingList = createValidBoardList(command.listId(), boardId);
                BoardList updatedList = createValidBoardList(command.listId(), boardId);
                updatedList.updateTitle(command.title());
                updatedList.updateDescription(command.description());
                updatedList.updateColor(command.color());

                when(updateBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(existingList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.save(any(BoardList.class)))
                                .thenReturn(updatedList);

                // when
                Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(command.title());
                assertThat(result.get().getDescription()).isEqualTo(command.description());
                assertThat(result.get().getColor()).isEqualTo(command.color());

                verify(updateBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).save(any(BoardList.class));
        }

        @Test
        @DisplayName("제목만 수정하는 경우 성공해야 한다")
        void updateBoardList_withTitleOnly_shouldReturnUpdatedBoardList() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "새로운 제목",
                                null,
                                null);
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList existingList = createValidBoardList(command.listId(), boardId);
                BoardList updatedList = createValidBoardList(command.listId(), boardId);
                updatedList.updateTitle(command.title());

                when(updateBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(existingList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.save(any(BoardList.class)))
                                .thenReturn(updatedList);

                // when
                Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(command.title());
                assertThat(result.get().getDescription()).isEqualTo(existingList.getDescription());
                assertThat(result.get().getColor()).isEqualTo(existingList.getColor());

                verify(updateBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).save(any(BoardList.class));
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void updateBoardList_withInvalidData_shouldReturnInputError() {
                // given
                UpdateBoardListCommand command = createValidCommand();
                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("title")
                                .message("제목은 필수입니다")
                                .rejectedValue(command.title())
                                .build();
                ValidationResult<UpdateBoardListCommand> invalidResult = ValidationResult.invalid(violation);

                when(updateBoardListValidator.validate(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getViolations()).hasSize(1);
                assertThat(inputError.getViolations().get(0).field()).isEqualTo("title");
                assertThat(inputError.getViolations().get(0).message()).isEqualTo("제목은 필수입니다");

                verify(updateBoardListValidator).validate(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("리스트를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
        void updateBoardList_withNonExistentList_shouldReturnNotFoundFailure() {
                // given
                UpdateBoardListCommand command = createValidCommand();

                when(updateBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.empty());
                when(validationMessageResolver.getMessage("error.service.card.move.list_not_found"))
                                .thenReturn("리스트를 찾을 수 없습니다");

                // when
                Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                assertThat(notFound.getErrorCode()).isEqualTo("LIST_NOT_FOUND");
                assertThat(notFound.getContext()).isInstanceOf(Map.class);
                Map<String, Object> context = (Map<String, Object>) notFound.getContext();
                assertThat(context).containsKey("listId");

                verify(updateBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(validationMessageResolver).getMessage("error.service.card.move.list_not_found");
        }

        @Test
        @DisplayName("보드를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
        void updateBoardList_withNonExistentBoard_shouldReturnNotFoundFailure() {
                // given
                UpdateBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                BoardList existingList = createValidBoardList(command.listId(), boardId);

                when(updateBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(existingList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.empty());
                when(validationMessageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다");

                // when
                Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                assertThat(notFound.getErrorCode()).isEqualTo("BOARD_NOT_FOUND");
                assertThat(notFound.getContext()).isInstanceOf(Map.class);
                Map<String, Object> context = (Map<String, Object>) notFound.getContext();
                assertThat(context).containsKey("boardId");

                verify(updateBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(validationMessageResolver).getMessage("validation.board.not.found");
        }

        @Test
        @DisplayName("보드 소유자가 아닌 경우 PermissionDenied 오류를 반환해야 한다")
        void updateBoardList_withUnauthorizedUser_shouldReturnPermissionDeniedFailure() {
                // given
                UpdateBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                UserId differentUserId = new UserId();
                Board board = createValidBoard(boardId, differentUserId);
                BoardList existingList = createValidBoardList(command.listId(), boardId);

                when(updateBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(existingList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(validationMessageResolver.getMessage("validation.board.modification.access.denied"))
                                .thenReturn("보드에 대한 접근 권한이 없습니다");

                // when
                Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) result.getLeft();
                assertThat(permissionDenied.getErrorCode()).isEqualTo("UNAUTHORIZED_ACCESS");
                assertThat(permissionDenied.getContext()).isInstanceOf(Map.class);
                Map<String, Object> context = (Map<String, Object>) permissionDenied.getContext();
                assertThat(context).containsKey("listId");
                assertThat(context).containsKey("userId");

                verify(updateBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(validationMessageResolver).getMessage("validation.board.modification.access.denied");
        }

        @Test
        @DisplayName("리스트 수정 중 예외 발생 시 InternalError를 반환해야 한다")
        void updateBoardList_withSaveException_shouldReturnInternalError() {
                // given
                UpdateBoardListCommand command = createValidCommand();
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList existingList = createValidBoardList(command.listId(), boardId);

                when(updateBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(existingList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.save(any(BoardList.class)))
                                .thenThrow(new RuntimeException("데이터베이스 오류"));

                // when
                Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                assertThat(internalError.getErrorCode()).isEqualTo("BOARD_LIST_UPDATE_ERROR");
                assertThat(internalError.getMessage()).isEqualTo("데이터베이스 오류");

                verify(updateBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).save(any(BoardList.class));
        }

        @Test
        @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
        void updateBoardList_withMultipleValidationErrors_shouldReturnAllErrors() {
                // given
                UpdateBoardListCommand command = createValidCommand();
                Failure.FieldViolation violation1 = Failure.FieldViolation.builder()
                                .field("title")
                                .message("제목은 필수입니다")
                                .rejectedValue(command.title())
                                .build();
                Failure.FieldViolation violation2 = Failure.FieldViolation.builder()
                                .field("color")
                                .message("색상 형식이 올바르지 않습니다")
                                .rejectedValue(command.color())
                                .build();
                ValidationResult<UpdateBoardListCommand> invalidResult = ValidationResult
                                .invalid(io.vavr.collection.List.of(violation1, violation2));

                when(updateBoardListValidator.validate(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getViolations()).hasSize(2);
                assertThat(inputError.getViolations().get(0).field()).isEqualTo("title");
                assertThat(inputError.getViolations().get(0).message()).isEqualTo("제목은 필수입니다");
                assertThat(inputError.getViolations().get(1).field()).isEqualTo("color");
                assertThat(inputError.getViolations().get(1).message()).isEqualTo("색상 형식이 올바르지 않습니다");

                verify(updateBoardListValidator).validate(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("설명을 null로 설정하는 경우 성공해야 한다")
        void updateBoardList_withNullDescription_shouldReturnUpdatedBoardList() {
                // given
                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(),
                                new UserId(),
                                "새로운 제목",
                                null,
                                ListColor.of("#FF6B6B"));
                BoardId boardId = new BoardId();
                Board board = createValidBoard(boardId, command.userId());
                BoardList existingList = createValidBoardList(command.listId(), boardId);
                BoardList updatedList = createValidBoardList(command.listId(), boardId);
                updatedList.updateTitle(command.title());
                updatedList.updateDescription(null);
                updatedList.updateColor(command.color());

                when(updateBoardListValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardListRepository.findById(command.listId()))
                                .thenReturn(Optional.of(existingList));
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.save(any(BoardList.class)))
                                .thenReturn(updatedList);

                // when
                Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(command.title());
                assertThat(result.get().getDescription()).isEqualTo(null);
                assertThat(result.get().getColor()).isEqualTo(command.color());

                verify(updateBoardListValidator).validate(command);
                verify(boardListRepository).findById(command.listId());
                verify(boardRepository).findById(boardId);
                verify(boardListRepository).save(any(BoardList.class));
        }
}