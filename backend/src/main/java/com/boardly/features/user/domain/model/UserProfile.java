package com.boardly.features.user.domain.model;

import static org.apache.commons.lang3.StringUtils.trim;

public record UserProfile(String firstName, String lastName) {

    public UserProfile(String firstName, String lastName) {
        this.firstName = trim(firstName);
        this.lastName = trim(lastName);
    }

    public String getFullName() {
        return String.format("%s %s", lastName, firstName);
    }
}
