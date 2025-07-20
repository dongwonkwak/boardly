package com.boardly.features.card.application.port.input;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;

/**
 * 카드 조회 커맨드
 * 
 * @param cardId 조회할 카드 ID (필수)
 * @param userId 요청 사용자 ID (필수, 권한 검증용)
 */
public record GetCardCommand(
    CardId cardId,
    UserId userId) {

}
