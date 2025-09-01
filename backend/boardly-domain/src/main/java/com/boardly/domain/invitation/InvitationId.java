package com.boardly.domain.invitation;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 초대 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvitationId extends EntityId {

    public InvitationId(String value) {
        super(value);
    }

    /**
     * 새로운 InvitationId 생성 (prefix 포함 ULID)
     */
    public static InvitationId generate() {
        return new InvitationId(generateWithPrefix(DomainIdPrefixes.INVITATION));
    }

    /**
     * 문자열로부터 InvitationId 생성
     */
    public static InvitationId of(String value) {
        return new InvitationId(value);
    }

    /**
     * InvitationId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.INVITATION);
    }

    /**
     * 문자열에서 안전하게 InvitationId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 InvitationId, 그렇지 않으면 null
     */
    public static InvitationId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new InvitationId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.INVITATION;
    }
}
