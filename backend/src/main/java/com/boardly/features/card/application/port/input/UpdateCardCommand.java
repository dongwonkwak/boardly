package com.boardly.features.card.application.port.input;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;

import static org.apache.commons.lang3.StringUtils.trim;

/**
 * 카드 수정 커맨드
 * 
 * @param cardId      수정할 카드 ID (필수)
 * @param title       새로운 카드 제목 (필수, 1-200자)
 * @param description 새로운 카드 설명 (선택, 최대 2000자, 마크다운 지원)
 * @param userId      요청 사용자 ID (필수, 권한 검증용)
 */
public record UpdateCardCommand(
    CardId cardId,
    String title,
    String description,
    UserId userId) {

  public static UpdateCardCommand of(CardId cardId, String title, String description, UserId userId) {
    return new UpdateCardCommand(
        cardId,
        trim(title),
        description != null ? description.trim() : null,
        userId);
  }
}
