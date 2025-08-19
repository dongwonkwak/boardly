package com.boardly.features.card.application.command;

import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;

/**
 * 카드 삭제 커맨드
 * 
 * @param cardId 삭제할 카드 ID (필수)
 * @param userId 요청 사용자 ID (필수, 권한 검증용)
 */
public record DeleteCardCommand(
    CardId cardId,
    UserId userId) {

  public static DeleteCardCommand of(CardId cardId, UserId userId) {
    return new DeleteCardCommand(cardId, userId);
  }
}
