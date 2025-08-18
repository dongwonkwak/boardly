package com.boardly.features.workspace.domain.port;

import com.boardly.features.workspace.domain.Workspace;
import com.boardly.shared.common.value.UserId;
import java.util.List;

/**
 * Workspace 도메인 Repository 포트
 */
public interface WorkspaceRepository {
    /**
     * 주어진 사용자 ID 기준으로 사용자가 소유하거나 멤버로 포함된 워크스페이스 목록을 반환합니다.
     */
    List<Workspace> findByUserId(UserId userId);
}
