package com.boardly.features.board.application.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * 보드 요약 정보 DTO
 *
 * <p>
 * 대시보드에서 사용되는 보드의 요약 정보를 담는 데이터 전송 객체입니다.
 *
 * @since 1.0.0
 */
@Value
@Builder
public class BoardSummaryDto {
    String id;
    String title;
    String description;
    Instant createdAt;
    int listCount;
    int cardCount;
    boolean isStarred;
    String color;
    String role;
}