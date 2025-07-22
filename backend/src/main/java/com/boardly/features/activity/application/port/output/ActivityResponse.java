package com.boardly.features.activity.application.port.output;

import java.time.Instant;
import java.util.Map;

import com.boardly.features.activity.domain.model.ActivityType;

import lombok.Builder;

@Builder
public record ActivityResponse(
        String id,
        ActivityType type,
        ActorResponse actor,
        Instant timestamp,
        Map<String, Object> payload) {

}
