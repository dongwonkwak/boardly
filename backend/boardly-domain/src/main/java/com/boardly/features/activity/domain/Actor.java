package com.boardly.features.activity.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Actor {

    String id;

    String firstName;

    String lastName;

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
