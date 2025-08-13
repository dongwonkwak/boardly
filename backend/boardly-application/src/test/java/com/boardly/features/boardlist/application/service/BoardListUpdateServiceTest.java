package com.boardly.features.boardlist.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.ActivityType;
import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.application.command.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.command.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.boardlist.domain.config.BoardListPolicyConfig;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.boardlist.infrastructure.policy.BoardListMovePolicy;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.ListColor;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import io.vavr.control.Either;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardListUpdateService 테스트")
class BoardListUpdateServiceTest {

    @Mock
    private BoardListValidator boardListValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardListPolicyConfig boardListPolicyConfig;

    @Mock
    private BoardListMovePolicy boardListMovePolicy;

    @Mock
    private MessageResolver messageResolver;

    @Mock
    private ActivityHelper activityHelper;

    @InjectMocks
    private BoardListUpdateService boardListUpdateService;

    private UserId testUserId;
    private BoardId testBoardId;
    private ListId testListId;
    private Board testBoard;
    private BoardList testBoardList;
    private UpdateBoardListCommand validUpdateCommand;
    private UpdateBoardListPositionCommand validPositionCommand;

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
            .title("기존 리스트 제목")
            .description("기존 리스트 설명")
            .position(1)
            .color(ListColor.defaultColor())
            .boardId(testBoardId)
            .createdAt(now)
            .updatedAt(now)
            .build();

        validUpdateCommand = new UpdateBoardListCommand(
            testListId,
            testUserId,
            "새로운 제목",
            "새로운 설명",
            ListColor.of("#0079BF")
        );

