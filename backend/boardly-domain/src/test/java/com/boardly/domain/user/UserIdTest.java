package com.boardly.domain.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserId 형식 검증 테스트
 */
class UserIdTest {

    @Test
    void generate_정상적인_UserId_생성() {
        // when
        var userId = UserId.generate();

        // then
        assertThat(userId.getValue()).isNotNull();
        assertThat(userId.getValue()).startsWith("usr");
        assertThat(userId.getValue()).hasSize(29); // "usr" (3) + ULID (26)
    }

    @Test
    void isValidFormat_유효한_형식_true() {
        // given
        var validUserId = UserId.generate();
        String validFormat = validUserId.getValue();

        // when & then
        assertThat(UserId.isValidFormat(validFormat)).isTrue();
    }

    @Test
    void isValidFormat_null_또는_빈값_false() {
        // when & then
        assertThat(UserId.isValidFormat(null)).isFalse();
        assertThat(UserId.isValidFormat("")).isFalse();
        assertThat(UserId.isValidFormat("   ")).isFalse();
    }

    @Test
    void isValidFormat_잘못된_prefix_false() {
        // given
        String wrongPrefix = "admin_01234567890123456789012345";

        // when & then
        assertThat(UserId.isValidFormat(wrongPrefix)).isFalse();
    }

    @Test
    void isValidFormat_잘못된_ULID_길이_false() {
        // given
        String shortUlid = "usr123";
        String longUlid = "usr012345678901234567890123456789";

        // when & then
        assertThat(UserId.isValidFormat(shortUlid)).isFalse();
        assertThat(UserId.isValidFormat(longUlid)).isFalse();
    }

    @Test
    void isValidFormat_잘못된_ULID_문자_false() {
        // given (ULID에 허용되지 않는 문자 포함)
        String invalidChars1 = "usr01234567890123456789ILOU23"; // I, L, O, U 포함
        String invalidChars2 = "usr0123456789012345678901234!"; // 특수문자 포함
        String invalidChars3 = "usr0123456789012345678901234a"; // 소문자 포함

        // when & then
        assertThat(UserId.isValidFormat(invalidChars1)).isFalse();
        assertThat(UserId.isValidFormat(invalidChars2)).isFalse();
        assertThat(UserId.isValidFormat(invalidChars3)).isFalse();
    }

    @Test
    void isValidFormat_유효한_ULID_문자_true() {
        // given - 실제로 생성된 유효한 UserId 사용
        var validUserId = UserId.generate();
        String validUlid = validUserId.getValue();

        // when & then
        assertThat(UserId.isValidFormat(validUlid)).isTrue();
    }

    @Test
    void tryParse_유효한_형식_UserId_반환() {
        // given
        var originalUserId = UserId.generate();
        String validFormat = originalUserId.getValue();

        // when
        var parsedUserId = UserId.tryParse(validFormat);

        // then
        assertThat(parsedUserId).isNotNull();
        assertThat(parsedUserId.getValue()).isEqualTo(validFormat);
    }

    @Test
    void tryParse_유효하지않은_형식_null_반환() {
        // given
        String invalidFormat = "invalid_format";

        // when
        var parsedUserId = UserId.tryParse(invalidFormat);

        // then
        assertThat(parsedUserId).isNull();
    }

    @Test
    void of_유효한_형식_UserId_생성() {
        // given
        var originalUserId = UserId.generate();
        String validFormat = originalUserId.getValue();

        // when
        var userId = UserId.of(validFormat);

        // then
        assertThat(userId.getValue()).isEqualTo(validFormat);
    }

    @Test
    void of_유효하지않은_형식_예외_발생() {
        // given
        String invalidFormat = "invalid_format";

        // when & then
        assertThatThrownBy(() -> UserId.of(invalidFormat))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid EntityId format");
    }
}
