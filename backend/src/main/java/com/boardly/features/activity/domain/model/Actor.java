package com.boardly.features.activity.domain.model;

import io.micrometer.common.lang.NonNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Actor {
    @NonNull
    String id;

    @NonNull
    String firstName;

    @NonNull
    String lastName;

    @NonNull
    String profileImageUrl;

    public static Actor of(String id, String firstName, String lastName, String profileImageUrl) {
        return Actor.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
