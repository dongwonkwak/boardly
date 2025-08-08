package com.boardly.features.activity.application.port.output;

import com.boardly.features.activity.domain.model.ActivityType;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ActivityResponse {

    String id;
    ActivityType type;
    ActorResponse actor;

    @Builder.Default
    Instant timestamp = Instant.now();

    @Builder.Default
    Map<String, Object> payload = new HashMap<>();

    String boardName;
    String boardId;
}
