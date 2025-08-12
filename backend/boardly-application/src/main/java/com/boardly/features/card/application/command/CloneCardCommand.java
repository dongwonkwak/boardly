package com.boardly.features.card.application.command;

import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;

/**
 * 카드 복제 커맨드
 * 
 * @param cardId       복제할 원본 카드 ID (필수)
 * @param newTitle     새로운 카드 제목 (필수, 1-200자)
 * @param targetListId 대상 리스트 ID (다른 리스트로 복제 시 필수, 같은 리스트에 복제 시 null)
 * @param userId       요청 사용자 ID (필수, 권한 검증용)
 */
public record CloneCardCommand(
    CardId cardId,
    String newTitle,
    ListId targetListId,
    UserId userId) {

  public static CloneCardCommand of(CardId cardId, String newTitle, ListId targetListId, UserId userId) {
    return new CloneCardCommand(cardId, newTitle, targetListId, userId);
  }
}
