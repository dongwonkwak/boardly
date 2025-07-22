package com.boardly.features.boardlist.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.boardly.features.boardlist.domain.policy.BoardListPolicyConfig;

/**
 * BoardListPolicyConfigImpl 테스트
 */
@DisplayName("BoardListPolicyConfigImpl 테스트")
class BoardListPolicyConfigImplTest {

    private BoardListPolicyConfigImpl config;

    @BeforeEach
    void setUp() {
        config = new BoardListPolicyConfigImpl();
    }

    @Nested
    @DisplayName("기본값 테스트")
    class DefaultValueTest {

        @Test
        @DisplayName("설정값이 null일 때 기본값을 반환해야 함")
        void shouldReturnDefaultValuesWhenSettingsAreNull() {
            // when & then
            assertThat(config.getMaxListsPerBoard()).isEqualTo(BoardListPolicyConfig.Defaults.MAX_LISTS_PER_BOARD);
            assertThat(config.getRecommendedListsPerBoard())
                    .isEqualTo(BoardListPolicyConfig.Defaults.RECOMMENDED_LISTS_PER_BOARD);
            assertThat(config.getWarningThreshold()).isEqualTo(BoardListPolicyConfig.Defaults.WARNING_THRESHOLD);
            assertThat(config.getMaxTitleLength()).isEqualTo(BoardListPolicyConfig.Defaults.MAX_TITLE_LENGTH);
        }
    }

    @Nested
    @DisplayName("설정값 테스트")
    class ConfigValueTest {

        @Test
        @DisplayName("설정값이 있을 때 해당 값을 반환해야 함")
        void shouldReturnConfigValuesWhenSet() {
            // given
            config.setMaxListsPerBoard(30);
            config.setRecommendedListsPerBoard(15);
            config.setWarningThreshold(20);
            config.setMaxTitleLength(150);

            // when & then
            assertThat(config.getMaxListsPerBoard()).isEqualTo(30);
            assertThat(config.getRecommendedListsPerBoard()).isEqualTo(15);
            assertThat(config.getWarningThreshold()).isEqualTo(20);
            assertThat(config.getMaxTitleLength()).isEqualTo(150);
        }

        @Test
        @DisplayName("일부 설정값만 있을 때 나머지는 기본값을 반환해야 함")
        void shouldReturnDefaultValuesForUnsetConfigs() {
            // given
            config.setMaxListsPerBoard(25);
            // 다른 설정값들은 설정하지 않음

            // when & then
            assertThat(config.getMaxListsPerBoard()).isEqualTo(25);
            assertThat(config.getRecommendedListsPerBoard())
                    .isEqualTo(BoardListPolicyConfig.Defaults.RECOMMENDED_LISTS_PER_BOARD);
            assertThat(config.getWarningThreshold()).isEqualTo(BoardListPolicyConfig.Defaults.WARNING_THRESHOLD);
            assertThat(config.getMaxTitleLength()).isEqualTo(BoardListPolicyConfig.Defaults.MAX_TITLE_LENGTH);
        }
    }

    @Nested
    @DisplayName("논리적 유효성 테스트")
    class LogicalValidityTest {

        @Test
        @DisplayName("설정값들이 논리적으로 유효한지 확인")
        void shouldHaveValidLogicalValues() {
            // given
            config.setMaxListsPerBoard(30);
            config.setRecommendedListsPerBoard(15);
            config.setWarningThreshold(20);
            config.setMaxTitleLength(150);

            // when & then
            assertThat(config.getMaxListsPerBoard()).isGreaterThan(config.getWarningThreshold());
            assertThat(config.getWarningThreshold()).isGreaterThan(config.getRecommendedListsPerBoard());
            assertThat(config.getRecommendedListsPerBoard()).isGreaterThan(0);
            assertThat(config.getMaxTitleLength()).isGreaterThan(0);
        }

        @Test
        @DisplayName("기본값들이 논리적으로 유효한지 확인")
        void shouldHaveValidDefaultLogicalValues() {
            // when & then
            assertThat(config.getMaxListsPerBoard()).isGreaterThan(config.getWarningThreshold());
            assertThat(config.getWarningThreshold()).isGreaterThan(config.getRecommendedListsPerBoard());
            assertThat(config.getRecommendedListsPerBoard()).isGreaterThan(0);
            assertThat(config.getMaxTitleLength()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTest {

        @Test
        @DisplayName("최소값 설정 테스트")
        void shouldHandleMinimumValues() {
            // given
            config.setMaxListsPerBoard(1);
            config.setRecommendedListsPerBoard(1);
            config.setWarningThreshold(1);
            config.setMaxTitleLength(1);

            // when & then
            assertThat(config.getMaxListsPerBoard()).isEqualTo(1);
            assertThat(config.getRecommendedListsPerBoard()).isEqualTo(1);
            assertThat(config.getWarningThreshold()).isEqualTo(1);
            assertThat(config.getMaxTitleLength()).isEqualTo(1);
        }

        @Test
        @DisplayName("큰 값 설정 테스트")
        void shouldHandleLargeValues() {
            // given
            config.setMaxListsPerBoard(1000);
            config.setRecommendedListsPerBoard(500);
            config.setWarningThreshold(750);
            config.setMaxTitleLength(10000);

            // when & then
            assertThat(config.getMaxListsPerBoard()).isEqualTo(1000);
            assertThat(config.getRecommendedListsPerBoard()).isEqualTo(500);
            assertThat(config.getWarningThreshold()).isEqualTo(750);
            assertThat(config.getMaxTitleLength()).isEqualTo(10000);
        }
    }

    @Nested
    @DisplayName("설정 변경 테스트")
    class ConfigChangeTest {

        @Test
        @DisplayName("설정값을 변경할 수 있어야 함")
        void shouldAllowConfigValueChanges() {
            // given
            config.setMaxListsPerBoard(20);

            // when
            config.setMaxListsPerBoard(30);

            // then
            assertThat(config.getMaxListsPerBoard()).isEqualTo(30);
        }

        @Test
        @DisplayName("여러 설정값을 순차적으로 변경할 수 있어야 함")
        void shouldAllowSequentialConfigValueChanges() {
            // given
            config.setMaxListsPerBoard(20);
            config.setRecommendedListsPerBoard(10);

            // when
            config.setMaxListsPerBoard(25);
            config.setRecommendedListsPerBoard(12);
            config.setWarningThreshold(18);

            // then
            assertThat(config.getMaxListsPerBoard()).isEqualTo(25);
            assertThat(config.getRecommendedListsPerBoard()).isEqualTo(12);
            assertThat(config.getWarningThreshold()).isEqualTo(18);
        }
    }
}