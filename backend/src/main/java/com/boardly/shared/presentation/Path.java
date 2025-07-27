package com.boardly.shared.presentation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Path {
  public static final String PREFIX = "/api";
  public static final String USERS = PREFIX + "/users";
  public static final String BOARDS = PREFIX + "/boards";
  public static final String DASHBOARD = PREFIX + "/dashboard";

  /*
   * Board List
   */
  public static final String BOARD_LISTS = PREFIX + "/board-lists";

  /*
   * Card 관련 경로
   */
  public static final String CARDS = PREFIX + "/cards";
  public static final String CARD_BY_ID = CARDS + "/{cardId}";
  public static final String CARDS_BY_LIST = CARDS + "/lists/{listId}";
  public static final String CARD_SEARCH = CARDS + "/lists/{listId}/search";
  public static final String CARD_MOVE = CARDS + "/{cardId}/move";
  public static final String CARD_CLONE = CARDS + "/{cardId}/clone";

  /**
   * 특정 보드의 리스트 목록 조회 경로
   */
  public static final String BOARD_LISTS_BY_BOARD = BOARD_LISTS + "/board/{boardId}";

  /**
   * Activity 관련 경로
   */
  public static final String ACTIVITIES = PREFIX + "/activities";

  /**
   * Label 관련 경로
   */
  public static final String LABELS = PREFIX + "/labels";
}
