package com.boardly.features.board.domain.model;

/**
 * 보드 멤버의 역할을 정의하는 enum
 */
public enum BoardRole {

    /**
     * 소유자 - 모든 권한을 가짐
     */
    OWNER(true, true, true, true),

    /**
     * 관리자 - 보드 설정 변경, 멤버 관리 가능
     */
    ADMIN(true, true, true, false),

    /**
     * 편집자 - 카드와 리스트 생성/수정 가능
     */
    EDITOR(true, true, false, false),

    /**
     * 뷰어 - 읽기만 가능
     */
    VIEWER(true, false, false, false);

    private final boolean canRead;
    private final boolean canWrite;
    private final boolean canAdmin;
    private final boolean canOwn;

    BoardRole(boolean canRead, boolean canWrite, boolean canAdmin, boolean canOwn) {
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.canAdmin = canAdmin;
        this.canOwn = canOwn;
    }

    public boolean hasReadPermission() {
        return canRead;
    }

    public boolean hasWritePermission() {
        return canWrite;
    }

    public boolean hasAdminPermission() {
        return canAdmin;
    }

    public boolean hasOwnerPermission() {
        return canOwn;
    }

    /**
     * 역할의 우선순위를 반환합니다. (높을수록 권한이 큼)
     */
    public int getPriority() {
        return switch (this) {
            case OWNER -> 4;
            case ADMIN -> 3;
            case EDITOR -> 2;
            case VIEWER -> 1;
        };
    }

    /**
     * 다른 역할보다 권한이 높은지 확인합니다.
     */
    public boolean hasHigherPermissionThan(BoardRole other) {
        return this.getPriority() > other.getPriority();
    }
}