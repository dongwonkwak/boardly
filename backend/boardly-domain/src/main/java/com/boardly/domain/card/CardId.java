package com.boardly.domain.card;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 카드 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardId extends EntityId {

    public CardId(String value) {
        super(value);
    }

    /**
     * 새로운 CardId 생성 (prefix 포함 ULID)
     */
    public static CardId generate() {
        return new CardId(generateWithPrefix(DomainIdPrefixes.CARD));
    }

    /**
     * 문자열로부터 CardId 생성
     */
    public static CardId of(String value) {
        return new CardId(value);
    }

    /**
     * CardId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.CARD);
    }

    /**
     * 문자열에서 안전하게 CardId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 CardId, 그렇지 않으면 null
     */
    public static CardId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new CardId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.CARD;
    }
}
