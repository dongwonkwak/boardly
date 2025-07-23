package com.boardly.features.activity.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.boardly.features.activity.domain.model.Activity;
import com.boardly.features.activity.domain.model.ActivityId;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface ActivityRepository {
    /**
     * 활동을 저장합니다.
     */
    Either<Failure, Activity> save(Activity activity);

    /**
     * 활동 ID로 활동을 조회합니다.
     */
    Optional<Activity> findById(ActivityId activityId);

    /**
     * 보드의 활동 목록을 최신순으로 조회합니다.
     */
    List<Activity> findByBoardIdOrderByTimestampDesc(BoardId boardId);

    /**
     * 보드의 활동 목록을 페이징으로 조회합니다.
     */
    List<Activity> findByBoardIdOrderByTimestampDesc(BoardId boardId, int page, int size);

    /**
     * 사용자의 모든 활동 목록을 조회합니다.
     */
    List<Activity> findByActorIdOrderByTimestampDesc(UserId userId);

    /**
     * 사용자의 활동 목록을 페이징으로 조회합니다.
     */
    List<Activity> findByActorIdOrderByTimestampDesc(UserId userId, int page, int size);

    /**
     * 특정 기간 동안의 보드 활동을 조회합니다.
     */
    List<Activity> findByBoardIdAndTimestampBetween(BoardId boardId, Instant startTime, Instant endTime);

    /**
     * 보드의 활동 개수를 조회합니다.
     */
    long countByBoardId(BoardId boardId);

    /**
     * 특정 시점 이후의 보드 활동 개수를 조회합니다.
     */
    long countByBoardIdAndTimestampAfter(BoardId boardId, Instant after);

    /**
     * 사용자의 활동 개수를 조회합니다.
     */
    long countByActorId(UserId userId);

    /**
     * 특정 시점 이후의 보드 활동을 조회합니다.
     */
    List<Activity> findByBoardIdAndTimestampAfter(BoardId boardId, Instant after);

    /**
     * 특정 시점 이후의 보드 활동을 페이징으로 조회합니다.
     */
    List<Activity> findByBoardIdAndTimestampAfter(BoardId boardId, Instant after, int page, int size);

    /**
     * 오래된 활동을 삭제합니다 (데이터 정리용).
     */
    Either<Failure, Void> deleteByTimestampBefore(Instant before);

    /**
     * 활동이 존재하는지 확인합니다.
     */
    boolean existsById(ActivityId activityId);
}
