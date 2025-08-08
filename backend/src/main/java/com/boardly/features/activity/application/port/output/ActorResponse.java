package com.boardly.features.activity.application.port.output;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ActorResponse {
    String id;
    String firstName;
    String lastName;
    String profileImageUrl;
}
