package com.boardly.features.activity.application.port.output;

import java.util.List;

import lombok.Builder;

@Builder
public record ActivityListResponse(
        List<ActivityResponse> activities,
        int totalCount,
        int currentPage,
        int totalPages,
        boolean hasNextPage,
        boolean hasPreviousPage) {

    public static ActivityListResponse of(List<ActivityResponse> activities) {
        return ActivityListResponse.builder()
                .activities(activities)
                .totalCount(activities.size())
                .currentPage(0)
                .totalPages(1)
                .hasNextPage(false)
                .hasPreviousPage(false)
                .build();
    }

    public static ActivityListResponse withPaging(
            List<ActivityResponse> activities,
            int totalCount,
            int currentPage,
            int pageSize) {
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        return ActivityListResponse.builder()
                .activities(activities)
                .totalCount(totalCount)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .hasNextPage(currentPage < totalPages - 1)
                .hasPreviousPage(currentPage > 0)
                .build();
    }
}
