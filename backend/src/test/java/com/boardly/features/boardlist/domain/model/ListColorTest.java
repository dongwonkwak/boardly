package com.boardly.features.boardlist.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ListColorTest {

    @Test
    @DisplayName("유효한 색상으로 ListColor를 생성할 수 있어야 한다")
    void of_ValidColor_ShouldCreateListColor() {
        // given
        String validColor = "#0079BF";

        // when
        ListColor listColor = ListColor.of(validColor);

        // then
        assertThat(listColor.color()).isEqualTo(validColor);
    }

    @Test
    @DisplayName("유효하지 않은 색상으로 ListColor를 생성하면 기본 색상이 사용되어야 한다")
    void of_InvalidColor_ShouldUseDefaultColor() {
        // given
        String invalidColor = "#FFFFFF";

        // when
        ListColor listColor = ListColor.of(invalidColor);

        // then
        assertThat(listColor.color()).isEqualTo("#0079BF"); // 기본 색상
    }

    @Test
    @DisplayName("빈 문자열로 ListColor를 생성하면 기본 색상이 사용되어야 한다")
    void of_EmptyString_ShouldUseDefaultColor() {
        // given
        String emptyColor = "";

        // when
        ListColor listColor = ListColor.of(emptyColor);

        // then
        assertThat(listColor.color()).isEqualTo("#0079BF"); // 기본 색상
    }

    @Test
    @DisplayName("공백만 있는 문자열로 ListColor를 생성하면 기본 색상이 사용되어야 한다")
    void of_BlankString_ShouldUseDefaultColor() {
        // given
        String blankColor = "   ";

        // when
        ListColor listColor = ListColor.of(blankColor);

        // then
        assertThat(listColor.color()).isEqualTo("#0079BF"); // 기본 색상
    }

    @Test
    @DisplayName("null로 ListColor를 생성하면 기본 색상이 사용되어야 한다")
    void of_Null_ShouldUseDefaultColor() {
        // when
        ListColor listColor = ListColor.of(null);

        // then
        assertThat(listColor.color()).isEqualTo("#0079BF"); // 기본 색상
    }

    @Test
    @DisplayName("defaultColor()는 기본 색상을 반환해야 한다")
    void defaultColor_ShouldReturnDefaultColor() {
        // when
        ListColor listColor = ListColor.defaultColor();

        // then
        assertThat(listColor.color()).isEqualTo("#0079BF");
    }

    @Test
    @DisplayName("isValidColor()는 유효한 색상을 true로 반환해야 한다")
    void isValidColor_ValidColor_ShouldReturnTrue() {
        // given
        String[] validColors = {
            "#0079BF", // Blue
            "#D29034", // Orange
            "#519839", // Green
            "#B04632", // Red
            "#89609E", // Purple
            "#CD5A91", // Pink
            "#4BBFDA", // Light Blue
            "#00AECC", // Teal
            "#838C91"  // Gray
        };

        // when & then
        for (String color : validColors) {
            assertThat(ListColor.isValidColor(color)).as("Color: " + color).isTrue();
        }
    }

    @Test
    @DisplayName("isValidColor()는 유효하지 않은 색상을 false로 반환해야 한다")
    void isValidColor_InvalidColor_ShouldReturnFalse() {
        // given
        String[] invalidColors = {
            "#FFFFFF", // White (유효하지 않음)
            "#000000", // Black (유효하지 않음)
            "#FF0000", // Red (유효하지 않음)
            "invalid", // 잘못된 형식
            "0079BF",  // # 없음
            "#0079B",  // 잘못된 길이
            "#0079BFF" // 잘못된 길이
        };

        // when & then
        for (String color : invalidColors) {
            assertThat(ListColor.isValidColor(color)).as("Color: " + color).isFalse();
        }
    }

    @Test
    @DisplayName("isValidColor()는 null을 false로 반환해야 한다")
    void isValidColor_Null_ShouldReturnFalse() {
        // when & then
        assertThat(ListColor.isValidColor(null)).isFalse();
    }

    @Test
    @DisplayName("isValidColor()는 빈 문자열을 false로 반환해야 한다")
    void isValidColor_EmptyString_ShouldReturnFalse() {
        // when & then
        assertThat(ListColor.isValidColor("")).isFalse();
    }

    @Test
    @DisplayName("isValidColor()는 공백만 있는 문자열을 false로 반환해야 한다")
    void isValidColor_BlankString_ShouldReturnFalse() {
        // when & then
        assertThat(ListColor.isValidColor("   ")).isFalse();
    }

    @Test
    @DisplayName("getValidColors()는 모든 유효한 색상을 반환해야 한다")
    void getValidColors_ShouldReturnAllValidColors() {
        // when
        Set<String> validColors = ListColor.getValidColors();

        // then
        assertThat(validColors).hasSize(9);
        assertThat(validColors).contains(
            "#0079BF", // Blue
            "#D29034", // Orange
            "#519839", // Green
            "#B04632", // Red
            "#89609E", // Purple
            "#CD5A91", // Pink
            "#4BBFDA", // Light Blue
            "#00AECC", // Teal
            "#838C91"  // Gray
        );
    }

    @Test
    @DisplayName("getValidColors()는 불변 Set을 반환해야 한다")
    void getValidColors_ShouldReturnImmutableSet() {
        // when
        Set<String> validColors = ListColor.getValidColors();

        // then
        assertThat(validColors).isUnmodifiable();
    }
} 