        validPositionCommand = new UpdateBoardListPositionCommand(
            testListId,
            testUserId,
            2
        );
    }

    @Nested
    @DisplayName("updateBoardList 메서드 테스트")
    class UpdateBoardListTest {

        @Test
        @DisplayName("유효한 데이터로 리스트 수정 시 성공해야 한다")
        @SuppressWarnings("unchecked")
        void updateBoardList_withValidData_shouldReturnUpdatedBoardList() {
            ValidationResult<UpdateBoardListCommand> validResult =
                ValidationResult.valid(validUpdateCommand);
            when(
                boardListValidator.validateUpdateBoardList(validUpdateCommand)
            ).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.of(testBoardList)
            );
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
            when(boardListRepository.save(any(BoardList.class))).thenReturn(
                testBoardList
            );

            Either<Failure, BoardList> result =
                boardListUpdateService.updateBoardList(validUpdateCommand);

            assertThat(result.isRight()).isTrue();
            BoardList updatedList = result.get();
            assertThat(updatedList.getTitle()).isEqualTo("새로운 제목");
            assertThat(updatedList.getDescription()).isEqualTo("새로운 설명");
            assertThat(updatedList.getColor()).isEqualTo(
                ListColor.of("#0079BF")
            );

            verify(boardListRepository).save(any(BoardList.class));
            verify(activityHelper).logListActivity(
                eq(ActivityType.LIST_RENAME),
                eq(testUserId),
                any(Map.class),
                eq("테스트 보드"),
                eq(testBoardId),
                eq(testListId)
            );
            verify(activityHelper).logListActivity(
                eq(ActivityType.LIST_CHANGE_COLOR),
                eq(testUserId),
                any(Map.class),
                eq("테스트 보드"),
                eq(testBoardId),
                eq(testListId)
            );
        }

        @Test
        @DisplayName("리스트가 존재하지 않을 때 NotFound를 반환해야 한다")
        void updateBoardList_withNonExistentList_shouldReturnNotFound() {
            when(
                boardListValidator.validateUpdateBoardList(validUpdateCommand)
            ).thenReturn(ValidationResult.valid(validUpdateCommand));
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.empty()
            );
            when(
                messageResolver.getMessage("validation.boardlist.not.found")
            ).thenReturn("리스트를 찾을 수 없습니다");

            Either<Failure, BoardList> result =
                boardListUpdateService.updateBoardList(validUpdateCommand);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
        }

        @Test
        @DisplayName("보드가 존재하지 않을 때 NotFound를 반환해야 한다")
        void updateBoardList_withNonExistentBoard_shouldReturnNotFound() {
            when(
                boardListValidator.validateUpdateBoardList(validUpdateCommand)
            ).thenReturn(ValidationResult.valid(validUpdateCommand));
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.of(testBoardList)
            );
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.empty()
            );
            when(
                messageResolver.getMessage("validation.board.not.found")
            ).thenReturn("보드를 찾을 수 없습니다");

            Either<Failure, BoardList> result =
                boardListUpdateService.updateBoardList(validUpdateCommand);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
        }

        @Test
        @DisplayName("권한이 없을 때 PermissionDenied를 반환해야 한다")
        void updateBoardList_withUnauthorizedUser_shouldReturnPermissionDenied() {
            when(
                boardListValidator.validateUpdateBoardList(validUpdateCommand)
            ).thenReturn(ValidationResult.valid(validUpdateCommand));
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.of(testBoardList)
            );
            Board otherOwnerBoard = Board.builder()
                .boardId(testBoardId)
                .title("보드")
                .description("설명")
                .isArchived(false)
                .ownerId(new UserId("other"))
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(otherOwnerBoard)
            );
            when(
                messageResolver.getMessage(
                    "validation.boardlist.update.access.denied"
                )
            ).thenReturn("리스트 수정 권한이 없습니다");

            Either<Failure, BoardList> result =
                boardListUpdateService.updateBoardList(validUpdateCommand);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(
                Failure.PermissionDenied.class
            );
        }

        @Test
        @DisplayName("제목 길이 초과 시 BusinessRuleViolation을 반환해야 한다")
        void updateBoardList_withTitleTooLong_shouldReturnBusinessRuleViolation() {
            UpdateBoardListCommand longTitleCmd = new UpdateBoardListCommand(
                testListId,
                testUserId,
                "a".repeat(101),
                null,
                null
            );
            when(
                boardListValidator.validateUpdateBoardList(longTitleCmd)
            ).thenReturn(ValidationResult.valid(longTitleCmd));
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.of(testBoardList)
            );
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
            when(
                messageResolver.getMessage(
                    "validation.boardlist.title.length.exceeded",
                    100
                )
            ).thenReturn("리스트 제목은 최대 100자까지 입력할 수 있습니다");

            Either<Failure, BoardList> result =
                boardListUpdateService.updateBoardList(longTitleCmd);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(
                Failure.BusinessRuleViolation.class
            );
        }

        @Test
        @DisplayName("저장 중 예외 발생 시 InternalError를 반환해야 한다")
        void updateBoardList_withSaveException_shouldReturnInternalError() {
            when(
                boardListValidator.validateUpdateBoardList(validUpdateCommand)
            ).thenReturn(ValidationResult.valid(validUpdateCommand));
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.of(testBoardList)
            );
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(boardListPolicyConfig.getMaxTitleLength()).thenReturn(100);
            when(boardListRepository.save(any(BoardList.class))).thenThrow(
                new RuntimeException("SAVE FAILED")
            );
            when(
                messageResolver.getMessage("validation.boardlist.update.error")
            ).thenReturn("리스트 수정 중 오류가 발생했습니다");

            Either<Failure, BoardList> result =
                boardListUpdateService.updateBoardList(validUpdateCommand);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(
                Failure.InternalError.class
            );
        }
    }

    @Nested
    @DisplayName("updateBoardListPosition 메서드 테스트")
    class UpdateBoardListPositionTest {

        @Test
        @DisplayName("유효한 위치 변경 시 성공해야 한다")
        @SuppressWarnings("unchecked")
        void updateBoardListPosition_withValidPosition_shouldReturnUpdatedLists() {
            BoardList targetList = BoardList.builder()
                .listId(testListId)
                .title("리스트2")
                .description("설명")
                .position(1)
                .color(ListColor.defaultColor())
                .boardId(testBoardId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            List<BoardList> allLists = List.of(
                createBoardList("리스트1", 0),
                targetList,
                createBoardList("리스트3", 2)
            );

            ValidationResult<UpdateBoardListPositionCommand> validResult =
                ValidationResult.valid(validPositionCommand);
            when(
                boardListValidator.validateUpdateBoardListPosition(
                    validPositionCommand
                )
            ).thenReturn(validResult);
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.of(targetList)
            );
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(
                boardListRepository.findByBoardIdOrderByPosition(testBoardId)
            ).thenReturn(allLists);
            when(
                boardListMovePolicy.canMoveWithinSameBoard(any(), eq(2))
            ).thenReturn(Either.right(null));
            when(
                boardListMovePolicy.hasPositionChanged(any(), eq(2))
            ).thenReturn(true);
            when(boardListRepository.saveAll(anyList())).thenReturn(allLists);

            Either<Failure, List<BoardList>> result =
                boardListUpdateService.updateBoardListPosition(
                    validPositionCommand
                );

            assertThat(result.isRight()).isTrue();
            List<BoardList> updatedLists = result.get();
            assertThat(updatedLists).hasSize(3);
            verify(boardListRepository).saveAll(anyList());
            verify(activityHelper).logListActivity(
                eq(ActivityType.LIST_MOVE),
                eq(testUserId),
                any(Map.class),
                eq("테스트 보드"),
                eq(testBoardId),
                eq(testListId)
            );
        }

        private BoardList createBoardList(String title, int position) {
            return BoardList.builder()
                .listId(new ListId())
                .title(title)
                .description("설명")
                .position(position)
                .color(ListColor.defaultColor())
                .boardId(testBoardId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        }

        @Test
        @DisplayName("리스트가 존재하지 않을 때 NotFound")
        void updateBoardListPosition_withNonExistentList_shouldReturnNotFound() {
            when(
                boardListValidator.validateUpdateBoardListPosition(
                    validPositionCommand
                )
            ).thenReturn(ValidationResult.valid(validPositionCommand));
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.empty()
            );
            when(
                messageResolver.getMessage("validation.boardlist.not.found")
            ).thenReturn("리스트를 찾을 수 없습니다");

            Either<Failure, List<BoardList>> result =
                boardListUpdateService.updateBoardListPosition(
                    validPositionCommand
                );

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
        }

        @Test
        @DisplayName("권한 없음 PermissionDenied")
        void updateBoardListPosition_withUnauthorizedUser_shouldReturnPermissionDenied() {
            BoardList target = testBoardList;
            when(
                boardListValidator.validateUpdateBoardListPosition(
                    validPositionCommand
                )
            ).thenReturn(ValidationResult.valid(validPositionCommand));
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.of(target)
            );
            Board otherBoard = Board.builder()
                .boardId(testBoardId)
                .title("보드")
                .description("")
                .isArchived(false)
                .ownerId(new UserId("other"))
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(otherBoard)
            );
            when(
                messageResolver.getMessage(
                    "validation.boardlist.update.access.denied"
                )
            ).thenReturn("리스트 수정 권한이 없습니다");

            Either<Failure, List<BoardList>> result =
                boardListUpdateService.updateBoardListPosition(
                    validPositionCommand
                );

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(
                Failure.PermissionDenied.class
            );
        }

        @Test
        @DisplayName("이동 정책 위반 BusinessRuleViolation")
        void updateBoardListPosition_withPolicyViolation_shouldReturnBusinessRuleViolation() {
            BoardList target = testBoardList;
            when(
                boardListValidator.validateUpdateBoardListPosition(
                    validPositionCommand
                )
            ).thenReturn(ValidationResult.valid(validPositionCommand));
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.of(target)
            );
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            // Arrange only interactions that are actually executed before failure
            when(
                boardListMovePolicy.canMoveWithinSameBoard(any(), eq(2))
            ).thenReturn(
                Either.left(Failure.ofBusinessRuleViolation("invalid move"))
            );
            when(
                messageResolver.getMessage(
                    "validation.boardlist.move.policy.violation"
                )
            ).thenReturn("정책 위반");

            Either<Failure, List<BoardList>> result =
                boardListUpdateService.updateBoardListPosition(
                    validPositionCommand
                );

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(
                Failure.BusinessRuleViolation.class
            );
        }

        @Test
        @DisplayName("저장 중 예외 발생 InternalError")
        void updateBoardListPosition_withSaveException_shouldReturnInternalError() {
            // 준비: 3개의 리스트가 있는 보드, 가운데 리스트를 끝으로 이동 시 예외 발생
            BoardList l1 = createBoardList("리스트1", 0);
            BoardList l2 = createBoardList("리스트2", 1);
            BoardList l3 = createBoardList("리스트3", 2);

            when(
                boardListValidator.validateUpdateBoardListPosition(
                    validPositionCommand
                )
            ).thenReturn(ValidationResult.valid(validPositionCommand));
            when(boardListRepository.findById(testListId)).thenReturn(
                Optional.of(l2)
            );
            when(boardRepository.findById(testBoardId)).thenReturn(
                Optional.of(testBoard)
            );
            when(
                boardListRepository.findByBoardIdOrderByPosition(testBoardId)
            ).thenReturn(List.of(l1, l2, l3));
            when(boardListMovePolicy.canMoveWithinSameBoard(l2, 2)).thenReturn(
                Either.right(null)
            );
            when(boardListMovePolicy.hasPositionChanged(l2, 2)).thenReturn(
                true
            );
            when(boardListRepository.saveAll(anyList())).thenThrow(
                new RuntimeException("BATCH SAVE ERROR")
            );
            when(
                messageResolver.getMessage(
                    "validation.boardlist.position.update.error"
                )
            ).thenReturn("리스트 위치 변경 중 오류가 발생했습니다");

            Either<Failure, List<BoardList>> result =
                boardListUpdateService.updateBoardListPosition(
                    validPositionCommand
                );

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(
                Failure.InternalError.class
            );
        }
    }
}
