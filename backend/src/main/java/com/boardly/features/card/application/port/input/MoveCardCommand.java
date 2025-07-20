package com.boardly.features.card.application.port.input;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;

/**
 * 카드 이동 커맨드
 * 
 * @param cardId       이동할 카드 ID (필수)
 * @param targetListId 대상 리스트 ID (다른 리스트로 이동 시 필수, 같은 리스트 내 이동 시 null)
 * @param newPosition  새로운 위치 (필수, 0 이상)
 * @param userId       요청 사용자 ID (필수, 권한 검증용)
 */
public record MoveCardCommand(
    CardId cardId,
    ListId targetListId,
    Integer newPosition,
    UserId userId) {

  public static MoveCardCommand of(CardId cardId, ListId targetListId, Integer newPosition, UserId userId) {
    return new MoveCardCommand(cardId, targetListId, newPosition, userId);
  }
}
