package com.boardly.domain.user;

import com.boardly.shared.annotation.DomainService;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * 사용자 도메인 서비스
 * 사용자 관련 비즈니스 로직을 처리합니다.
 */
@DomainService(domain = "User", responsibility = "Account Security")
@RequiredArgsConstructor
public class UserDomainService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;

    /**
     * 로그인 시도 가능 여부 확인
     */
    public boolean canAttemptLogin(User user) {
        if (!user.isActive()) {
            return false;
        }

        // 계정이 잠겨있고 아직 잠금 기간이 지나지 않았으면 로그인 불가
        if (user.isAccountLocked() && !user.isAccountLockExpired()) {
            return false;
        }

        // 잠금 기간이 지났으면 자동으로 잠금 해제
        if (user.isAccountLockExpired()) {
            user.unlockAccount();
        }

        return true;
    }

    /**
     * 로그인 실패 처리
     */
    public void handleFailedLogin(User user) {
        user.recordFailedLogin();
    }

    /**
     * 로그인 성공 처리
     */
    public void handleSuccessfulLogin(User user) {
        user.recordSuccessfulLogin();
    }

    /**
     * 계정 잠금 상태 확인 및 메시지 반환
     */
    public String getAccountLockMessage(User user) {
        if (!user.isAccountLocked()) {
            return null;
        }

        if (user.isAccountLockExpired()) {
            return "계정 잠금이 해제되었습니다. 다시 로그인해주세요.";
        }

        Instant unlockTime = user.getAccountLockedAt().plusSeconds(ACCOUNT_LOCK_DURATION_MINUTES * 60);
        long remainingMinutes = (unlockTime.getEpochSecond() - Instant.now().getEpochSecond()) / 60;
        
        return String.format("계정이 잠겼습니다. %d분 후에 다시 시도해주세요.", remainingMinutes);
    }

    /**
     * 남은 로그인 시도 횟수 반환
     */
    public int getRemainingLoginAttempts(User user) {
        return Math.max(0, MAX_FAILED_LOGIN_ATTEMPTS - user.getFailedLoginAttempts());
    }

    /**
     * 계정 잠금 해제 (관리자용)
     */
    public void unlockUserAccount(User user) {
        user.unlockAccount();
    }
}
