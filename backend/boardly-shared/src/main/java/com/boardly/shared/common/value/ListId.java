package com.boardly.shared.common.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ListId extends EntityId {

    public ListId(String listId) {
        super(listId);
    }

    public ListId() {
        super();
    }
}
