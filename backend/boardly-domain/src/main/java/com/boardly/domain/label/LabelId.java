package com.boardly.domain.label;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 라벨 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabelId extends EntityId {

    public LabelId(String value) {
        super(value);
    }

    /**
     * 새로운 LabelId 생성 (prefix 포함 ULID)
     */
    public static LabelId generate() {
        return new LabelId(generateWithPrefix(DomainIdPrefixes.LABEL));
    }

    /**
     * 문자열로부터 LabelId 생성
     */
    public static LabelId of(String value) {
        return new LabelId(value);
    }

    /**
     * LabelId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.LABEL);
    }

    /**
     * 문자열에서 안전하게 LabelId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 LabelId, 그렇지 않으면 null
     */
    public static LabelId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new LabelId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.LABEL;
    }
}
