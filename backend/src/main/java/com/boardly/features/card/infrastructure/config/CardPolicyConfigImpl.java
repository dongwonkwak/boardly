package com.boardly.features.card.infrastructure.config;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.boardly.features.card.domain.policy.CardPolicyConfig;

/**
 * 카드 정책 설정 구현체
 * 
 * <p>
 * Infrastructure 레이어에서 도메인의 CardPolicyConfig 인터페이스를 구현합니다.
 * 설정값이 없거나 유효하지 않은 경우 기본값을 반환합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardPolicyConfigImpl implements CardPolicyConfig {

  private final CardPolicyProperties properties;

  @Override
  public int getMaxCardsPerList() {
    int configValue = properties.getMaxCardsPerList();

    if (configValue <= 0) {
      log.debug("리스트당 최대 카드 개수 설정값이 유효하지 않음: {}. 기본값 {} 사용",
          configValue, Defaults.MAX_CARDS_PER_LIST);
      return Defaults.MAX_CARDS_PER_LIST;
    }

    return configValue;
  }

  @Override
  public int getMaxTitleLength() {
    int configValue = properties.getMaxTitleLength();

    if (configValue <= 0) {
      log.debug("카드 제목 최대 길이 설정값이 유효하지 않음: {}. 기본값 {} 사용",
          configValue, Defaults.MAX_TITLE_LENGTH);
      return Defaults.MAX_TITLE_LENGTH;
    }

    return configValue;
  }

  @Override
  public int getMaxDescriptionLength() {
    int configValue = properties.getMaxDescriptionLength();

    if (configValue <= 0) {
      log.debug("카드 설명 최대 길이 설정값이 유효하지 않음: {}. 기본값 {} 사용",
          configValue, Defaults.MAX_DESCRIPTION_LENGTH);
      return Defaults.MAX_DESCRIPTION_LENGTH;
    }

    return configValue;
  }

  @Override
  public int getMaxSearchResults() {
    int configValue = properties.getMaxSearchResults();

    if (configValue <= 0) {
      log.debug("검색 결과 최대 개수 설정값이 유효하지 않음: {}. 기본값 {} 사용",
          configValue, Defaults.MAX_SEARCH_RESULTS);
      return Defaults.MAX_SEARCH_RESULTS;
    }

    return configValue;
  }
}
