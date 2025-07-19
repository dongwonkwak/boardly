package com.boardly.features.boardlist.domain.model;

import java.util.Set;

import io.micrometer.common.lang.NonNull;

public record ListColor(@NonNull String color) {

  private static final String DEFAULT_COLOR = "#0079BF";
  private static final Set<String> VALID_COLORS = Set.of(
      "#0079BF", // Blue (default)
      "#D29034", // Orange
      "#519839", // Green
      "#B04632", // Red
      "#89609E", // Purple
      "#CD5A91", // Pink
      "#4BBFDA", // Light Blue
      "#00AECC", // Teal
      "#838C91"  // Gray
  );

  public static ListColor of(String color) {
    if (color == null || color.trim().isEmpty() || !VALID_COLORS.contains(color.trim())) {
      return new ListColor(DEFAULT_COLOR);
    }
    
    return new ListColor(color.trim());
  }

  public static ListColor defaultColor() {
    return new ListColor(DEFAULT_COLOR);
  }

  /**
   * 주어진 색상 값이 유효한 색상인지 검증합니다.
   * 
   * @param color 검증할 색상 값
   * @return 유효한 색상이면 true, 그렇지 않으면 false
   */
  public static boolean isValidColor(String color) {
    if (color == null || color.trim().isEmpty()) {
      return false;
    }
    
    return VALID_COLORS.contains(color.trim());
  }

  /**
   * 유효한 색상 목록을 반환합니다.
   * 
   * @return 유효한 색상들의 Set
   */
  public static Set<String> getValidColors() {
    return Set.copyOf(VALID_COLORS);
  }
}
