package com.boardly.features.activity.application.service;

import com.boardly.features.activity.application.dto.*;
import com.boardly.features.activity.application.query.GetActivityQuery;
import com.boardly.features.activity.application.usecase.GetActivityUseCase;
import com.boardly.features.activity.domain.Activity;
import com.boardly.features.activity.domain.port.ActivityRepository;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.validation.MessageResolver;
import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ActivityReadService implements GetActivityUseCase {

    private final ActivityRepository activityRepository;
    private final BoardRepository boardRepository;
    private final MessageResolver messageResolver;

    private static final int DEFAULT_PAGE_SIZE = 50;

    @Override
    public Either<Failure, ActivityListResponse> getActivities(
        GetActivityQuery query
    ) {
        try {
            log.debug("활동 목록 조회 요청: {}", query);

            // 쿼리 검증
            Either<Failure, Void> validationResult = validateQuery(query);
            if (validationResult.isLeft()) return Either.left(
                validationResult.getLeft()
            );

            // 활동 조회 및 응답 생성
            List<Activity> activities = fetchActivities(query);
            ActivityListResponse response = createResponse(activities, query);

            log.debug("활동 목록 조회 완료: 총 {}개", activities.size());
            return Either.right(response);
        } catch (Exception e) {
            log.error("활동 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            String errorMessage = messageResolver.getMessageWithDefault(
                String.format(
                    "validation.%s.%s.%s",
                    "activity",
                    "read",
                    "error"
                ),
                "활동 목록 조회 중 오류가 발생했습니다."
            );
            return Either.left(Failure.ofInternalServerError(errorMessage));
        }
    }

    /**
     * 쿼리 검증
     */
    private Either<Failure, Void> validateQuery(GetActivityQuery query) {
        if (query.boardId() == null && query.userId() == null) {
            String errorMessage =
                messageResolver.getDomainValidationMessageWithDefault(
                    "activity",
                    "query",
                    "invalid",
                    "보드 ID 또는 사용자 ID 중 하나는 필수입니다."
                );
            return Either.left(Failure.ofInputError(errorMessage));
        }
        return Either.right(null);
    }

    /**
     * 쿼리 조건에 따라 활동 조회
     */
    private List<Activity> fetchActivities(GetActivityQuery query) {
        if (query.boardId() != null) {
            return fetchBoardActivities(query);
        } else {
            return fetchUserActivities(query);
        }
    }

    /**
     * 보드 활동 조회
     */
    private List<Activity> fetchBoardActivities(GetActivityQuery query) {
        if (query.since() != null && query.until() != null) {
            return activityRepository.findByBoardIdAndTimestampBetween(
                query.boardId(),
                query.since(),
                query.until()
            );
        } else if (query.since() != null) {
            return fetchBoardActivitiesWithSinceAndPaging(query);
        } else {
            return fetchBoardActivitiesWithPaging(query);
        }
    }

    /**
     * 보드 활동 조회 (since 파라미터와 페이징 포함)
     */
    private List<Activity> fetchBoardActivitiesWithSinceAndPaging(
        GetActivityQuery query
    ) {
        int page = query.getPageOrDefault();
        int size = query.getSizeOrDefault();
        boolean hasPaging = isPagingRequested(query);

        if (hasPaging) {
            return activityRepository.findByBoardIdAndTimestampAfter(
                query.boardId(),
                query.since(),
                page,
                size
            );
        } else {
            return activityRepository.findByBoardIdAndTimestampAfter(
                query.boardId(),
                query.since()
            );
        }
    }

    /**
     * 보드 활동 조회 (페이징 포함)
     */
    private List<Activity> fetchBoardActivitiesWithPaging(
        GetActivityQuery query
    ) {
        int page = query.getPageOrDefault();
        int size = query.getSizeOrDefault();
        boolean hasPaging = isPagingRequested(query);

        if (hasPaging) {
            return activityRepository.findByBoardIdOrderByTimestampDesc(
                query.boardId(),
                page,
                size
            );
        } else {
            return activityRepository.findByBoardIdOrderByTimestampDesc(
                query.boardId()
            );
        }
    }

    /**
     * 사용자 활동 조회
     */
    private List<Activity> fetchUserActivities(GetActivityQuery query) {
        int page = query.getPageOrDefault();
        int size = query.getSizeOrDefault();
        boolean hasPaging = isPagingRequested(query);

        if (hasPaging) {
            return activityRepository.findByActorIdOrderByTimestampDesc(
                query.userId(),
                page,
                size
            );
        } else {
            return activityRepository.findByActorIdOrderByTimestampDesc(
                query.userId()
            );
        }
    }

    /**
     * 응답 생성
     */
    private ActivityListResponse createResponse(
        List<Activity> activities,
        GetActivityQuery query
    ) {
        List<ActivityResponse> activityResponses = activities
            .stream()
            .map(this::toActivityResponse)
            .collect(Collectors.toList());

        int page = query.getPageOrDefault();
        int size = query.getSizeOrDefault();
        boolean hasPaging = isPagingRequested(query);

        if (hasPaging) {
            long totalCount = getTotalCount(query);
            return ActivityListResponse.withPaging(
                activityResponses,
                (int) totalCount,
                page,
                size
            );
        } else {
            return ActivityListResponse.of(activityResponses);
        }
    }

    /**
     * 총 개수 조회
     */
    private long getTotalCount(GetActivityQuery query) {
        if (query.boardId() != null) {
            if (query.since() != null) {
                return activityRepository.countByBoardIdAndTimestampAfter(
                    query.boardId(),
                    query.since()
                );
            } else {
                return activityRepository.countByBoardId(query.boardId());
            }
        } else {
            return activityRepository.countByActorId(query.userId());
        }
    }

    private boolean isPagingRequested(GetActivityQuery query) {
        return (
            query.getPageOrDefault() > 0 ||
            query.getSizeOrDefault() != DEFAULT_PAGE_SIZE
        );
    }

    /**
     * Activity 도메인 객체를 ActivityResponse로 변환
     */
    private ActivityResponse toActivityResponse(Activity activity) {
        ActorResponse actorResponse = ActorResponse.builder()
            .id(activity.getActor().getId())
            .firstName(activity.getActor().getFirstName())
            .lastName(activity.getActor().getLastName())
            .profileImageUrl(activity.getActor().getProfileImageUrl())
            .build();

        // 보드 정보 조회
        String boardName = null;
        String boardId = null;
        if (activity.getBoardId() != null) {
            boardId = activity.getBoardId().getId();
            boardName = boardRepository
                .findBoardNameById(activity.getBoardId())
                .orElse(null);
        }

        return ActivityResponse.builder()
            .id(activity.getId().getId())
            .type(activity.getType())
            .actor(actorResponse)
            .timestamp(activity.getTimestamp())
            .payload(activity.getPayload().getData())
            .boardName(boardName)
            .boardId(boardId)
            .build();
    }
}
