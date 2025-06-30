package com.boardly.features.user.domain.model;

import com.boardly.shared.domain.valueobject.EntityId;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class UserId extends EntityId {

    public UserId(String userId) {
        super(userId);
    }

    public UserId() {
        super();
    }

    @Override
    public String toString() {
        return String.format("UserId{userId=%s}", super.toString());
    }
}
