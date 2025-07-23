package com.boardly.features.boardlist.application.service;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
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
 * BoardListDeleteService 테스트
 * 
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BoardListDeleteService 테스트")
class BoardListDeleteServiceTest {

        @Mock
        private BoardListValidator boardListValidator;

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

        @Mock
        private ActivityHelper activityHelper;

        @InjectMocks
        private BoardListDeleteService boardListDeleteService;

        private UserId testUserId;
        private BoardId testBoardId;
        private ListId testListId;
        private Board testBoard;
        private BoardList testBoardList;
        private DeleteBoardListCommand validCommand;

        @BeforeEach
        void setUp() {
                testUserId = new UserId("test-user-123");
                testBoardId = new BoardId("test-board-123");
                testListId = new ListId("test-list-123");

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

                testBoardList = BoardList.builder()
                                .listId(testListId)
                                .boardId(testBoardId)
                                .title("테스트 리스트")
                                .description("테스트 리스트 설명")
                                .position(1)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                validCommand = new DeleteBoardListCommand(testListId, testUserId);
        }

        @Nested
        @DisplayName("deleteBoardList 메서드 테스트")
        class DeleteBoardListTest {

                @Test
                @DisplayName("유효한 데이터로 리스트 삭제가 성공해야 한다")
                void deleteBoardList_withValidData_shouldReturnSuccess() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        when(boardListRepository.findByBoardIdAndPositionGreaterThan(testBoardId, 1))
                                        .thenReturn(List.of());
                        when(cardRepository.countByListId(testListId)).thenReturn(5L);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(boardListRepository).deleteById(testListId);
                        verify(cardRepository).deleteByListId(testListId);
                        verify(activityHelper).logListActivity(
                                        eq(ActivityType.LIST_DELETE),
                                        eq(testUserId),
                                        any(Map.class),
                                        eq(testBoardId),
                                        eq(testListId));
                }

