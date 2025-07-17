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
    var trimmedColor = color.trim();
    if (trimmedColor.isEmpty() || !VALID_COLORS.contains(trimmedColor)) {
      return new ListColor(DEFAULT_COLOR);
    }
    
    return new ListColor(trimmedColor);
  }

  public static ListColor defaultColor() {
    return new ListColor(DEFAULT_COLOR);
  }
}
