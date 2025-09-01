package com.boardly.domain.invitation;

import com.boardly.shared.EntityId;

/**
 * 초대 ID 도메인 객체
 */
public class InvitationId extends EntityId {

    public InvitationId() {
        super();
    }

    public InvitationId(String value) {
        super(value);
    }

    public static InvitationId generate() {
        return new InvitationId(generateWithPrefix("inv_"));
    }

    public static InvitationId of(String value) {
        return new InvitationId(value);
    }

    @Override
    protected String getPrefix() {
        return "inv_";
    }
}
