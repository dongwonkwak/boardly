package com.boardly.features.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("UserId 테스트")
class UserIdTest {

    @Test
    @DisplayName("문자열로 UserId 생성 시 해당 문자열이 저장되어야 한다")
    void createUserIdWithString_shouldStoreGivenString() {
        // given
        String expectedId = "test-user-id-123";

        // when
        UserId userId = new UserId(expectedId);

        // then
        assertThat(userId.getId()).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("기본 생성자로 UserId 생성 시 자동으로 ID가 생성되어야 한다")
    void createUserIdWithDefaultConstructor_shouldGenerateId() {
        // when
        UserId userId = new UserId();

        // then
        assertNotNull(userId.getId());
        assertThat(userId.getId()).isNotEmpty();
        assertThat(userId.getId()).hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("두 번의 기본 생성자 호출로 생성된 UserId는 서로 다른 ID를 가져야 한다")
    void createTwoUserIdsWithDefaultConstructor_shouldHaveDifferentIds() {
        // when
        UserId userId1 = new UserId();
        UserId userId2 = new UserId();

        // then
        assertThat(userId1.getId()).isNotEqualTo(userId2.getId());
    }

    @Test
    @DisplayName("같은 문자열로 생성된 두 UserId는 동일해야 한다")
    void createTwoUserIdsWithSameString_shouldBeEqual() {
        // given
        String sameId = "same-user-id";

        // when
        UserId userId1 = new UserId(sameId);
        UserId userId2 = new UserId(sameId);

        // then
        assertThat(userId1).isEqualTo(userId2);
        assertThat(userId1.hashCode()).isEqualTo(userId2.hashCode());
    }

    @Test
    @DisplayName("다른 문자열로 생성된 두 UserId는 다르다")
    void createTwoUserIdsWithDifferentStrings_shouldNotBeEqual() {
        // given
        String id1 = "user-id-1";
        String id2 = "user-id-2";

        // when
        UserId userId1 = new UserId(id1);
        UserId userId2 = new UserId(id2);

        // then
        assertThat(userId1).isNotEqualTo(userId2);
        assertThat(userId1.hashCode()).isNotEqualTo(userId2.hashCode());
    }

    @Test
    @DisplayName("UserId와 null 비교 시 다르다")
    void compareUserIdWithNull_shouldNotBeEqual() {
        // given
        UserId userId = new UserId("test-id");

        // when & then
        assertThat(userId).isNotEqualTo(null);
    }

    @Test
    @DisplayName("UserId와 다른 타입 객체 비교 시 다르다")
    void compareUserIdWithDifferentType_shouldNotBeEqual() {
        // given
        UserId userId = new UserId("test-id");
        String stringObject = "test-id";

        // when & then
        assertThat(userId).isNotEqualTo(stringObject);
    }


    @Test
    @DisplayName("getId 메서드는 순수한 문자열 ID를 반환해야 한다")
    void getId_shouldReturnPureStringId() {
        // given
        String expectedId = "pure-string-id";
        UserId userId = new UserId(expectedId);

        // when
        String result = userId.getId();

        // then
        assertThat(result).isEqualTo(expectedId);
        assertThat(result).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("빈 문자열로 UserId 생성 시 빈 문자열이 저장되어야 한다")
    void createUserIdWithEmptyString_shouldStoreEmptyString() {
        // given
        String emptyId = "";

        // when
        UserId userId = new UserId(emptyId);

        // then
        assertThat(userId.getId()).isEqualTo(emptyId);
        assertThat(userId.getId()).isEmpty();
    }

    @Test
    @DisplayName("null 문자열로 UserId 생성 시 null이 저장되어야 한다")
    void createUserIdWithNullString_shouldStoreNull() {
        // given
        String nullId = null;

        // when
        UserId userId = new UserId(nullId);

        // then
        assertThat(userId.getId()).isNull();
    }

    @Test
    @DisplayName("특수 문자가 포함된 문자열로 UserId 생성이 가능해야 한다")
    void createUserIdWithSpecialCharacters_shouldWork() {
        // given
        String specialId = "user-id_123@domain.com#special!";

        // when
        UserId userId = new UserId(specialId);

        // then
        assertThat(userId.getId()).isEqualTo(specialId);
    }

    @Test
    @DisplayName("매우 긴 문자열로 UserId 생성이 가능해야 한다")
    void createUserIdWithLongString_shouldWork() {
        // given
        String longId = "a".repeat(1000);

        // when
        UserId userId = new UserId(longId);

        // then
        assertThat(userId.getId()).isEqualTo(longId);
        assertThat(userId.getId()).hasSize(1000);
    }
} 