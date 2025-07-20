package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.application.validation.CreateBoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.policy.ListLimitPolicy;
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
class CreateBoardListServiceTest {

        private CreateBoardListService createBoardListService;

        @Mock
        private CreateBoardListValidator createBoardListValidator;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private BoardListRepository boardListRepository;

        @Mock
        private ListLimitPolicy listLimitPolicy;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @BeforeEach
        void setUp() {
                createBoardListService = new CreateBoardListService(
                                createBoardListValidator,
                                boardRepository,
                                boardListRepository,
                                listLimitPolicy,
                                validationMessageResolver);
        }

        private CreateBoardListCommand createValidCommand() {
                return new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "테스트 리스트",
                                "테스트 리스트 설명",
                                ListColor.of("#0079BF"));
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

        private BoardList createValidBoardList(CreateBoardListCommand command, int position) {
                return BoardList.create(
                                command.title(),
                                command.description(),
                                position,
                                command.color(),
                                command.boardId());
        }

        @Test
        @DisplayName("유효한 정보로 리스트 생성이 성공해야 한다")
        void createBoardList_withValidData_shouldReturnBoardList() {
                // given
                CreateBoardListCommand command = createValidCommand();
                Board board = createValidBoard(command.boardId(), command.userId());
                BoardList savedList = createValidBoardList(command, 0);

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(listLimitPolicy.canCreateList(0))
                                .thenReturn(true);
                when(boardListRepository.countByBoardId(command.boardId()))
                                .thenReturn(0L);
                when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                                .thenReturn(Optional.empty());
                when(boardListRepository.save(any(BoardList.class)))
                                .thenReturn(savedList);

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(command.title());
                assertThat(result.get().getDescription()).isEqualTo(command.description());
                assertThat(result.get().getColor()).isEqualTo(command.color());
                assertThat(result.get().getBoardId()).isEqualTo(command.boardId());
                assertThat(result.get().getPosition()).isEqualTo(0);

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(boardRepository).findById(command.boardId());
                verify(listLimitPolicy).canCreateList(0);
                verify(boardListRepository).countByBoardId(command.boardId());
                verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
                verify(boardListRepository).save(any(BoardList.class));
        }

