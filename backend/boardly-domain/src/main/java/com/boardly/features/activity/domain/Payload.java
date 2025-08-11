package com.boardly.features.activity.domain;

import java.util.Map;

import lombok.Value;

@Value
public class Payload {
    Map<String, Object> data;

    public static Payload of(Map<String, Object> data) {
        return new Payload(data);
    }

    public static Payload empty() {
        return new Payload(Map.of());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    public String getString(String key) {
        return get(key, String.class);
    }

    public Integer getInteger(String key) {
        return get(key, Integer.class);
    }

    public Boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }
}
