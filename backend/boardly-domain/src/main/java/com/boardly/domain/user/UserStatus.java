package com.boardly.domain.user;

/**
 * 사용자 상태
 */
public enum UserStatus {

    /**
     * 활성 상태
     */
    ACTIVE,

    /**
     * 비활성 상태
     */
    INACTIVE,

    /**
     * 정지 상태
     */
    SUSPENDED,

    /**
     * 삭제 상태
     */
    DELETED,

    /**
     * 이메일 미인증 상태
     */
    PENDING_EMAIL_VERIFICATION
}