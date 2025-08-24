package com.boardly.shared;

import com.github.f4b6a3.ulid.UlidCreator;

import java.util.Objects;

/**
 * 모든 엔티티 ID의 기본 클래스
 */
public abstract class EntityId {
    private final String value;

    protected EntityId() {
        this.value = null; // JPA를 위한 기본 생성자
    }

    protected EntityId(String value) {
        validate(value);
        this.value = value;
    }

    protected static String generateWithPrefix(String prefix) {
        return prefix + UlidCreator.getUlid().toString();
    }

    protected abstract String getPrefix();

    protected void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("EntityId cannot be null or empty");
        }

        String prefix = getPrefix();
        if (!value.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid EntityId format: must start with '" + prefix + "'");
        }

        // prefix 제외한 부분이 26자 ULID인지 검증
        String ulidPart = value.substring(prefix.length());
        if (ulidPart.length() != 26) {
            throw new IllegalArgumentException("EntityId ULID part must be 26 characters long");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EntityId))
            return false;
        EntityId entityId = (EntityId) o;
        return Objects.equals(value, entityId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}