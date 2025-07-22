package com.boardly.features.user.application.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserFinder {

    private final UserRepository userRepository;

    public User findUserOrThrow(UserId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId.getId()));
    }

    public boolean checkUserExists(UserId userId) {
        return userRepository.findById(userId).isPresent();
    }
}
