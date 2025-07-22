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
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * BoardListCreationPolicy 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BoardListCreationPolicy 테스트")
class BoardListCreationPolicyTest {

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardListPolicyConfig policyConfig;

    private BoardListCreationPolicy creationPolicy;

    @BeforeEach
    void setUp() {
        creationPolicy = new BoardListCreationPolicy(boardListRepository, policyConfig);
    }

    @Nested
    @DisplayName("canCreateBoardList 메서드 테스트")
    class CanCreateBoardListTest {

        @Test
        @DisplayName("리스트 개수가 제한 미만일 때 생성 가능해야 함")
        void shouldAllowCreationWhenUnderLimit() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(15L);

            // when
            Either<Failure, Void> result = creationPolicy.canCreateBoardList(boardId);

            // then
            assertThat(result.isRight()).isTrue();
        }

        @Test
        @DisplayName("리스트 개수가 제한에 도달했을 때 생성 불가해야 함")
        void shouldNotAllowCreationWhenAtLimit() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(20L);

            // when
            Either<Failure, Void> result = creationPolicy.canCreateBoardList(boardId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("보드당 최대 20개의 리스트만 생성할 수 있습니다");
        }

        @Test
        @DisplayName("리스트 개수가 제한을 초과했을 때 생성 불가해야 함")
        void shouldNotAllowCreationWhenOverLimit() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(25L);