        @Test
        @DisplayName("설명이 null인 경우에도 리스트 생성이 성공해야 한다")
        void createBoardList_withNullDescription_shouldReturnBoardList() {
                // given
                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(),
                                new UserId(),
                                "테스트 리스트",
                                null,
                                ListColor.of("#0079BF"));
                Board board = createValidBoard(command.boardId(), command.userId());
                BoardList savedList = createValidBoardList(command, 0);

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(listLimitPolicy.canCreateList(0))
                                .thenReturn(true);
                when(boardListRepository.countByBoardId(command.boardId()))
                                .thenReturn(0L);
                when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                                .thenReturn(Optional.empty());
                when(boardListRepository.save(any(BoardList.class)))
                                .thenReturn(savedList);

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getTitle()).isEqualTo(command.title());
                assertThat(result.get().getDescription()).isEqualTo(null);
                assertThat(result.get().getColor()).isEqualTo(command.color());

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(boardRepository).findById(command.boardId());
                verify(listLimitPolicy).canCreateList(0);
                verify(boardListRepository).countByBoardId(command.boardId());
                verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
                verify(boardListRepository).save(any(BoardList.class));
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void createBoardList_withInvalidData_shouldReturnInputError() {
                // given
                CreateBoardListCommand command = createValidCommand();
                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("title")
                                .message("제목은 필수입니다")
                                .rejectedValue(command.title())
                                .build();
                ValidationResult<CreateBoardListCommand> invalidResult = ValidationResult.invalid(violation);

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                Failure.InputError inputError = (Failure.InputError) result.getLeft();
                assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(inputError.getViolations()).hasSize(1);
                assertThat(inputError.getViolations().get(0).field()).isEqualTo("title");
                assertThat(inputError.getViolations().get(0).message()).isEqualTo("제목은 필수입니다");

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("보드를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
        void createBoardList_withNonExistentBoard_shouldReturnNotFoundFailure() {
                // given
                CreateBoardListCommand command = createValidCommand();

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.empty());
                when(validationMessageResolver.getMessage("validation.board.not.found"))
                                .thenReturn("보드를 찾을 수 없습니다");

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                assertThat(notFound.getErrorCode()).isEqualTo("BOARD_NOT_FOUND");
                assertThat(notFound.getContext()).isInstanceOf(Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> context = (Map<String, Object>) notFound.getContext();
                assertThat(context).containsKey("boardId");

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(boardRepository).findById(command.boardId());
                verify(validationMessageResolver).getMessage("validation.board.not.found");
        }

        @Test
        @DisplayName("보드 소유자가 아닌 경우 PermissionDenied 오류를 반환해야 한다")
        void createBoardList_withUnauthorizedUser_shouldReturnPermissionDeniedFailure() {
                // given
                CreateBoardListCommand command = createValidCommand();
                UserId differentUserId = new UserId();
                Board board = createValidBoard(command.boardId(), differentUserId);

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(validationMessageResolver.getMessage("validation.board.modification.access.denied"))
                                .thenReturn("보드에 대한 접근 권한이 없습니다");

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

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

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(boardRepository).findById(command.boardId());
                verify(validationMessageResolver).getMessage("validation.board.modification.access.denied");
        }

        @Test
        @DisplayName("리스트 생성 한도 초과 시 BusinessRuleViolation 오류를 반환해야 한다")
        void createBoardList_withLimitExceeded_shouldReturnBusinessRuleViolationFailure() {
                // given
                CreateBoardListCommand command = createValidCommand();
                Board board = createValidBoard(command.boardId(), command.userId());

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(listLimitPolicy.canCreateList(10))
                                .thenReturn(false);
                when(boardListRepository.countByBoardId(command.boardId()))
                                .thenReturn(10L);
                when(validationMessageResolver.getMessage("error.business.list.limit.exceeded"))
                                .thenReturn("리스트 생성 한도를 초과했습니다");

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.BusinessRuleViolation.class);
                Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) result.getLeft();
                assertThat(businessRuleViolation.getErrorCode()).isEqualTo("LIST_LIMIT_EXCEEDED");
                assertThat(businessRuleViolation.getContext()).isInstanceOf(Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> context = (Map<String, Object>) businessRuleViolation.getContext();
                assertThat(context).containsKey("boardId");
                assertThat(context).containsKey("currentCount");

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(boardRepository).findById(command.boardId());
                verify(listLimitPolicy).canCreateList(10);
                verify(boardListRepository).countByBoardId(command.boardId());
                verify(validationMessageResolver).getMessage("error.business.list.limit.exceeded");
        }

        @Test
        @DisplayName("기존 리스트가 있는 경우 다음 위치에 생성해야 한다")
        void createBoardList_withExistingLists_shouldCreateAtNextPosition() {
                // given
                CreateBoardListCommand command = createValidCommand();
                Board board = createValidBoard(command.boardId(), command.userId());
                BoardList savedList = createValidBoardList(command, 5);

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(listLimitPolicy.canCreateList(5))
                                .thenReturn(true);
                when(boardListRepository.countByBoardId(command.boardId()))
                                .thenReturn(5L);
                when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                                .thenReturn(Optional.of(4));
                when(boardListRepository.save(any(BoardList.class)))
                                .thenReturn(savedList);

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get().getPosition()).isEqualTo(5);

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(boardRepository).findById(command.boardId());
                verify(listLimitPolicy).canCreateList(5);
                verify(boardListRepository).countByBoardId(command.boardId());
                verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
                verify(boardListRepository).save(any(BoardList.class));
        }

        @Test
        @DisplayName("리스트 저장 중 예외 발생 시 InternalError를 반환해야 한다")
        void createBoardList_withSaveException_shouldReturnInternalError() {
                // given
                CreateBoardListCommand command = createValidCommand();
                Board board = createValidBoard(command.boardId(), command.userId());

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(listLimitPolicy.canCreateList(0))
                                .thenReturn(true);
                when(boardListRepository.countByBoardId(command.boardId()))
                                .thenReturn(0L);
                when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                                .thenReturn(Optional.empty());
                when(boardListRepository.save(any(BoardList.class)))
                                .thenThrow(new RuntimeException("데이터베이스 오류"));

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                assertThat(internalError.getErrorCode()).isEqualTo("LIST_CREATION_ERROR");
                assertThat(internalError.getMessage()).isEqualTo("데이터베이스 오류");

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(boardRepository).findById(command.boardId());
                verify(listLimitPolicy).canCreateList(0);
                verify(boardListRepository).countByBoardId(command.boardId());
                verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
                verify(boardListRepository).save(any(BoardList.class));
        }

        @Test
        @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
        void createBoardList_withMultipleValidationErrors_shouldReturnAllErrors() {
                // given
                CreateBoardListCommand command = createValidCommand();
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
                ValidationResult<CreateBoardListCommand> invalidResult = ValidationResult
                                .invalid(io.vavr.collection.List.of(violation1, violation2));

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(invalidResult);
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력 데이터가 유효하지 않습니다");

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

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

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("생성된 리스트는 올바른 초기 상태를 가져야 한다")
        void createBoardList_shouldCreateListWithCorrectInitialState() {
                // given
                CreateBoardListCommand command = createValidCommand();
                Board board = createValidBoard(command.boardId(), command.userId());
                BoardList savedList = createValidBoardList(command, 0);

                when(createBoardListValidator.validateCreateBoardList(command))
                                .thenReturn(ValidationResult.valid(command));
                when(boardRepository.findById(command.boardId()))
                                .thenReturn(Optional.of(board));
                when(listLimitPolicy.canCreateList(0))
                                .thenReturn(true);
                when(boardListRepository.countByBoardId(command.boardId()))
                                .thenReturn(0L);
                when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                                .thenReturn(Optional.empty());
                when(boardListRepository.save(any(BoardList.class)))
                                .thenReturn(savedList);

                // when
                Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

                // then
                assertThat(result.isRight()).isTrue();
                BoardList createdList = result.get();
                assertThat(createdList.getTitle()).isEqualTo(command.title());
                assertThat(createdList.getDescription()).isEqualTo(command.description());
                assertThat(createdList.getColor()).isEqualTo(command.color());
                assertThat(createdList.getBoardId()).isEqualTo(command.boardId());
                assertThat(createdList.getPosition()).isEqualTo(0);
                assertThat(createdList.getListId()).isNotNull();

                verify(createBoardListValidator).validateCreateBoardList(command);
                verify(boardRepository).findById(command.boardId());
                verify(listLimitPolicy).canCreateList(0);
                verify(boardListRepository).countByBoardId(command.boardId());
                verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
                verify(boardListRepository).save(any(BoardList.class));
        }
}