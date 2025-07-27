package com.boardly.features.attachment.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AttachmentPolicyConfig 기본값 테스트
 */
@DisplayName("AttachmentPolicyConfig 기본값 테스트")
class AttachmentPolicyConfigTest {

    @Test
    @DisplayName("기본값들이 올바르게 설정되어 있는지 확인")
    void shouldHaveCorrectDefaultValues() {
        // when & then
        assertThat(AttachmentPolicyConfig.Defaults.MAX_FILE_SIZE_MB).isEqualTo(10);
        assertThat(AttachmentPolicyConfig.Defaults.MAX_ATTACHMENTS_PER_CARD).isEqualTo(10);
        assertThat(AttachmentPolicyConfig.Defaults.MAX_FILE_NAME_LENGTH).isEqualTo(255);
    }

    @Test
    @DisplayName("기본값들이 논리적으로 유효한지 확인")
    void shouldHaveValidDefaultValues() {
        // when & then
        assertThat(AttachmentPolicyConfig.Defaults.MAX_FILE_SIZE_MB)
                .isGreaterThan(0);

        assertThat(AttachmentPolicyConfig.Defaults.MAX_ATTACHMENTS_PER_CARD)
                .isGreaterThan(0);

        assertThat(AttachmentPolicyConfig.Defaults.MAX_FILE_NAME_LENGTH)
                .isGreaterThan(0);
    }
}