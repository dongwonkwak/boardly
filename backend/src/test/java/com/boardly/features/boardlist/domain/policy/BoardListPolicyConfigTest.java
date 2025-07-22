package com.boardly.features.boardlist.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * BoardListPolicyConfig 기본값 테스트
 */
@DisplayName("BoardListPolicyConfig 기본값 테스트")
class BoardListPolicyConfigTest {

    @Test
    @DisplayName("기본값들이 올바르게 설정되어 있는지 확인")
    void shouldHaveCorrectDefaultValues() {
        // when & then
        assertThat(BoardListPolicyConfig.Defaults.MAX_LISTS_PER_BOARD).isEqualTo(20);
        assertThat(BoardListPolicyConfig.Defaults.RECOMMENDED_LISTS_PER_BOARD).isEqualTo(10);
        assertThat(BoardListPolicyConfig.Defaults.WARNING_THRESHOLD).isEqualTo(15);
        assertThat(BoardListPolicyConfig.Defaults.MAX_TITLE_LENGTH).isEqualTo(100);
    }

    @Test
    @DisplayName("기본값들이 논리적으로 유효한지 확인")
    void shouldHaveValidDefaultValues() {
        // when & then
        assertThat(BoardListPolicyConfig.Defaults.MAX_LISTS_PER_BOARD)
                .isGreaterThan(BoardListPolicyConfig.Defaults.WARNING_THRESHOLD);

        assertThat(BoardListPolicyConfig.Defaults.WARNING_THRESHOLD)
                .isGreaterThan(BoardListPolicyConfig.Defaults.RECOMMENDED_LISTS_PER_BOARD);

        assertThat(BoardListPolicyConfig.Defaults.RECOMMENDED_LISTS_PER_BOARD)
                .isGreaterThan(0);

        assertThat(BoardListPolicyConfig.Defaults.MAX_TITLE_LENGTH)
                .isGreaterThan(0);
    }
}