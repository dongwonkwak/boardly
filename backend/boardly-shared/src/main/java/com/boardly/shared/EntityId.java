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

    /**
     * EntityId 형식이 유효한지 검증 (예외 없이 boolean 반환)
     * 
     * @param value  검증할 문자열
     * @param prefix 해당 EntityId의 prefix
     * @return 유효하면 true, 그렇지 않으면 false
     */
    protected static boolean isValidFormat(String value, String prefix) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        if (!value.startsWith(prefix)) {
            return false;
        }

        // prefix 제외한 부분이 26자 ULID인지 검증
        String ulidPart = value.substring(prefix.length());
        if (ulidPart.length() != 26) {
            return false;
        }

        // ULID 문자 집합 검증 (Crockford's Base32: 0-9, A-Z except I, L, O, U)
        return ulidPart.matches("^[0-9A-HJKMNP-TV-Z]{26}$");
    }

    protected void validate(String value) {
        if (!isValidFormat(value, getPrefix())) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("EntityId cannot be null or empty");
            }

            String prefix = getPrefix();
            if (!value.startsWith(prefix)) {
                throw new IllegalArgumentException("Invalid EntityId format: must start with '" + prefix + "'");
            }

            String ulidPart = value.substring(prefix.length());
            if (ulidPart.length() != 26) {
                throw new IllegalArgumentException("EntityId ULID part must be 26 characters long");
            }

            throw new IllegalArgumentException("Invalid EntityId format: ULID part contains invalid characters");
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