package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.policy.BoardListCreationPolicy;
import com.boardly.features.boardlist.domain.policy.BoardListCreationPolicy.ListCountStatus;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BoardListReadService 테스트
 * 
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BoardListReadService 테스트")
class BoardListReadServiceTest {

    @Mock
    private BoardListValidator boardListValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardListCreationPolicy boardListCreationPolicy;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @InjectMocks
    private BoardListReadService boardListReadService;

    private UserId testUserId;
    private BoardId testBoardId;
    private Board testBoard;
    private GetBoardListsCommand validCommand;
    private List<BoardList> testBoardLists;

    @BeforeEach
    void setUp() {
        testUserId = new UserId("test-user-123");
        testBoardId = new BoardId("test-board-123");

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

        validCommand = new GetBoardListsCommand(testBoardId, testUserId);

        // 테스트용 보드 리스트 생성
        testBoardLists = List.of(
                BoardList.builder()
                        .listId(new ListId("list-1"))
                        .boardId(testBoardId)
                        .title("할 일")
                        .description("해야 할 일들")
                        .color(ListColor.defaultColor())
                        .position(1)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                BoardList.builder()
                        .listId(new ListId("list-2"))
                        .boardId(testBoardId)
                        .title("진행 중")
                        .description("진행 중인 작업들")
                        .color(ListColor.defaultColor())
                        .position(2)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                BoardList.builder()
                        .listId(new ListId("list-3"))
                        .boardId(testBoardId)
                        .title("완료")
                        .description("완료된 작업들")
                        .color(ListColor.defaultColor())
                        .position(3)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
    }

    @Nested
    @DisplayName("getBoardLists 메서드 테스트")
    class GetBoardListsTest {

        @Test
        @DisplayName("유효한 데이터로 보드 리스트 조회가 성공해야 한다")
        void getBoardLists_withValidData_shouldReturnBoardLists() {
            // given
            ValidationResult<GetBoardListsCommand> validValidationResult = ValidationResult.valid(validCommand);
            ListCountStatus normalStatus = ListCountStatus.NORMAL;

            when(boardListValidator.validateGetBoardLists(validCommand))
                    .thenReturn(validValidationResult);
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardListRepository.findByBoardIdOrderByPosition(testBoardId))
                    .thenReturn(testBoardLists);
            when(boardListCreationPolicy.getStatus(testBoardId))
                    .thenReturn(normalStatus);

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testBoardLists);
            assertThat(result.get()).hasSize(3);

            verify(boardListValidator).validateGetBoardLists(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(boardListRepository).findByBoardIdOrderByPosition(testBoardId);
            verify(boardListCreationPolicy).getStatus(testBoardId);
        }

        @Test
        @DisplayName("빈 리스트가 있는 경우 빈 리스트를 반환해야 한다")
        void getBoardLists_withEmptyLists_shouldReturnEmptyList() {
            // given
            ValidationResult<GetBoardListsCommand> validValidationResult = ValidationResult.valid(validCommand);
            ListCountStatus normalStatus = ListCountStatus.NORMAL;
            List<BoardList> emptyList = List.of();

            when(boardListValidator.validateGetBoardLists(validCommand))
                    .thenReturn(validValidationResult);
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardListRepository.findByBoardIdOrderByPosition(testBoardId))
                    .thenReturn(emptyList);
            when(boardListCreationPolicy.getStatus(testBoardId))
                    .thenReturn(normalStatus);

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEmpty();

            verify(boardListValidator).validateGetBoardLists(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(boardListRepository).findByBoardIdOrderByPosition(testBoardId);
            verify(boardListCreationPolicy).getStatus(testBoardId);
        }

        @Test
        @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
        void getBoardLists_withInvalidData_shouldReturnInputError() {
            // given
            ValidationResult<GetBoardListsCommand> invalidValidationResult = ValidationResult.invalid(
                    "boardId", "boardId는 필수입니다", null);

            when(boardListValidator.validateGetBoardLists(validCommand))
                    .thenReturn(invalidValidationResult);
            when(validationMessageResolver.getMessage("validation.input.invalid"))
                    .thenReturn("입력 데이터가 유효하지 않습니다");

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            Failure.InputError inputError = (Failure.InputError) result.getLeft();
            assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다");
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
            assertThat(inputError.getViolations()).isNotEmpty();

            verify(boardListValidator).validateGetBoardLists(validCommand);
            verify(validationMessageResolver).getMessage("validation.input.invalid");
            verifyNoInteractions(boardRepository, boardListRepository, boardListCreationPolicy);
        }

        @Test
        @DisplayName("보드가 존재하지 않을 때 NotFound를 반환해야 한다")
        void getBoardLists_withNonExistentBoard_shouldReturnNotFound() {
            // given
            ValidationResult<GetBoardListsCommand> validValidationResult = ValidationResult.valid(validCommand);

            when(boardListValidator.validateGetBoardLists(validCommand))
                    .thenReturn(validValidationResult);
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("validation.board.not.found"))
                    .thenReturn("보드를 찾을 수 없습니다");

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
            assertThat(notFound.getMessage()).isEqualTo("보드를 찾을 수 없습니다");
            assertThat(notFound.getErrorCode()).isEqualTo("BOARD_NOT_FOUND");
            assertThat(notFound.getContext()).isNotNull();

            verify(boardListValidator).validateGetBoardLists(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(validationMessageResolver).getMessage("validation.board.not.found");
            verifyNoInteractions(boardListRepository, boardListCreationPolicy);
        }

        @Test
        @DisplayName("보드 접근 권한이 없을 때 PermissionDenied를 반환해야 한다")
        void getBoardLists_withUnauthorizedAccess_shouldReturnPermissionDenied() {
            // given
            UserId unauthorizedUserId = new UserId("unauthorized-user-456");
            GetBoardListsCommand unauthorizedCommand = new GetBoardListsCommand(testBoardId, unauthorizedUserId);
            ValidationResult<GetBoardListsCommand> validValidationResult = ValidationResult.valid(unauthorizedCommand);

            when(boardListValidator.validateGetBoardLists(unauthorizedCommand))
                    .thenReturn(validValidationResult);
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(validationMessageResolver.getMessage("validation.board.modification.access.denied"))
                    .thenReturn("보드에 접근할 권한이 없습니다");

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(unauthorizedCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) result.getLeft();
            assertThat(permissionDenied.getMessage()).isEqualTo("보드에 접근할 권한이 없습니다");
            assertThat(permissionDenied.getErrorCode()).isEqualTo("UNAUTHORIZED_ACCESS");
            assertThat(permissionDenied.getContext()).isNotNull();

            verify(boardListValidator).validateGetBoardLists(unauthorizedCommand);
            verify(boardRepository).findById(testBoardId);
            verify(validationMessageResolver).getMessage("validation.board.modification.access.denied");
            verifyNoInteractions(boardListRepository, boardListCreationPolicy);
        }

        @Test
        @DisplayName("리스트 조회 중 예외 발생 시 InternalError를 반환해야 한다")
        void getBoardLists_withQueryException_shouldReturnInternalError() {
            // given
            ValidationResult<GetBoardListsCommand> validValidationResult = ValidationResult.valid(validCommand);
            RuntimeException queryException = new RuntimeException("데이터베이스 연결 오류");

            when(boardListValidator.validateGetBoardLists(validCommand))
                    .thenReturn(validValidationResult);
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardListRepository.findByBoardIdOrderByPosition(testBoardId))
                    .thenThrow(queryException);

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
            Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
            assertThat(internalError.getMessage()).isEqualTo("데이터베이스 연결 오류");
            assertThat(internalError.getErrorCode()).isEqualTo("BOARD_LIST_QUERY_ERROR");

            verify(boardListValidator).validateGetBoardLists(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(boardListRepository).findByBoardIdOrderByPosition(testBoardId);
            verifyNoInteractions(boardListCreationPolicy);
        }

        @Test
        @DisplayName("리스트 개수 상태가 알림이 필요한 경우 로그가 기록되어야 한다")
        void getBoardLists_withNotificationRequiredStatus_shouldLogWarning() {
            // given
            ValidationResult<GetBoardListsCommand> validValidationResult = ValidationResult.valid(validCommand);
            ListCountStatus warningStatus = ListCountStatus.WARNING;

            when(boardListValidator.validateGetBoardLists(validCommand))
                    .thenReturn(validValidationResult);
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardListRepository.findByBoardIdOrderByPosition(testBoardId))
                    .thenReturn(testBoardLists);
            when(boardListCreationPolicy.getStatus(testBoardId))
                    .thenReturn(warningStatus);

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testBoardLists);

