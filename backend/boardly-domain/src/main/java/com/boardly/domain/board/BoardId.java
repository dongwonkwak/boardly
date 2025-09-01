package com.boardly.domain.board;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 보드 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardId extends EntityId {

    public BoardId(String value) {
        super(value);
    }

    /**
     * 새로운 BoardId 생성 (prefix 포함 ULID)
     */
    public static BoardId generate() {
        return new BoardId(generateWithPrefix(DomainIdPrefixes.BOARD));
    }

    /**
     * 문자열로부터 BoardId 생성
     */
    public static BoardId of(String value) {
        return new BoardId(value);
    }

    /**
     * BoardId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.BOARD);
    }

    /**
     * 문자열에서 안전하게 BoardId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 BoardId, 그렇지 않으면 null
     */
    public static BoardId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new BoardId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.BOARD;
    }
}
