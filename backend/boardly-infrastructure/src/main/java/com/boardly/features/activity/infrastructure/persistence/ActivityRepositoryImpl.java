package com.boardly.features.activity.infrastructure.persistence;

import com.boardly.features.activity.domain.Activity;
import com.boardly.features.activity.domain.port.ActivityRepository;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.ActivityId;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;
import io.vavr.control.Either;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ActivityRepositoryImpl implements ActivityRepository {

  private final ActivityJpaRepository activityJpaRepository;

  @Override
  public Either<Failure, Activity> save(Activity activity) {
    try {
      log.debug("활동 저장 시작: activityId={}, type={}", activity.getId().getId(), activity.getType());
      ActivityEntity entity = ActivityEntity.fromDomainEntity(activity);
      ActivityEntity savedEntity = activityJpaRepository.save(entity);
      log.debug("활동 저장 완료: activityId={}", savedEntity.getActivityId());
      return Either.right(savedEntity.toDomainEntity());
    } catch (Exception e) {
      log.error("활동 저장 중 오류 발생: {}", e.getMessage(), e);
      return Either.left(Failure.ofInternalServerError("Failed to save activity: " + e.getMessage()));
    }
  }

  @Override
  public Optional<Activity> findById(ActivityId activityId) {
    log.debug("활동 조회 시작: activityId={}", activityId.getId());
    return activityJpaRepository.findById(activityId.getId()).map(ActivityEntity::toDomainEntity);
  }

  @Override
  public List<Activity> findByBoardIdOrderByTimestampDesc(BoardId boardId) {
    log.debug("보드별 활동 목록 조회 시작: boardId={}", boardId.getId());
    return activityJpaRepository.findByBoardIdOrderByCreatedAtDesc(boardId.getId()).stream()
        .map(ActivityEntity::toDomainEntity)
        .toList();
  }

  @Override
  public List<Activity> findByBoardIdOrderByTimestampDesc(BoardId boardId, int page, int size) {
    log.debug("보드별 활동 목록 페이징 조회 시작: boardId={}, page={}, size={}", boardId.getId(), page, size);
    Pageable pageable = PageRequest.of(page, size);
    return activityJpaRepository
        .findByBoardIdOrderByCreatedAtDesc(boardId.getId(), pageable)
        .stream()
        .map(ActivityEntity::toDomainEntity)
        .toList();
  }

  @Override
  public List<Activity> findByActorIdOrderByTimestampDesc(UserId userId) {
    log.debug("사용자별 활동 목록 조회 시작: userId={}", userId.getId());
    return activityJpaRepository.findByActorIdOrderByCreatedAtDesc(userId.getId()).stream()
        .map(ActivityEntity::toDomainEntity)
        .toList();
  }

  @Override
  public List<Activity> findByActorIdOrderByTimestampDesc(UserId userId, int page, int size) {
    log.debug(
        "사용자별 활동 목록 페이징 조회 시작: userId={}, page={}, size={}",
        userId.getId(),
        page,
        size);
    Pageable pageable = PageRequest.of(page, size);
    return activityJpaRepository
        .findByActorIdOrderByCreatedAtDesc(userId.getId(), pageable)
        .stream()
        .map(ActivityEntity::toDomainEntity)
        .toList();
  }

  @Override
  public List<Activity> findByBoardIdAndTimestampBetween(BoardId boardId, Instant startTime, Instant endTime) {
    log.debug(
        "보드별 기간 활동 목록 조회 시작: boardId={}, startTime={}, endTime={}",
        boardId.getId(),
        startTime,
        endTime);
    return activityJpaRepository
        .findByBoardIdAndCreatedAtBetween(boardId.getId(), startTime, endTime)
        .stream()
        .map(ActivityEntity::toDomainEntity)
        .toList();
  }

  @Override
  public long countByBoardId(BoardId boardId) {
    log.debug("보드별 활동 개수 조회: boardId={}", boardId.getId());
    return activityJpaRepository.countByBoardId(boardId.getId());
  }

  @Override
  public long countByBoardIdAndTimestampAfter(BoardId boardId, Instant after) {
    log.debug("보드별 특정 시점 이후 활동 개수 조회: boardId={}, after={}", boardId.getId(), after);
    return activityJpaRepository
        .findByBoardIdAndCreatedAtAfter(boardId.getId(), after)
        .size();
  }

  @Override
  public long countByActorId(UserId userId) {
    log.debug("사용자별 활동 개수 조회: userId={}", userId.getId());
    return activityJpaRepository.countByActorId(userId.getId());
  }

  @Override
  public List<Activity> findByBoardIdAndTimestampAfter(BoardId boardId, Instant after) {
    log.debug("보드별 특정 시점 이후 활동 목록 조회: boardId={}, after={}", boardId.getId(), after);
    return activityJpaRepository
        .findByBoardIdAndCreatedAtAfter(boardId.getId(), after)
        .stream()
        .map(ActivityEntity::toDomainEntity)
        .toList();
  }

  @Override
  public List<Activity> findByBoardIdAndTimestampAfter(BoardId boardId, Instant after, int page, int size) {
    log.debug(
        "보드별 특정 시점 이후 활동 목록 페이징 조회: boardId={}, after={}, page={}, size={}",
        boardId.getId(), after, page, size);
    Pageable pageable = PageRequest.of(page, size);
    return activityJpaRepository
        .findByBoardIdAndCreatedAtAfter(boardId.getId(), after)
        .stream()
        .skip((long) page * size)
        .limit(size)
        .map(ActivityEntity::toDomainEntity)
        .toList();
  }

  @Override
  public Either<Failure, Void> deleteByTimestampBefore(Instant before) {
    try {
      log.debug("특정 시점 이전 활동 삭제 시작: before={}", before);
      activityJpaRepository.deleteByCreatedAtBefore(before);
      log.debug("특정 시점 이전 활동 삭제 완료: before={}", before);
      return Either.right(null);
    } catch (Exception e) {
      log.error("활동 삭제 중 오류 발생: {}", e.getMessage(), e);
      return Either.left(Failure.ofInternalServerError("Failed to delete activities: " + e.getMessage()));
    }
  }

  @Override
  public boolean existsById(ActivityId activityId) {
    log.debug("활동 존재 확인: activityId={}", activityId.getId());
    return activityJpaRepository.existsById(activityId.getId());
  }
}
