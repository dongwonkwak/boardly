package com.boardly.features.dashboard.domain.model;

import com.boardly.features.activity.domain.model.Activity;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.dashboard.domain.valueobject.DashboardStatistics;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * 대시보드 도메인 모델
 * 
 * <p>
 * 사용자의 대시보드 정보를 담는 도메인 객체입니다.
 * 보드 목록, 최근 활동, 통계 정보를 포함합니다.
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dashboard {

    private List<Board> boards;
    private List<Activity> recentActivities;
    private DashboardStatistics statistics;

    @Builder
    public Dashboard(
            List<Board> boards,
            List<Activity> recentActivities,
            DashboardStatistics statistics) {
        this.boards = boards;
        this.recentActivities = recentActivities;
        this.statistics = statistics;
    }

    /**
     * 대시보드 정보를 업데이트합니다.
     * 
     * @param boards           보드 목록
     * @param recentActivities 최근 활동 목록
     * @param statistics       통계 정보
     */
    public void updateDashboard(
            List<Board> boards,
            List<Activity> recentActivities,
            DashboardStatistics statistics) {
        this.boards = boards;
        this.recentActivities = recentActivities;
        this.statistics = statistics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Dashboard dashboard = (Dashboard) o;
        return Objects.equals(boards, dashboard.boards) &&
                Objects.equals(recentActivities, dashboard.recentActivities) &&
                Objects.equals(statistics, dashboard.statistics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boards, recentActivities, statistics);
    }
}