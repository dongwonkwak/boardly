package com.boardly.features.boardlist.domain.policy;

/**
 * 보드 리스트 정책 설정 인터페이스
 * 
 * <p>
 * 도메인 레이어에서 정책 설정값을 읽기 위한 인터페이스입니다.
 * Infrastructure 레이어의 구현체로부터 설정값을 받아옵니다.
 */
public interface BoardListPolicyConfig {
    /**
     * 보드당 최대 리스트 개수를 반환합니다.
     * 설정되지 않은 경우 기본값을 반환합니다.
     */
    int getMaxListsPerBoard();

    /**
     * 권장 리스트 개수를 반환합니다.
     * 설정되지 않은 경우 기본값을 반환합니다.
     */
    int getRecommendedListsPerBoard();

    /**
     * 경고 임계값을 반환합니다.
     * 설정되지 않은 경우 기본값을 반환합니다.
     */
    int getWarningThreshold();

    /**
     * 리스트 제목 최대 길이를 반환합니다.
     * 설정되지 않은 경우 기본값을 반환합니다.
     */
    int getMaxTitleLength();

    /**
     * 기본값들을 반환하는 상수 클래스
     */
    final class Defaults {
        public static final int MAX_LISTS_PER_BOARD = 20;
        public static final int RECOMMENDED_LISTS_PER_BOARD = 10;
        public static final int WARNING_THRESHOLD = 15;
        public static final int MAX_TITLE_LENGTH = 100;

        private Defaults() {
            // 상수 클래스
        }
    }
}