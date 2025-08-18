package com.boardly.features.workspace.domain;

import com.boardly.shared.common.value.BoardRole;
import com.boardly.shared.common.value.WorkspaceRole;

/**
 * 워크스페이스와 보드 권한 우선순위 정책을 정의합니다.
 * 원칙: 워크스페이스 권한이 보드 권한보다 우선한다.
 */
public final class WorkspaceBoardPermissionPolicy {

    private WorkspaceBoardPermissionPolicy() {}

    /**
     * 권한 충돌 시 최종 권한을 설명적으로 결정합니다.
     * - 워크스페이스 OWNER/ADMIN은 모든 보드에서 최고 권한
     * - 워크스페이스 MEMBER는 최소한 읽기/쓰기 기본 권한 유지(문서 기준)
     * - 워크스페이스 GUEST는 보드 권한만 적용
     */
    public static EffectivePermission resolve(WorkspaceRole workspaceRole, BoardRole boardRole) {
        if (workspaceRole == null) {
            // 워크스페이스 멤버가 아닌 경우: 보드 권한만 적용
            return EffectivePermission.fromBoard(boardRole);
        }
        switch (workspaceRole) {
            case OWNER:
                return EffectivePermission.owner();
            case ADMIN:
                return EffectivePermission.admin();
            case MEMBER:
                // 워크스페이스 멤버는 최소한 읽기/쓰기 권한, 보드 권한이 더 높으면 승급
                if (boardRole == null) {
                    return EffectivePermission.member();
                }
                return EffectivePermission.max(EffectivePermission.member(), EffectivePermission.fromBoard(boardRole));
            case GUEST:
            default:
                return EffectivePermission.fromBoard(boardRole);
        }
    }

    /**
     * 최종 권한 모델(간소화).
     */
    public record EffectivePermission(boolean canRead, boolean canWrite, boolean canAdmin, boolean canOwn) {
        public static EffectivePermission owner() { return new EffectivePermission(true, true, true, true); }
        public static EffectivePermission admin() { return new EffectivePermission(true, true, true, false); }
        public static EffectivePermission member() { return new EffectivePermission(true, true, false, false); }
        public static EffectivePermission viewer() { return new EffectivePermission(true, false, false, false); }
        public static EffectivePermission none() { return new EffectivePermission(false, false, false, false); }

        public static EffectivePermission fromBoard(BoardRole role) {
            if (role == null) return none();
            return new EffectivePermission(
                role.hasReadPermission(),
                role.hasWritePermission(),
                role.hasAdminPermission(),
                role.hasOwnerPermission()
            );
        }

        public static EffectivePermission max(EffectivePermission a, EffectivePermission b) {
            return new EffectivePermission(
                a.canRead || b.canRead,
                a.canWrite || b.canWrite,
                a.canAdmin || b.canAdmin,
                a.canOwn || b.canOwn
            );
        }
    }
}
