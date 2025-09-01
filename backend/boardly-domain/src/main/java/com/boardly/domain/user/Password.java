package com.boardly.domain.user;

/**
 * 비밀번호 값 객체
 * - 해시된 비밀번호를 안전하게 캡슐화
 * - 원시 비밀번호는 저장하지 않음
 */
public record Password(String hash) {

    public Password {
        if (hash == null || hash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
    }

    /**
     * 해시된 비밀번호로부터 Password 생성
     * 
     * @param hash BCrypt 등으로 해시된 비밀번호
     * @return Password 객체
     */
    public static Password fromHash(String hash) {
        return new Password(hash);
    }

    /**
     * 해시 값 반환
     */
    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "[PROTECTED]"; // 보안을 위해 실제 해시 값은 노출하지 않음
    }
}
