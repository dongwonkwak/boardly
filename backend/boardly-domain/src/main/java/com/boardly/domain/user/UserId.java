package com.boardly.domain.user;

import com.boardly.shared.DomainPrefixes;
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
        return new UserId(generateWithPrefix(DomainPrefixes.USER));
    }

    /**
     * 문자열로부터 UserId 생성 (검증 포함)
     */
    public static UserId of(String value) {
        return new UserId(value);
    }

    /**
     * 문자열로부터 UserId 생성 (검증 포함) - of()의 별칭
     */
    public static UserId from(String value) {
        return of(value);
    }

    @Override
    protected String getPrefix() {
        return DomainPrefixes.USER;
    }
}
