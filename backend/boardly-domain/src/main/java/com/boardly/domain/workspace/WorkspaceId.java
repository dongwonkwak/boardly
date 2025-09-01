package com.boardly.domain.workspace;

import com.boardly.shared.DomainIdPrefixes;
import com.boardly.shared.EntityId;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 워크스페이스 ID 값 객체
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceId extends EntityId {

    public WorkspaceId(String value) {
        super(value);
    }

    /**
     * 새로운 WorkspaceId 생성 (prefix 포함 ULID)
     */
    public static WorkspaceId generate() {
        return new WorkspaceId(generateWithPrefix(DomainIdPrefixes.WORKSPACE));
    }

    /**
     * 문자열로부터 WorkspaceId 생성
     */
    public static WorkspaceId of(String value) {
        return new WorkspaceId(value);
    }

    /**
     * WorkspaceId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidFormat(String value) {
        return EntityId.isValidFormat(value, DomainIdPrefixes.WORKSPACE);
    }

    /**
     * 문자열에서 안전하게 WorkspaceId를 생성 (검증 실패 시 null 반환)
     * 
     * @param value 검증할 문자열
     * @return 유효하면 WorkspaceId, 그렇지 않으면 null
     */
    public static WorkspaceId tryParse(String value) {
        if (!isValidFormat(value)) {
            return null;
        }
        try {
            return new WorkspaceId(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getPrefix() {
        return DomainIdPrefixes.WORKSPACE;
    }
}
