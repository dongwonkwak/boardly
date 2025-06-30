package com.boardly.shared.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
public class EntityId {

    private final String id;

    protected EntityId(final String id) {
        this.id = id;
    }

    protected EntityId() {
        this.id = UUID.randomUUID().toString();
    }
}
