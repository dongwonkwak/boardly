package com.boardly.domain.user;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 사용자 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserId extends EntityId {

    public UserId(String value) {
        super(value);
    }

    /**
     * 새로운 UserId 생성 (prefix 포함 ULID)
     */
    public static UserId generate() {
        return new UserId(generateWithPrefix(DomainIdPrefixes.USER));
    }

    /**
     * 문자열로부터 UserId 생성
     */
    public static UserId of(String value) {
        return new UserId(value);
    }

    /**
     * UserId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.USER);
    }

    /**
     * 문자열에서 안전하게 UserId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 UserId, 그렇지 않으면 null
     */
    public static UserId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new UserId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.USER;
    }
}
