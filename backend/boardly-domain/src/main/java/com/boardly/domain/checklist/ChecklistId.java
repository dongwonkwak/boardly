package com.boardly.domain.checklist;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 체크리스트 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistId extends EntityId {

    public ChecklistId(String value) {
        super(value);
    }

    /**
     * 새로운 ChecklistId 생성 (prefix 포함 ULID)
     */
    public static ChecklistId generate() {
        return new ChecklistId(generateWithPrefix(DomainIdPrefixes.CHECKLIST));
    }

    /**
     * 문자열로부터 ChecklistId 생성
     */
    public static ChecklistId of(String value) {
        return new ChecklistId(value);
    }

    /**
     * ChecklistId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.CHECKLIST);
    }

    /**
     * 문자열에서 안전하게 ChecklistId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 ChecklistId, 그렇지 않으면 null
     */
    public static ChecklistId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new ChecklistId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.CHECKLIST;
    }
}
