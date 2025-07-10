package com.boardly.shared.domain.valueobject;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@Getter
@EqualsAndHashCode
public class EntityId {

    private final String id;

    protected EntityId(final String id) {
        this.id = id;
    }

    protected EntityId() {
        this.id = UlidCreator.getUlid().toString();
    }

    @Override
    public String toString() {
        return id;
    }
}
