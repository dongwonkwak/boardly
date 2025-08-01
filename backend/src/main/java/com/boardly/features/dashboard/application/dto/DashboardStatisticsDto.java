package com.boardly.features.dashboard.application.dto;

import lombok.Builder;

/**
 * 대시보드 통계 정보 DTO
 * 
 * <p>
 * 대시보드 통계 정보를 담는 데이터 전송 객체입니다.
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
@Builder
public record DashboardStatisticsDto(
        int totalBoards,
        int totalCards,
        int starredBoards,
        int archivedBoards) {
}