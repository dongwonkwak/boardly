package com.boardly.features.user.domain.port;

import com.boardly.features.user.domain.User;
import com.boardly.shared.common.value.UserId;
import java.util.Optional;

public interface UserFinder {
    User findUserOrThrow(UserId userId);

    boolean userExists(UserId userId);

    Optional<UserNameDto> findUserNameById(UserId userid);

    Optional<User> findUserByEmail(String email);

    record UserNameDto(String firstName, String lastName) {}
}