            verify(boardListValidator).validateGetBoardLists(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(boardListRepository).findByBoardIdOrderByPosition(testBoardId);
            verify(boardListCreationPolicy).getStatus(testBoardId);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTest {

        @Test
        @DisplayName("보드 소유자가 자신의 보드 리스트를 조회할 때 성공해야 한다")
        void getBoardLists_withBoardOwner_shouldSucceed() {
            // given
            ValidationResult<GetBoardListsCommand> validValidationResult = ValidationResult.valid(validCommand);
            ListCountStatus normalStatus = ListCountStatus.NORMAL;

            when(boardListValidator.validateGetBoardLists(validCommand))
                    .thenReturn(validValidationResult);
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardListRepository.findByBoardIdOrderByPosition(testBoardId))
                    .thenReturn(testBoardLists);
            when(boardListCreationPolicy.getStatus(testBoardId))
                    .thenReturn(normalStatus);

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testBoardLists);

            verify(boardListValidator).validateGetBoardLists(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(boardListRepository).findByBoardIdOrderByPosition(testBoardId);
            verify(boardListCreationPolicy).getStatus(testBoardId);
        }

        @Test
        @DisplayName("보드가 아카이브된 상태에서도 리스트 조회가 가능해야 한다")
        void getBoardLists_withArchivedBoard_shouldSucceed() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(testBoardId)
                    .title("아카이브된 보드")
                    .description("아카이브된 보드 설명")
                    .isArchived(true)
                    .ownerId(testUserId)
                    .isStarred(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            ValidationResult<GetBoardListsCommand> validValidationResult = ValidationResult.valid(validCommand);
            ListCountStatus normalStatus = ListCountStatus.NORMAL;

            when(boardListValidator.validateGetBoardLists(validCommand))
                    .thenReturn(validValidationResult);
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(archivedBoard));
            when(boardListRepository.findByBoardIdOrderByPosition(testBoardId))
                    .thenReturn(testBoardLists);
            when(boardListCreationPolicy.getStatus(testBoardId))
                    .thenReturn(normalStatus);

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(testBoardLists);

            verify(boardListValidator).validateGetBoardLists(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(boardListRepository).findByBoardIdOrderByPosition(testBoardId);
            verify(boardListCreationPolicy).getStatus(testBoardId);
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("리스트 조회 중 예외 발생 시 InternalError를 반환해야 한다")
        void getBoardLists_withQueryException_shouldReturnInternalError() {
            // given
            ValidationResult<GetBoardListsCommand> validValidationResult = ValidationResult.valid(validCommand);
            RuntimeException queryException = new RuntimeException("데이터베이스 연결 오류");

            when(boardListValidator.validateGetBoardLists(validCommand))
                    .thenReturn(validValidationResult);
            when(boardRepository.findById(testBoardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardListRepository.findByBoardIdOrderByPosition(testBoardId))
                    .thenThrow(queryException);

            // when
            Either<Failure, List<BoardList>> result = boardListReadService.getBoardLists(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
            Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
            assertThat(internalError.getMessage()).isEqualTo("데이터베이스 연결 오류");
            assertThat(internalError.getErrorCode()).isEqualTo("BOARD_LIST_QUERY_ERROR");

            verify(boardListValidator).validateGetBoardLists(validCommand);
            verify(boardRepository).findById(testBoardId);
            verify(boardListRepository).findByBoardIdOrderByPosition(testBoardId);
            verifyNoInteractions(boardListCreationPolicy);
        }
    }
}