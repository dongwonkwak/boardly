package com.boardly.domain.activitylog;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 활동 로그 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityLogId extends EntityId {

    public ActivityLogId(String value) {
        super(value);
    }

    /**
     * 새로운 ActivityLogId 생성 (prefix 포함 ULID)
     */
    public static ActivityLogId generate() {
        return new ActivityLogId(generateWithPrefix(DomainIdPrefixes.ACTIVITY_LOG));
    }

    /**
     * 문자열로부터 ActivityLogId 생성
     */
    public static ActivityLogId of(String value) {
        return new ActivityLogId(value);
    }

    /**
     * ActivityLogId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.ACTIVITY_LOG);
    }

    /**
     * 문자열에서 안전하게 ActivityLogId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 ActivityLogId, 그렇지 않으면 null
     */
    public static ActivityLogId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new ActivityLogId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.ACTIVITY_LOG;
    }
}
