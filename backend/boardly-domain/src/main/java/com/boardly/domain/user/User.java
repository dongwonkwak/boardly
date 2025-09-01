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
    private Email email;
    private Password password;
    private String displayName;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    public User(UserId id, Email email, Password password, String displayName,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 사용자 생성 팩토리 메서드
     */
    public static User create(Email email, String passwordHash, String displayName) {
        return User.builder()
                .id(UserId.generate())
                .email(email)
                .password(Password.fromHash(passwordHash))
                .displayName(displayName)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 사용자 정보 업데이트
     */
    public void updateInfo(String displayName) {
        this.displayName = displayName;
        this.updatedAt = Instant.now();
    }

    /**
     * 표시 이름 반환 (displayName이 없으면 이메일의 로컬 부분 반환)
     */
    public String getDisplayName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        // username이 없으므로 이메일의 @ 앞 부분을 사용
        return email.value().split("@")[0];
    }

    /**
     * 비밀번호 해시 값 반환
     */
    public String getPasswordHash() {
        return password != null ? password.getHash() : null;
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPasswordHash) {
        this.password = Password.fromHash(newPasswordHash);
        this.updatedAt = Instant.now();
    }
}
