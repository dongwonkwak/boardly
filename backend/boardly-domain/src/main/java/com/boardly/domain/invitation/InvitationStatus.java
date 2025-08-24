package com.boardly.domain.invitation;

/**
 * 초대 상태
 */
public enum InvitationStatus {
    PENDING, // 대기중
    ACCEPTED, // 수락됨
    DECLINED, // 거절됨
    EXPIRED // 만료됨
}
