package com.boardly.features.activity.application.dto;

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
