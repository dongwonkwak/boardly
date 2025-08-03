package com.boardly.features.user.application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.boardly.features.user.application.dto.UserNameDto;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserFinder {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#userId.id")
    public User findUserOrThrow(UserId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId.getId()));
    }

    @Cacheable(value = "users", key = "#userId.id")
    public boolean checkUserExists(UserId userId) {
        return userRepository.findById(userId).isPresent();
    }

    @Cacheable(value = "userNames", key = "#userId.id")
    public Optional<UserNameDto> findUserNameById(UserId userId) {
        return userRepository.findUserNameById(userId);
    }
}
