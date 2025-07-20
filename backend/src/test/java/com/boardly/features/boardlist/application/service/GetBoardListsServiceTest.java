package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.boardlist.application.validation.GetBoardListsValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBoardListsServiceTest {

        private GetBoardListsService getBoardListsService;

        @Mock
        private GetBoardListsValidator getBoardListsValidator;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @BeforeEach
        void setUp() {
                getBoardListsService = new GetBoardListsService(
                                getBoardListsValidator,
                                boardRepository,
                                boardListRepository,
                                validationMessageResolver);
        }

        private GetBoardListsCommand createValidCommand() {
                return new GetBoardListsCommand(
                                new BoardId(),
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

        private BoardList createValidBoardList(BoardId boardId, int position) {
                return BoardList.create(
                                "테스트 리스트 " + position,
                                "테스트 리스트 설명 " + position,
                                position,
                                ListColor.of("#0079BF"),
                                boardId);
        }

        @Test
        @DisplayName("유효한 정보로 보드 리스트 조회가 성공해야 한다")
        void getBoardLists_withValidData_shouldReturnBoardLists() {
                // given
                GetBoardListsCommand command = createValidCommand();
                Board board = createValidBoard(command.boardId(), command.userId());
                List<BoardList> expectedLists = List.of(
                                createValidBoardList(command.boardId(), 0),
                                createValidBoardList(command.boardId(), 1));

                when(getBoardListsValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.findByBoardIdOrderByPosition(command.boardId()))
                                .thenReturn(expectedLists);

                // when
                Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).hasSize(2);
                assertThat(result.get().get(0).getPosition()).isEqualTo(0);
                assertThat(result.get().get(1).getPosition()).isEqualTo(1);

                verify(getBoardListsValidator).validate(command);
                verify(boardRepository).findById(command.boardId());
                verify(boardListRepository).findByBoardIdOrderByPosition(command.boardId());
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void getBoardLists_withInvalidData_shouldReturnInputError() {
                // given
                GetBoardListsCommand command = createValidCommand();
                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("boardId")
                                .message("보드 ID는 필수입니다")
                                .rejectedValue(command.boardId())
                                .build();
                ValidationResult<GetBoardListsCommand> invalidResult = ValidationResult.invalid(violation);

                when(getBoardListsValidator.validate(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getViolations()).hasSize(1);
                assertThat(inputError.getViolations().get(0).field()).isEqualTo("boardId");
                assertThat(inputError.getViolations().get(0).message()).isEqualTo("보드 ID는 필수입니다");

                verify(getBoardListsValidator).validate(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("보드를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
        void getBoardLists_withNonExistentBoard_shouldReturnNotFoundFailure() {
                // given
                GetBoardListsCommand command = createValidCommand();

                when(getBoardListsValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.empty());
                when(validationMessageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다");

                // when
                Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                assertThat(notFound.getErrorCode()).isEqualTo("BOARD_NOT_FOUND");
                assertThat(notFound.getContext()).isInstanceOf(Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> context = (Map<String, Object>) notFound.getContext();
                assertThat(context).containsKey("boardId");

                verify(getBoardListsValidator).validate(command);
                verify(boardRepository).findById(command.boardId());
                verify(validationMessageResolver).getMessage("validation.board.not.found");
        }

        @Test
        @DisplayName("보드 소유자가 아닌 경우 PermissionDenied 오류를 반환해야 한다")
        void getBoardLists_withUnauthorizedUser_shouldReturnPermissionDeniedFailure() {
                // given
                GetBoardListsCommand command = createValidCommand();
                UserId differentUserId = new UserId();
                Board board = createValidBoard(command.boardId(), differentUserId);

                when(getBoardListsValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(validationMessageResolver.getMessage("validation.board.modification.access.denied"))
                                .thenReturn("보드에 대한 접근 권한이 없습니다");

                // when
                Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) result.getLeft();
                assertThat(permissionDenied.getErrorCode()).isEqualTo("UNAUTHORIZED_ACCESS");
                assertThat(permissionDenied.getContext()).isInstanceOf(Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> context = (Map<String, Object>) permissionDenied.getContext();
                assertThat(context).containsKey("boardId");
                assertThat(context).containsKey("userId");

                verify(getBoardListsValidator).validate(command);
                verify(boardRepository).findById(command.boardId());
                verify(validationMessageResolver).getMessage("validation.board.modification.access.denied");
        }

        @Test
        @DisplayName("리스트가 없는 경우 빈 리스트를 반환해야 한다")
        void getBoardLists_withNoLists_shouldReturnEmptyList() {
                // given
                GetBoardListsCommand command = createValidCommand();
                Board board = createValidBoard(command.boardId(), command.userId());

                when(getBoardListsValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.findByBoardIdOrderByPosition(command.boardId()))
                                .thenReturn(List.of());

                // when
                Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).isEmpty();

                verify(getBoardListsValidator).validate(command);
                verify(boardRepository).findById(command.boardId());
                verify(boardListRepository).findByBoardIdOrderByPosition(command.boardId());
        }

        @Test
        @DisplayName("리스트 조회 중 예외 발생 시 InternalError를 반환해야 한다")
        void getBoardLists_withQueryException_shouldReturnInternalError() {
                // given
                GetBoardListsCommand command = createValidCommand();
                Board board = createValidBoard(command.boardId(), command.userId());

                when(getBoardListsValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.findByBoardIdOrderByPosition(command.boardId()))
                                .thenThrow(new RuntimeException("데이터베이스 오류"));

                // when
                Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                assertThat(internalError.getErrorCode()).isEqualTo("BOARD_LIST_QUERY_ERROR");
                assertThat(internalError.getMessage()).isEqualTo("데이터베이스 오류");

                verify(getBoardListsValidator).validate(command);
                verify(boardRepository).findById(command.boardId());
                verify(boardListRepository).findByBoardIdOrderByPosition(command.boardId());
        }

        @Test
        @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
        void getBoardLists_withMultipleValidationErrors_shouldReturnAllErrors() {
                // given
                GetBoardListsCommand command = createValidCommand();
                Failure.FieldViolation violation1 = Failure.FieldViolation.builder()
                                .field("boardId")
                                .message("보드 ID는 필수입니다")
                                .rejectedValue(command.boardId())
                                .build();
                Failure.FieldViolation violation2 = Failure.FieldViolation.builder()
                                .field("userId")
                                .message("사용자 ID는 필수입니다")
                                .rejectedValue(command.userId())
                                .build();
                ValidationResult<GetBoardListsCommand> invalidResult = ValidationResult
                                .invalid(io.vavr.collection.List.of(violation1, violation2));

                when(getBoardListsValidator.validate(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getViolations()).hasSize(2);
                assertThat(inputError.getViolations().get(0).field()).isEqualTo("boardId");
                assertThat(inputError.getViolations().get(0).message()).isEqualTo("보드 ID는 필수입니다");
                assertThat(inputError.getViolations().get(1).field()).isEqualTo("userId");
                assertThat(inputError.getViolations().get(1).message()).isEqualTo("사용자 ID는 필수입니다");

                verify(getBoardListsValidator).validate(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("리스트가 위치 순으로 정렬되어 반환되어야 한다")
        void getBoardLists_shouldReturnListsInPositionOrder() {
                // given
                GetBoardListsCommand command = createValidCommand();
                Board board = createValidBoard(command.boardId(), command.userId());
                List<BoardList> expectedLists = List.of(
                                createValidBoardList(command.boardId(), 2),
                                createValidBoardList(command.boardId(), 0),
                                createValidBoardList(command.boardId(), 1));

                when(getBoardListsValidator.validate(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(boardListRepository.findByBoardIdOrderByPosition(command.boardId()))
                                .thenReturn(expectedLists);

                // when
                Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).hasSize(3);
                assertThat(result.get().get(0).getPosition()).isEqualTo(2);
                assertThat(result.get().get(1).getPosition()).isEqualTo(0);
                assertThat(result.get().get(2).getPosition()).isEqualTo(1);

                verify(getBoardListsValidator).validate(command);
                verify(boardRepository).findById(command.boardId());
                verify(boardListRepository).findByBoardIdOrderByPosition(command.boardId());
        }
}