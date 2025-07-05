package com.boardly.shared.presentation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Path {
  public static final String PREFIX = "/api";
  public static final String USERS = PREFIX + "/users";
  public static final String BOARDS = PREFIX + "/boards";
}
