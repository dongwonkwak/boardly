package com.boardly.features.boardlist.domain.policy;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 보드 리스트 개수 제한 정책
 * 
 * <p>보드당 생성 가능한 리스트의 최대 개수를 관리하는 도메인 정책입니다.
 * 성능과 사용자 경험을 고려하여 적절한 제한을 두어 시스템 안정성을 보장합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListLimitPolicy {
  
  // 보드당 최대 리스트 개수
  private static final int MAX_LISTS_PER_BOARD = 20;

  /**
   * 권장 리스트 개수
   * 일반적인 칸반 보드 사용 패턴을 고려한 권장 개수
   */
  private static final int RECOMMENDED_LISTS_PER_BOARD = 10;
  
  /**
   * 경고 임계값
   * 이 개수를 초과하면 사용자에게 경고 메시지 표시
   */
  private static final int WARNING_THRESHOLD = 15;

  /**
   * 보드 리스트 개수 제한 정책
   * 
   * <p>보드당 생성 가능한 리스트의 최대 개수를 관리하는 도메인 정책입니다.
   * 성능과 사용자 경험을 고려하여 적절한 제한을 두어 시스템 안정성을 보장합니다.
   * 
   * @since 1.0.0
  /**
   * 새로운 리스트를 생성할 수 있는지 확인합니다.
   * 
   * @param currentListCount 현재 보드의 리스트 개수
   * @return 생성 가능하면 true, 그렇지 않으면 false
   */
  public boolean canCreateList(long currentListCount) {
    boolean canCreate = currentListCount < MAX_LISTS_PER_BOARD;
    
    if (!canCreate) {
      log.warn("보드 리스트 개수 제한 초과: currentCount={}, maxAllowed={}", 
              currentListCount, MAX_LISTS_PER_BOARD);
    }

    return canCreate;
  }
  
  /**
   * 경고 임계값을 초과했는지 확인합니다.
   * 
   * @param currentListCount 현재 보드의 리스트 개수
   * @return 경고가 필요하면 true, 그렇지 않으면 false
   */
  public boolean shouldShowWarning(long currentListCount) {
    return currentListCount >= WARNING_THRESHOLD;
  }

  /**
   * 권장 개수를 초과했는지 확인합니다.
   * 
   * @param currentListCount 현재 보드의 리스트 개수
   * @return 권장 개수 초과 시 true, 그렇지 않으면 false
   */
  public boolean exceedsRecommended(long currentListCount) {
    return currentListCount > RECOMMENDED_LISTS_PER_BOARD;
  }
  
  /**
   * 현재 리스트 개수에 대한 상태를 반환합니다.
   * 
   * @param currentListCount 현재 보드의 리스트 개수
   * @return 리스트 개수 상태
   */
  public ListCountStatus getStatus(long currentListCount) {
    if (currentListCount >= MAX_LISTS_PER_BOARD) {
        return ListCountStatus.LIMIT_REACHED;
    } else if (currentListCount >= WARNING_THRESHOLD) {
        return ListCountStatus.WARNING;
    } else if (currentListCount > RECOMMENDED_LISTS_PER_BOARD) {
        return ListCountStatus.ABOVE_RECOMMENDED;
    } else {
        return ListCountStatus.NORMAL;
    }
  }
  
  /**
   * 남은 생성 가능한 리스트 개수를 반환합니다.
   * 
   * @param currentListCount 현재 보드의 리스트 개수
   * @return 남은 생성 가능 개수
   */
  public int getRemainingCount(long currentListCount) {
    return Math.max(0, MAX_LISTS_PER_BOARD - (int) currentListCount);
  }

  /**
   * 최대 허용 리스트 개수를 반환합니다.
   * 
   * @return 최대 허용 개수
   */
  public int getMaxListsPerBoard() {
    return MAX_LISTS_PER_BOARD;
  }
  
  /**
   * 권장 리스트 개수를 반환합니다.
   * 
   * @return 권장 개수
   */
  public int getRecommendedListsPerBoard() {
    return RECOMMENDED_LISTS_PER_BOARD;
  }
  
  /**
   * 경고 임계값을 반환합니다.
   * 
   * @return 경고 임계값
   */
  public int getWarningThreshold() {
    return WARNING_THRESHOLD;
  }

  /**
   * 리스트 개수 상태를 나타내는 열거형
   */
  public enum ListCountStatus {
    /**
     * 정상 범위 (권장 개수 이하)
     */
    NORMAL("정상", "리스트 개수가 적절합니다."),
    
    /**
     * 권장 개수 초과 (하지만 경고 임계값 미만)
     */
    ABOVE_RECOMMENDED("권장 초과", "권장 개수를 초과했습니다. 리스트를 정리하는 것을 고려해보세요."),
    
    /**
     * 경고 임계값 초과 (하지만 최대 개수 미만)
     */
    WARNING("경고", "리스트가 너무 많습니다. 성능에 영향을 줄 수 있습니다."),
    
    /**
     * 최대 개수 도달 (더 이상 생성 불가)
     */
    LIMIT_REACHED("제한 도달", "최대 리스트 개수에 도달했습니다. 새 리스트를 생성하려면 기존 리스트를 삭제해주세요.");
    
    private final String displayName;
    private final String message;
    
    ListCountStatus(String displayName, String message) {
      this.displayName = displayName;
      this.message = message;
    }
    
    public String getDisplayName() {
      return displayName;
    }
    
    public String getMessage() {
      return message;
    }
    
    /**
     * 리스트 생성이 가능한 상태인지 확인합니다.
     * 
     * @return 생성 가능하면 true
     */
    public boolean canCreateList() {
      return this != LIMIT_REACHED;
    }
    
    /**
     * 사용자에게 알림을 표시해야 하는 상태인지 확인합니다.
     * 
     * @return 알림이 필요하면 true
     */
    public boolean requiresNotification() {
      return this == WARNING || this == LIMIT_REACHED;
    }
  }
}
