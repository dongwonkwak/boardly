package com.boardly.features.boardlist.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.boardlist.infrastructure.config.BoardListPolicyConfigImpl;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * BoardList Policy 통합 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BoardList Policy 통합 테스트")
class BoardListPolicyIntegrationTest {

    @Mock
    private BoardListRepository boardListRepository;

    private BoardListPolicyConfigImpl policyConfig;
    private BoardListCreationPolicy creationPolicy;
    private BoardListMovePolicy movePolicy;

    @BeforeEach
    void setUp() {
        policyConfig = new BoardListPolicyConfigImpl();
        creationPolicy = new BoardListCreationPolicy(boardListRepository, policyConfig);
        movePolicy = new BoardListMovePolicy(boardListRepository);
    }

    @Test
    @DisplayName("정책 설정이 변경될 때 모든 정책이 일관되게 동작해야 함")
    void shouldWorkConsistentlyWhenPolicyConfigChanges() {
        // given
        BoardId boardId = new BoardId("board-1");
        policyConfig.setMaxListsPerBoard(15);
        policyConfig.setWarningThreshold(12);
        policyConfig.setRecommendedListsPerBoard(8);

        // when
        when(boardListRepository.countByBoardId(boardId)).thenReturn(10L);

        // then
        // CreationPolicy 테스트
        Either<Failure, Void> creationResult = creationPolicy.canCreateBoardList(boardId);
        assertThat(creationResult.isRight()).isTrue();

        // 상태 확인
        BoardListCreationPolicy.ListCountStatus status = creationPolicy.getStatus(boardId);
        assertThat(status).isEqualTo(BoardListCreationPolicy.ListCountStatus.ABOVE_RECOMMENDED);
    }

    @Test
    @DisplayName("리스트 생성과 이동이 연속적으로 동작해야 함")
    void shouldWorkSequentiallyForCreationAndMove() {
        // given
        BoardId boardId = new BoardId("board-1");
        ListId listId = new ListId("list-1");
        BoardList boardList = createBoardList(listId, boardId, 0);

        policyConfig.setMaxListsPerBoard(10);
        when(boardListRepository.countByBoardId(boardId)).thenReturn(5L);

        // when - 생성 가능 확인
        Either<Failure, Void> creationResult = creationPolicy.canCreateBoardList(boardId);

        // then - 생성 가능
        assertThat(creationResult.isRight()).isTrue();

        // when - 이동 가능 확인
        Either<Failure, Void> moveResult = movePolicy.canMoveWithinSameBoard(boardList, 3);

        // then - 이동 가능
        assertThat(moveResult.isRight()).isTrue();
    }

    @Test
    @DisplayName("최대 개수에 도달했을 때 생성은 불가하지만 이동은 가능해야 함")
    void shouldAllowMoveButNotCreationWhenAtMaxLimit() {
        // given
        BoardId boardId = new BoardId("board-1");
        ListId listId = new ListId("list-1");
        BoardList boardList = createBoardList(listId, boardId, 2);

        policyConfig.setMaxListsPerBoard(5);
        when(boardListRepository.countByBoardId(boardId)).thenReturn(5L);

        // when - 생성 시도
        Either<Failure, Void> creationResult = creationPolicy.canCreateBoardList(boardId);

        // then - 생성 불가
        assertThat(creationResult.isLeft()).isTrue();

        // when - 이동 시도
        Either<Failure, Void> moveResult = movePolicy.canMoveWithinSameBoard(boardList, 4);

        // then - 이동 가능
        assertThat(moveResult.isRight()).isTrue();
    }

    @Test
    @DisplayName("경고 임계값 초과 시 적절한 상태를 반환해야 함")
    void shouldReturnWarningStatusWhenOverThreshold() {
        // given
        BoardId boardId = new BoardId("board-1");
        policyConfig.setMaxListsPerBoard(20);
        policyConfig.setWarningThreshold(15);
        policyConfig.setRecommendedListsPerBoard(10);

        when(boardListRepository.countByBoardId(boardId)).thenReturn(17L);

        // when
        BoardListCreationPolicy.ListCountStatus status = creationPolicy.getStatus(boardId);
        boolean shouldShowWarning = creationPolicy.shouldShowWarning(boardId);
        boolean exceedsRecommended = creationPolicy.exceedsRecommended(boardId);

        // then
        assertThat(status).isEqualTo(BoardListCreationPolicy.ListCountStatus.WARNING);
        assertThat(shouldShowWarning).isTrue();
        assertThat(exceedsRecommended).isTrue();
    }

    @Test
    @DisplayName("사용 가능한 슬롯 계산이 정확해야 함")
    void shouldCalculateAvailableSlotsCorrectly() {
        // given
        BoardId boardId = new BoardId("board-1");
        policyConfig.setMaxListsPerBoard(20);
        when(boardListRepository.countByBoardId(boardId)).thenReturn(15L);

        // when
        long availableSlots = creationPolicy.getAvailableListSlots(boardId);

        // then
        assertThat(availableSlots).isEqualTo(5L);
    }

    @Test
    @DisplayName("위치 변경 감지가 정확해야 함")
    void shouldDetectPositionChangesCorrectly() {
        // given
        BoardId boardId = new BoardId("board-1");
        ListId listId = new ListId("list-1");
        BoardList boardList = createBoardList(listId, boardId, 2);

        // when & then
        assertThat(movePolicy.hasPositionChanged(boardList, 2)).isFalse();
        assertThat(movePolicy.hasPositionChanged(boardList, 3)).isTrue();
        assertThat(movePolicy.hasPositionChanged(boardList, 1)).isTrue();
    }

    @Test
    @DisplayName("정책 설정값들이 일관되게 반환되어야 함")
    void shouldReturnConsistentPolicyValues() {
        // given
        policyConfig.setMaxListsPerBoard(25);
        policyConfig.setRecommendedListsPerBoard(12);
        policyConfig.setWarningThreshold(18);

        // when & then
        assertThat(creationPolicy.getMaxListsPerBoard()).isEqualTo(25);
        assertThat(creationPolicy.getRecommendedListsPerBoard()).isEqualTo(12);
        assertThat(creationPolicy.getWarningThreshold()).isEqualTo(18);
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