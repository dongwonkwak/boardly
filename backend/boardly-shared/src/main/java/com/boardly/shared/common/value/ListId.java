package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ListId extends EntityId {

    public ListId(String listId) {
        super(listId);
    }

    public ListId() {
        super("list_" + UlidCreator.getUlid().toString());
    }
}
