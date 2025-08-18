package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class UserId extends EntityId {

    public UserId(String userId) {
        super(userId);
    }

    public UserId() {
        super("usr_" + UlidCreator.getUlid().toString());
    }
}
