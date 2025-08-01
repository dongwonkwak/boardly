package com.boardly.features.dashboard.application.dto;

import com.boardly.features.activity.application.port.output.ActivityResponse;
import com.boardly.features.board.application.dto.BoardSummaryDto;
import lombok.Builder;

import java.util.List;

/**
 * 대시보드 응답 DTO
 * 
 * <p>
 * 대시보드 API 응답에 사용되는 데이터 전송 객체입니다.
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
@Builder
public record DashboardResponse(
                List<BoardSummaryDto> boards,
                List<ActivityResponse> recentActivity,
                DashboardStatisticsDto statistics) {
}