package com.boardly.domain.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 사용자 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private UserId id;
    private String email;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private UserStatus status;
    private int failedLoginAttempts;
    private Instant accountLockedAt;
    private Instant lastFailedLoginAt;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    public User(UserId id, String email, String username, String displayName, String profileImageUrl, UserStatus status,
            int failedLoginAttempts, Instant accountLockedAt, Instant lastFailedLoginAt, Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.failedLoginAttempts = failedLoginAttempts;
        this.accountLockedAt = accountLockedAt;
        this.lastFailedLoginAt = lastFailedLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 사용자 생성 팩토리 메서드
     */
    public static User create(String email, String username, String displayName, String profileImageUrl) {
        return User.builder()
                .id(UserId.generate())
                .email(email)
                .username(username)
                .displayName(displayName)
                .profileImageUrl(profileImageUrl)
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .accountLockedAt(null)
                .lastFailedLoginAt(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 사용자 정보 업데이트
     */
    public void updateInfo(String username, String displayName, String profileImageUrl) {
        this.username = username;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = Instant.now();
    }

    /**
     * 사용자 상태 변경
     */
    public void changeStatus(UserStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    /**
     * 활성 사용자인지 확인
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * 계정이 잠겨있는지 확인
     */
    public boolean isAccountLocked() {
        return accountLockedAt != null;
    }

    /**
     * 계정 잠금이 해제되었는지 확인 (30분 후 자동 해제)
     */
    public boolean isAccountLockExpired() {
        if (accountLockedAt == null) {
            return false;
        }
        return Instant.now().isAfter(accountLockedAt.plusSeconds(30 * 60)); // 30분
    }

    /**
     * 로그인 실패 기록
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        this.lastFailedLoginAt = Instant.now();
        this.updatedAt = Instant.now();

        // 5회 실패 시 계정 잠금
        if (this.failedLoginAttempts >= 5) {
            this.accountLockedAt = Instant.now();
        }
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     */
    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.accountLockedAt = null;
        this.lastFailedLoginAt = null;
        this.updatedAt = Instant.now();
    }

    /**
     * 계정 잠금 해제 (관리자용)
     */
    public void unlockAccount() {
        this.failedLoginAttempts = 0;
        this.accountLockedAt = null;
        this.lastFailedLoginAt = null;
        this.updatedAt = Instant.now();
    }

    /**
     * 로그인 가능한 상태인지 확인
     */
    public boolean canLogin() {
        return isActive() && !isAccountLocked() || isAccountLockExpired();
    }

    /**
     * 표시 이름 반환 (displayName이 없으면 username 반환)
     */
    public String getDisplayName() {
        return displayName != null && !displayName.trim().isEmpty() ? displayName : username;
    }

    /**
     * @username 형태로 반환
     */
    public String getUsernameWithAt() {
        return "@" + username;
    }
}
