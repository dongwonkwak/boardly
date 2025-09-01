package com.boardly.domain.board;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 보드 컬럼 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardColumnId extends EntityId {

    public BoardColumnId(String value) {
        super(value);
    }

    /**
     * 새로운 BoardColumnId 생성 (prefix 포함 ULID)
     */
    public static BoardColumnId generate() {
        return new BoardColumnId(generateWithPrefix(DomainIdPrefixes.LIST));
    }

    /**
     * 문자열로부터 BoardColumnId 생성
     */
    public static BoardColumnId of(String value) {
        return new BoardColumnId(value);
    }

    /**
     * BoardColumnId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.LIST);
    }

    /**
     * 문자열에서 안전하게 BoardColumnId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 BoardColumnId, 그렇지 않으면 null
     */
    public static BoardColumnId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new BoardColumnId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.LIST;
    }
}
