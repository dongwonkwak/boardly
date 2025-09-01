package com.boardly.features.user.domain;

public record UserProfile(String firstName, String lastName) {

    public UserProfile(String firstName, String lastName) {
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
    }

    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
}
