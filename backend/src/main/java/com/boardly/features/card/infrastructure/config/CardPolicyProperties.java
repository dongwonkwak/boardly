package com.boardly.features.card.infrastructure.config;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 정책 프로퍼티
 * 
 * <p>
 * application.yml에서 카드 관련 정책 값들을 주입받는 설정 클래스입니다.
 * Infrastructure 레이어에 위치하여 외부 설정을 도메인으로 전달하는 역할을 합니다.
 */
@Slf4j
@Getter
@Component
@ConfigurationProperties(prefix = "boardly.card.policy")
public class CardPolicyProperties {

  /**
   * 리스트당 최대 카드 개수
   */
  private int maxCardsPerList = 0; // 기본값은 0으로 설정 (미설정 상태)

  /**
   * 카드 제목 최대 길이
   */
  private int maxTitleLength = 0;

  /**
   * 카드 설명 최대 길이
   */
  private int maxDescriptionLength = 0;

  /**
   * 카드 검색 시 최대 결과 개수
   */
  private int maxSearchResults = 0;

  /**
   * 설정값 로딩 완료 로깅
   */
  @PostConstruct
  public void logLoadedConfiguration() {
    log.info("카드 정책 프로퍼티 로드 완료:");
    log.info("  - 리스트당 최대 카드 개수: {}", maxCardsPerList > 0 ? maxCardsPerList : "미설정 (기본값 사용)");
    log.info("  - 카드 제목 최대 길이: {}", maxTitleLength > 0 ? maxTitleLength : "미설정 (기본값 사용)");
    log.info("  - 카드 설명 최대 길이: {}", maxDescriptionLength > 0 ? maxDescriptionLength : "미설정 (기본값 사용)");
    log.info("  - 검색 결과 최대 개수: {}", maxSearchResults > 0 ? maxSearchResults : "미설정 (기본값 사용)");
  }

  // Setter 메서드들 (Spring Boot가 사용)
  public void setMaxCardsPerList(int maxCardsPerList) {
    this.maxCardsPerList = maxCardsPerList;
  }

  public void setMaxTitleLength(int maxTitleLength) {
    this.maxTitleLength = maxTitleLength;
  }

  public void setMaxDescriptionLength(int maxDescriptionLength) {
    this.maxDescriptionLength = maxDescriptionLength;
  }

  public void setMaxSearchResults(int maxSearchResults) {
    this.maxSearchResults = maxSearchResults;
  }
}
