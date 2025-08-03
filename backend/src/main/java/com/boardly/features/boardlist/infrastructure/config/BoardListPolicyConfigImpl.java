package com.boardly.features.boardlist.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.boardly.features.boardlist.domain.policy.BoardListPolicyConfig;

import lombok.Data;

/**
 * 보드 리스트 정책 설정 구현체
 * 
 * <p>
 * application.yml에서 설정값을 읽어와 정책에 적용합니다.
 * 설정되지 않은 값은 기본값을 사용합니다.
 */
@Data
@ConfigurationProperties(prefix = "boardly.boardlist.policy")
public class BoardListPolicyConfigImpl implements BoardListPolicyConfig {

    /**
     * 보드당 최대 리스트 개수
     */
    private Integer maxListsPerBoard;

    /**
     * 권장 리스트 개수
     */
    private Integer recommendedListsPerBoard;

    /**
     * 경고 임계값
     */
    private Integer warningThreshold;

    /**
     * 리스트 제목 최대 길이
     */
    private Integer maxTitleLength;

    @Override
    public int getMaxListsPerBoard() {
        return maxListsPerBoard != null ? maxListsPerBoard : Defaults.MAX_LISTS_PER_BOARD;
    }

    @Override
    public int getRecommendedListsPerBoard() {
        return recommendedListsPerBoard != null ? recommendedListsPerBoard : Defaults.RECOMMENDED_LISTS_PER_BOARD;
    }

    @Override
    public int getWarningThreshold() {
        return warningThreshold != null ? warningThreshold : Defaults.WARNING_THRESHOLD;
    }

    @Override
    public int getMaxTitleLength() {
        return maxTitleLength != null ? maxTitleLength : Defaults.MAX_TITLE_LENGTH;
    }
}