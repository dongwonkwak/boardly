package com.boardly.features.dashboard.domain.valueobject;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대시보드 통계 정보 값 객체
 * 
 * <p>
 * 사용자의 보드 및 카드 관련 통계 정보를 담는 불변 객체입니다.
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardStatistics {

    private int totalBoards;
    private int totalCards;
    private int starredBoards;
    private int archivedBoards;

    @Builder
    public DashboardStatistics(
            int totalBoards,
            int totalCards,
            int starredBoards,
            int archivedBoards) {
        this.totalBoards = totalBoards;
        this.totalCards = totalCards;
        this.starredBoards = starredBoards;
        this.archivedBoards = archivedBoards;
    }

    /**
     * 새로운 통계 정보로 업데이트된 객체를 생성합니다.
     * 
     * @param totalBoards    전체 보드 수
     * @param totalCards     전체 카드 수
     * @param starredBoards  즐겨찾기한 보드 수
     * @param archivedBoards 보관된 보드 수
     * @return 새로운 DashboardStatistics 객체
     */
    public DashboardStatistics withUpdatedStatistics(
            int totalBoards,
            int totalCards,
            int starredBoards,
            int archivedBoards) {
        return DashboardStatistics.builder()
                .totalBoards(totalBoards)
                .totalCards(totalCards)
                .starredBoards(starredBoards)
                .archivedBoards(archivedBoards)
                .build();
    }
}