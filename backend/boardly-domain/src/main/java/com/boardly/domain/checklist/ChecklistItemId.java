package com.boardly.domain.checklist;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 체크리스트 아이템 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistItemId extends EntityId {

    public ChecklistItemId(String value) {
        super(value);
    }

    /**
     * 새로운 ChecklistItemId 생성 (prefix 포함 ULID)
     */
    public static ChecklistItemId generate() {
        return new ChecklistItemId(generateWithPrefix(DomainIdPrefixes.CHECKLIST_ITEM));
    }

    /**
     * 문자열로부터 ChecklistItemId 생성
     */
    public static ChecklistItemId of(String value) {
        return new ChecklistItemId(value);
    }

    /**
     * ChecklistItemId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.CHECKLIST_ITEM);
    }

    /**
     * 문자열에서 안전하게 ChecklistItemId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 ChecklistItemId, 그렇지 않으면 null
     */
    public static ChecklistItemId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new ChecklistItemId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.CHECKLIST_ITEM;
    }
}
