package com.boardly.features.boardlist.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * BoardListMovePolicy 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BoardListMovePolicy 테스트")
class BoardListMovePolicyTest {

    @Mock
    private BoardListRepository boardListRepository;

    private BoardListMovePolicy movePolicy;

    @BeforeEach
    void setUp() {
        movePolicy = new BoardListMovePolicy(boardListRepository);
    }

    @Nested
    @DisplayName("canMoveWithinSameBoard 메서드 테스트")
    class CanMoveWithinSameBoardTest {

        @Test
        @DisplayName("유효한 위치로 이동할 때 성공해야 함")
        void shouldAllowMoveToValidPosition() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(5L);

            // when
            Either<Failure, Void> result = movePolicy.canMoveWithinSameBoard(boardList, 3);

            // then
            assertThat(result.isRight()).isTrue();
        }

        @Test
        @DisplayName("음수 위치로 이동할 때 실패해야 함")
        void shouldNotAllowMoveToNegativePosition() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);

            // when
            Either<Failure, Void> result = movePolicy.canMoveWithinSameBoard(boardList, -1);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("POSITION_INVALID");
        }

        @Test
        @DisplayName("리스트 개수를 초과하는 위치로 이동할 때 실패해야 함")
        void shouldNotAllowMoveToPositionBeyondListCount() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(5L);

            // when
            Either<Failure, Void> result = movePolicy.canMoveWithinSameBoard(boardList, 6);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("POSITION_OUT_OF_RANGE");
        }

        @Test
        @DisplayName("리스트 개수와 같은 위치로 이동할 때 성공해야 함")
        void shouldAllowMoveToPositionEqualToListCount() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(5L);

            // when
            Either<Failure, Void> result = movePolicy.canMoveWithinSameBoard(boardList, 5);

            // then
            assertThat(result.isRight()).isTrue();
        }

        @Test
        @DisplayName("0번 위치로 이동할 때 성공해야 함")
        void shouldAllowMoveToPositionZero() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(5L);

            // when
            Either<Failure, Void> result = movePolicy.canMoveWithinSameBoard(boardList, 0);

            // then
            assertThat(result.isRight()).isTrue();
        }
    }

    @Nested
    @DisplayName("isValidMove 메서드 테스트")
    class IsValidMoveTest {

        @Test
        @DisplayName("유효한 이동일 때 true를 반환해야 함")
        void shouldReturnTrueForValidMove() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(5L);

            // when
            boolean isValid = movePolicy.isValidMove(boardList, 3);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("유효하지 않은 이동일 때 false를 반환해야 함")
        void shouldReturnFalseForInvalidMove() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(5L);

            // when
            boolean isValid = movePolicy.isValidMove(boardList, 6);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("음수 위치로의 이동일 때 false를 반환해야 함")
        void shouldReturnFalseForNegativePosition() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);

            // when
            boolean isValid = movePolicy.isValidMove(boardList, -1);

            // then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPositionChanged 메서드 테스트")
    class HasPositionChangedTest {

        @Test
        @DisplayName("위치가 변경되었을 때 true를 반환해야 함")
        void shouldReturnTrueWhenPositionChanged() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);

            // when
            boolean hasChanged = movePolicy.hasPositionChanged(boardList, 5);

            // then
            assertThat(hasChanged).isTrue();
        }

        @Test
        @DisplayName("위치가 변경되지 않았을 때 false를 반환해야 함")
        void shouldReturnFalseWhenPositionNotChanged() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 2);

            // when
            boolean hasChanged = movePolicy.hasPositionChanged(boardList, 2);

            // then
            assertThat(hasChanged).isFalse();
        }

        @Test
        @DisplayName("같은 위치로 이동할 때 false를 반환해야 함")
        void shouldReturnFalseWhenMovingToSamePosition() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 3);

            // when
            boolean hasChanged = movePolicy.hasPositionChanged(boardList, 3);

            // then
            assertThat(hasChanged).isFalse();
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTest {

        @Test
        @DisplayName("빈 보드에서 첫 번째 위치로 이동할 때 성공해야 함")
        void shouldAllowMoveToFirstPositionInEmptyBoard() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 0);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(0L);

            // when
            Either<Failure, Void> result = movePolicy.canMoveWithinSameBoard(boardList, 0);

            // then
            assertThat(result.isRight()).isTrue();
        }

        @Test
        @DisplayName("빈 보드에서 0번 위치를 초과하는 위치로 이동할 때 실패해야 함")
        void shouldNotAllowMoveBeyondZeroInEmptyBoard() {
            // given
            BoardId boardId = new BoardId("board-1");
            ListId listId = new ListId("list-1");
            BoardList boardList = createBoardList(listId, boardId, 0);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(0L);

            // when
            Either<Failure, Void> result = movePolicy.canMoveWithinSameBoard(boardList, 1);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("POSITION_OUT_OF_RANGE");
        }
    }

    /**
     * 테스트용 BoardList 객체를 생성합니다.
     */
    private BoardList createBoardList(ListId listId, BoardId boardId, int position) {
        return BoardList.builder()
                .listId(listId)
                .boardId(boardId)
                .title("Test List")
                .position(position)
                .color(ListColor.defaultColor())
                .build();
    }
}