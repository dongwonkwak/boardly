package com.boardly.features.activity.application.port.output;

import java.util.List;

/**
 * 활동 목록 응답 객체
 * 활동 목록과 페이징 정보를 포함
 */
public record ActivityListResponse(
        List<ActivityResponse> activities,
        int totalCount,
        int currentPage,
        int totalPages,
        boolean hasNextPage,
        boolean hasPreviousPage) {

    /**
     * 페이징 없이 활동 목록만으로 응답 객체 생성
     * 
     * @param activities 활동 목록
     * @return ActivityListResponse 객체
     */
    public static ActivityListResponse of(List<ActivityResponse> activities) {
        return new ActivityListResponse(
                activities,
                activities.size(),
                0,
                1,
                false,
                false);
    }

    /**
     * 페이징 정보와 함께 활동 목록 응답 객체 생성
     * 
     * @param activities  활동 목록
     * @param totalCount  전체 활동 수
     * @param currentPage 현재 페이지 번호
     * @param pageSize    페이지 크기
     * @return ActivityListResponse 객체
     */
    public static ActivityListResponse withPaging(
            List<ActivityResponse> activities,
            int totalCount,
            int currentPage,
            int pageSize) {
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        return new ActivityListResponse(
                activities,
                totalCount,
                currentPage,
                totalPages,
                currentPage < totalPages - 1,
                currentPage > 0);
    }
}
