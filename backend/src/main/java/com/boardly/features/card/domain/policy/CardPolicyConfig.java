package com.boardly.features.card.domain.policy;

/**
 * 카드 정책 설정 인터페이스
 * 
 * <p>
 * 도메인 레이어에서 정책 설정값을 읽기 위한 인터페이스입니다.
 * Infrastructure 레이어의 구현체로부터 설정값을 받아옵니다.
 */
public interface CardPolicyConfig {
  /**
   * 리스트당 최대 카드 개수를 반환합니다.
   * 설정되지 않은 경우 기본값을 반환합니다.
   */
  int getMaxCardsPerList();

  /**
   * 카드 제목 최대 길이를 반환합니다.
   * 설정되지 않은 경우 기본값을 반환합니다.
   */
  int getMaxTitleLength();

  /**
   * 카드 설명 최대 길이를 반환합니다.
   * 설정되지 않은 경우 기본값을 반환합니다.
   */
  int getMaxDescriptionLength();

  /**
   * 카드 검색 시 최대 결과 개수를 반환합니다.
   * 설정되지 않은 경우 기본값을 반환합니다.
   */
  int getMaxSearchResults();

  /**
   * 기본값들을 반환하는 상수 클래스
   */
  final class Defaults {
    public static final int MAX_CARDS_PER_LIST = 100;
    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;
    public static final int MAX_SEARCH_RESULTS = 50;

    private Defaults() {
      // 상수 클래스
    }
  }
}
