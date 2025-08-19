package com.boardly.features.card.application.command;

import static org.apache.commons.lang3.StringUtils.trim;

import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;

/**
 * 카드 생성 커맨드
 * 
 * @param title       카드 제목 (필수, 1-200자)
 * @param description 카드 설명 (선택, 최대 2000자, 마크다운 지원)
 * @param listId      대상 리스트 ID (필수)
 * @param workspaceId 워크스페이스 ID (필수)
 * @param userId      요청 사용자 ID (필수, 권한 검증용)
 */
public record CreateCardCommand(
    String title,
    String description,
    ListId listId,
    WorkspaceId workspaceId,
    UserId userId) {

  public static CreateCardCommand of(String title, String description, ListId listId, WorkspaceId workspaceId,
      UserId userId) {
    return new CreateCardCommand(
        trim(title),
        description != null ? description.trim() : null,
        listId,
        workspaceId,
        userId);
  }
}
