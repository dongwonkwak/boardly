package com.boardly.features.boardlist.domain.policy;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ListLimitPolicy {
  
  private static final int MAX_LISTS_PER_BOARD = 20;

  
}