                @Test
                @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
                void deleteBoardList_withInvalidData_shouldReturnInputError() {
                        // given
                        List<Failure.FieldViolation> validationErrors = List.of(
                                        Failure.FieldViolation.builder()
                                                        .field("listId")
                                                        .message("리스트 ID가 유효하지 않습니다")
                                                        .rejectedValue(null)
                                                        .build());
                        ValidationResult<DeleteBoardListCommand> invalidValidation = ValidationResult
                                        .invalid(io.vavr.collection.List.ofAll(validationErrors));
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(invalidValidation);
                        when(validationMessageResolver.getMessage("validation.input.invalid"))
                                        .thenReturn("입력 데이터가 유효하지 않습니다");

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) result.getLeft();
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다");
                        verify(boardListRepository, never()).deleteById(any());
                        verify(activityHelper, never()).logListActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("리스트가 존재하지 않을 때 NotFound를 반환해야 한다")
                void deleteBoardList_withNonExistentList_shouldReturnNotFound() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                        verify(boardListRepository, never()).deleteById(any());
                        verify(activityHelper, never()).logListActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("보드가 존재하지 않을 때 NotFound를 반환해야 한다")
                void deleteBoardList_withNonExistentBoard_shouldReturnNotFound() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
                        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                        verify(boardListRepository, never()).deleteById(any());
                        verify(activityHelper, never()).logListActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("권한 확인 실패 시 해당 에러를 반환해야 한다")
                void deleteBoardList_withPermissionCheckFailure_shouldReturnPermissionError() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        Failure permissionFailure = Failure.ofPermissionDenied("권한이 없습니다");
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.left(permissionFailure));

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isEqualTo(permissionFailure);
                        verify(boardListRepository, never()).deleteById(any());
                        verify(activityHelper, never()).logListActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("쓰기 권한이 없을 때 Forbidden을 반환해야 한다")
                void deleteBoardList_withNoWritePermission_shouldReturnForbidden() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(false));
                        when(validationMessageResolver.getMessage("validation.boardlist.delete.access.denied"))
                                        .thenReturn("리스트 삭제 권한이 없습니다");

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
                        Failure.PermissionDenied forbidden = (Failure.PermissionDenied) result.getLeft();
                        assertThat(forbidden.getMessage()).isEqualTo("접근이 거부되었습니다.");
                        assertThat(forbidden.getErrorCode()).isEqualTo("리스트 삭제 권한이 없습니다");
                        verify(boardListRepository, never()).deleteById(any());
                        verify(activityHelper, never()).logListActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("카드 삭제 실패 시 해당 에러를 반환해야 한다")
                void deleteBoardList_withCardDeleteFailure_shouldReturnCardDeleteError() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        Failure cardDeleteFailure = Failure.ofInternalServerError("카드 삭제 실패");
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.left(cardDeleteFailure));

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isEqualTo(cardDeleteFailure);
                        verify(boardListRepository, never()).deleteById(any());
                        verify(activityHelper, never()).logListActivity(any(), any(), any(), any(), any());
                }

                @Test
                @DisplayName("리스트 삭제 중 예외 발생 시 InternalError를 반환해야 한다")
                void deleteBoardList_withDeleteException_shouldReturnInternalError() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        doThrow(new RuntimeException("데이터베이스 오류")).when(boardListRepository).deleteById(testListId);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                        Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
                        assertThat(internalError.getMessage()).isEqualTo("데이터베이스 오류");
                        verify(activityHelper, never()).logListActivity(any(), any(), any(), any(), any());
                }
        }

        @Nested
        @DisplayName("리스트 재정렬 테스트")
        class ReorderListsTest {

                @Test
                @DisplayName("삭제된 리스트 이후의 리스트들이 올바르게 재정렬되어야 한다")
                void deleteBoardList_withRemainingLists_shouldReorderPositions() {
                        // given
                        BoardList list1 = BoardList.builder()
                                        .listId(new ListId("list-1"))
                                        .boardId(testBoardId)
                                        .title("리스트 1")
                                        .position(1)
                                        .build();
                        BoardList list2 = BoardList.builder()
                                        .listId(new ListId("list-2"))
                                        .boardId(testBoardId)
                                        .title("리스트 2")
                                        .position(2)
                                        .build();
                        BoardList list3 = BoardList.builder()
                                        .listId(new ListId("list-3"))
                                        .boardId(testBoardId)
                                        .title("리스트 3")
                                        .position(3)
                                        .build();

                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(list1));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        when(boardListRepository.findByBoardIdAndPositionGreaterThan(testBoardId, 1))
                                        .thenReturn(List.of(list2, list3));
                        when(cardRepository.countByListId(testListId)).thenReturn(0L);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(boardListRepository).saveAll(argThat(lists -> {
                                List<BoardList> listList = (List<BoardList>) lists;
                                return listList.size() == 2 &&
                                                listList.get(0).getPosition() == 1 &&
                                                listList.get(1).getPosition() == 2;
                        }));
                }

                @Test
                @DisplayName("삭제된 리스트가 마지막 위치일 때 재정렬이 필요하지 않아야 한다")
                void deleteBoardList_withLastPositionList_shouldNotReorder() {
                        // given
                        BoardList lastList = BoardList.builder()
                                        .listId(testListId)
                                        .boardId(testBoardId)
                                        .title("마지막 리스트")
                                        .position(3)
                                        .build();

                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(lastList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        when(boardListRepository.findByBoardIdAndPositionGreaterThan(testBoardId, 3))
                                        .thenReturn(List.of());
                        when(cardRepository.countByListId(testListId)).thenReturn(0L);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(boardListRepository, never()).saveAll(any());
                }

                @Test
                @DisplayName("재정렬 중 예외가 발생해도 전체 삭제는 성공해야 한다")
                void deleteBoardList_withReorderException_shouldStillSucceed() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        when(boardListRepository.findByBoardIdAndPositionGreaterThan(testBoardId, 1))
                                        .thenThrow(new RuntimeException("재정렬 오류"));
                        when(cardRepository.countByListId(testListId)).thenReturn(0L);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(boardListRepository).deleteById(testListId);
                }

                @Test
                @DisplayName("재정렬 저장 중 예외가 발생해도 전체 삭제는 성공해야 한다")
                void deleteBoardList_withReorderSaveException_shouldStillSucceed() {
                        // given
                        BoardList remainingList = BoardList.builder()
                                        .listId(new ListId("list-2"))
                                        .boardId(testBoardId)
                                        .title("남은 리스트")
                                        .position(2)
                                        .build();

                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        when(boardListRepository.findByBoardIdAndPositionGreaterThan(testBoardId, 1))
                                        .thenReturn(List.of(remainingList));
                        doThrow(new RuntimeException("저장 오류")).when(boardListRepository).saveAll(any());
                        when(cardRepository.countByListId(testListId)).thenReturn(0L);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(boardListRepository).deleteById(testListId);
                }
        }

        @Nested
        @DisplayName("경계값 테스트")
        class BoundaryValueTest {

                @Test
                @DisplayName("첫 번째 위치의 리스트 삭제 시 재정렬이 올바르게 되어야 한다")
                void deleteBoardList_withFirstPositionList_shouldReorderCorrectly() {
                        // given
                        BoardList firstList = BoardList.builder()
                                        .listId(testListId)
                                        .boardId(testBoardId)
                                        .title("첫 번째 리스트")
                                        .position(0)
                                        .build();
                        BoardList secondList = BoardList.builder()
                                        .listId(new ListId("list-2"))
                                        .boardId(testBoardId)
                                        .title("두 번째 리스트")
                                        .position(1)
                                        .build();

                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(firstList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        when(boardListRepository.findByBoardIdAndPositionGreaterThan(testBoardId, 0))
                                        .thenReturn(List.of(secondList));
                        when(cardRepository.countByListId(testListId)).thenReturn(0L);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(boardListRepository).saveAll(argThat(lists -> {
                                List<BoardList> listList = (List<BoardList>) lists;
                                return listList.size() == 1 && listList.get(0).getPosition() == 0;
                        }));
                }

                @Test
                @DisplayName("유일한 리스트 삭제 시 재정렬이 필요하지 않아야 한다")
                void deleteBoardList_withOnlyOneList_shouldNotReorder() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        when(boardListRepository.findByBoardIdAndPositionGreaterThan(testBoardId, 1))
                                        .thenReturn(List.of());
                        when(cardRepository.countByListId(testListId)).thenReturn(0L);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(boardListRepository, never()).saveAll(any());
                }

                @Test
                @DisplayName("카드가 많은 리스트 삭제 시에도 정상적으로 처리되어야 한다")
                void deleteBoardList_withManyCards_shouldHandleCorrectly() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        when(boardListRepository.findByBoardIdAndPositionGreaterThan(testBoardId, 1))
                                        .thenReturn(List.of());
                        when(cardRepository.countByListId(testListId)).thenReturn(1000L);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(activityHelper).logListActivity(
                                        eq(ActivityType.LIST_DELETE),
                                        eq(testUserId),
                                        argThat(payload -> {
                                                Map<String, Object> map = (Map<String, Object>) payload;
                                                return map.get("cardCount").equals(1000L);
                                        }),
                                        eq(testBoardId),
                                        eq(testListId));
                }
        }

        @Nested
        @DisplayName("활동 로그 테스트")
        class ActivityLogTest {

                @Test
                @DisplayName("활동 로그에 올바른 정보가 포함되어야 한다")
                void deleteBoardList_shouldLogActivityWithCorrectInfo() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
                        when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(testBoard));
                        when(boardPermissionService.canWriteBoard(testBoardId, testUserId))
                                        .thenReturn(Either.right(true));
                        when(cardRepository.deleteByListId(testListId)).thenReturn(Either.right(null));
                        when(boardListRepository.findByBoardIdAndPositionGreaterThan(testBoardId, 1))
                                        .thenReturn(List.of());
                        when(cardRepository.countByListId(testListId)).thenReturn(5L);

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(activityHelper).logListActivity(
                                        eq(ActivityType.LIST_DELETE),
                                        eq(testUserId),
                                        argThat(payload -> {
                                                Map<String, Object> map = (Map<String, Object>) payload;
                                                return map.get("listName").equals("테스트 리스트") &&
                                                                map.get("listId").equals(testListId.getId()) &&
                                                                map.get("boardName").equals("테스트 보드") &&
                                                                map.get("cardCount").equals(5L);
                                        }),
                                        eq(testBoardId),
                                        eq(testListId));
                }

                @Test
                @DisplayName("삭제 실패 시 활동 로그가 기록되지 않아야 한다")
                void deleteBoardList_whenDeleteFails_shouldNotLogActivity() {
                        // given
                        ValidationResult<DeleteBoardListCommand> validValidation = ValidationResult.valid(validCommand);
                        when(boardListValidator.validateDeleteBoardList(validCommand)).thenReturn(validValidation);
                        when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = boardListDeleteService.deleteBoardList(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        verify(activityHelper, never()).logListActivity(any(), any(), any(), any(), any());
                }
        }
}