            // when
            Either<Failure, Void> result = creationPolicy.canCreateBoardList(boardId);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().getMessage()).contains("보드당 최대 20개의 리스트만 생성할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("getStatus 메서드 테스트")
    class GetStatusTest {

        @Test
        @DisplayName("정상 범위일 때 NORMAL 상태를 반환해야 함")
        void shouldReturnNormalWhenUnderRecommended() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(policyConfig.getWarningThreshold()).thenReturn(15);
            when(policyConfig.getRecommendedListsPerBoard()).thenReturn(10);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(5L);

            // when
            BoardListCreationPolicy.ListCountStatus status = creationPolicy.getStatus(boardId);

            // then
            assertThat(status).isEqualTo(BoardListCreationPolicy.ListCountStatus.NORMAL);
        }

        @Test
        @DisplayName("권장 개수 초과일 때 ABOVE_RECOMMENDED 상태를 반환해야 함")
        void shouldReturnAboveRecommendedWhenOverRecommended() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(policyConfig.getWarningThreshold()).thenReturn(15);
            when(policyConfig.getRecommendedListsPerBoard()).thenReturn(10);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(12L);

            // when
            BoardListCreationPolicy.ListCountStatus status = creationPolicy.getStatus(boardId);

            // then
            assertThat(status).isEqualTo(BoardListCreationPolicy.ListCountStatus.ABOVE_RECOMMENDED);
        }

        @Test
        @DisplayName("경고 임계값 초과일 때 WARNING 상태를 반환해야 함")
        void shouldReturnWarningWhenOverWarningThreshold() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(policyConfig.getWarningThreshold()).thenReturn(15);
            when(policyConfig.getRecommendedListsPerBoard()).thenReturn(10);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(17L);

            // when
            BoardListCreationPolicy.ListCountStatus status = creationPolicy.getStatus(boardId);

            // then
            assertThat(status).isEqualTo(BoardListCreationPolicy.ListCountStatus.WARNING);
        }

        @Test
        @DisplayName("최대 개수 도달일 때 LIMIT_REACHED 상태를 반환해야 함")
        void shouldReturnLimitReachedWhenAtMaxLimit() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(policyConfig.getWarningThreshold()).thenReturn(15);
            when(policyConfig.getRecommendedListsPerBoard()).thenReturn(10);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(20L);

            // when
            BoardListCreationPolicy.ListCountStatus status = creationPolicy.getStatus(boardId);

            // then
            assertThat(status).isEqualTo(BoardListCreationPolicy.ListCountStatus.LIMIT_REACHED);
        }
    }

    @Nested
    @DisplayName("getAvailableListSlots 메서드 테스트")
    class GetAvailableListSlotsTest {

        @Test
        @DisplayName("사용 가능한 슬롯 개수를 올바르게 계산해야 함")
        void shouldCalculateAvailableSlotsCorrectly() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(15L);

            // when
            long availableSlots = creationPolicy.getAvailableListSlots(boardId);

            // then
            assertThat(availableSlots).isEqualTo(5L);
        }

        @Test
        @DisplayName("최대 개수에 도달했을 때 0을 반환해야 함")
        void shouldReturnZeroWhenAtMaxLimit() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(20L);

            // when
            long availableSlots = creationPolicy.getAvailableListSlots(boardId);

            // then
            assertThat(availableSlots).isEqualTo(0L);
        }

        @Test
        @DisplayName("최대 개수를 초과했을 때 0을 반환해야 함")
        void shouldReturnZeroWhenOverMaxLimit() {
            // given
            BoardId boardId = new BoardId("board-1");
            when(policyConfig.getMaxListsPerBoard()).thenReturn(20);
            when(boardListRepository.countByBoardId(boardId)).thenReturn(25L);

            // when
            long availableSlots = creationPolicy.getAvailableListSlots(boardId);

            // then
            assertThat(availableSlots).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("정책 설정값 반환 메서드 테스트")
    class PolicyConfigValueTest {

        @Test
        @DisplayName("정책 설정값들을 올바르게 반환해야 함")
        void shouldReturnPolicyConfigValues() {
            // given
            when(policyConfig.getMaxListsPerBoard()).thenReturn(25);
            when(policyConfig.getRecommendedListsPerBoard()).thenReturn(12);
            when(policyConfig.getWarningThreshold()).thenReturn(18);

            // when & then
            assertThat(creationPolicy.getMaxListsPerBoard()).isEqualTo(25);
            assertThat(creationPolicy.getRecommendedListsPerBoard()).isEqualTo(12);
            assertThat(creationPolicy.getWarningThreshold()).isEqualTo(18);
        }
    }

    @Nested
    @DisplayName("ListCountStatus 열거형 테스트")
    class ListCountStatusTest {

        @Test
        @DisplayName("각 상태의 속성들이 올바르게 설정되어 있어야 함")
        void shouldHaveCorrectProperties() {
            // when & then
            BoardListCreationPolicy.ListCountStatus normal = BoardListCreationPolicy.ListCountStatus.NORMAL;
            assertThat(normal.getDisplayName()).isEqualTo("정상");
            assertThat(normal.getMessage()).isEqualTo("리스트 개수가 적절합니다.");
            assertThat(normal.canCreateList()).isTrue();
            assertThat(normal.requiresNotification()).isFalse();

            BoardListCreationPolicy.ListCountStatus aboveRecommended = BoardListCreationPolicy.ListCountStatus.ABOVE_RECOMMENDED;
            assertThat(aboveRecommended.getDisplayName()).isEqualTo("권장 초과");
            assertThat(aboveRecommended.canCreateList()).isTrue();
            assertThat(aboveRecommended.requiresNotification()).isFalse();

            BoardListCreationPolicy.ListCountStatus warning = BoardListCreationPolicy.ListCountStatus.WARNING;
            assertThat(warning.getDisplayName()).isEqualTo("경고");
            assertThat(warning.canCreateList()).isTrue();
            assertThat(warning.requiresNotification()).isTrue();

            BoardListCreationPolicy.ListCountStatus limitReached = BoardListCreationPolicy.ListCountStatus.LIMIT_REACHED;
            assertThat(limitReached.getDisplayName()).isEqualTo("제한 도달");
            assertThat(limitReached.canCreateList()).isFalse();
            assertThat(limitReached.requiresNotification()).isTrue();
        }
    }
}