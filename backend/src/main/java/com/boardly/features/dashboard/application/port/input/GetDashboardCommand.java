package com.boardly.features.dashboard.application.port.input;

import com.boardly.features.user.domain.model.UserId;

/**
 * 대시보드 조회 커맨드
 * 
 * <p>
 * 대시보드 조회에 필요한 입력 데이터를 담는 커맨드 객체입니다.
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
public record GetDashboardCommand(
        UserId userId) {

    /**
     * 기본 대시보드 조회 커맨드 생성
     * 
     * @param userId 사용자 ID
     * @return GetDashboardCommand 객체
     */
    public static GetDashboardCommand of(UserId userId) {
        return new GetDashboardCommand(userId);
    }
}