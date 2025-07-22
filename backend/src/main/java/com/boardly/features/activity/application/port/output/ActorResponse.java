package com.boardly.features.activity.application.port.output;

import lombok.Builder;

@Builder
public record ActorResponse(
        String id,
        String firstName,
        String lastName,
        String profileImageUrl) {

}
