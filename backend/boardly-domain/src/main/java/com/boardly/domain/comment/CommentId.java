package com.boardly.domain.comment;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 댓글 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentId extends EntityId {

    public CommentId(String value) {
        super(value);
    }

    /**
     * 새로운 CommentId 생성 (prefix 포함 ULID)
     */
    public static CommentId generate() {
        return new CommentId(generateWithPrefix(DomainIdPrefixes.COMMENT));
    }

    /**
     * 문자열로부터 CommentId 생성
     */
    public static CommentId of(String value) {
        return new CommentId(value);
    }

    /**
     * CommentId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.COMMENT);
    }

    /**
     * 문자열에서 안전하게 CommentId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 CommentId, 그렇지 않으면 null
     */
    public static CommentId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new CommentId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.COMMENT;
    }
}
