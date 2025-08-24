package com.boardly.domain.activitylog;

import com.boardly.shared.DomainPrefixes;
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
        return new ActivityLogId(generateWithPrefix(DomainPrefixes.ACTIVITY_LOG));
    }

    /**
     * 문자열로부터 ActivityLogId 생성 (검증 포함)
     */
    public static ActivityLogId of(String value) {
        return new ActivityLogId(value);
    }

    /**
     * 문자열로부터 ActivityLogId 생성 (검증 포함) - of()의 별칭
     */
    public static ActivityLogId from(String value) {
        return of(value);
    }

    @Override
    protected String getPrefix() {
        return DomainPrefixes.ACTIVITY_LOG;
    }
}
