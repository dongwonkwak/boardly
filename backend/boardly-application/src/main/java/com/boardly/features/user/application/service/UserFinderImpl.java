package com.boardly.features.user.application.service;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.boardly.features.user.domain.User;
import com.boardly.shared.common.value.UserId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("userFinder")
@RequiredArgsConstructor
public class UserFinderImpl implements com.boardly.features.user.domain.port.UserFinder {

    private final com.boardly.features.user.domain.port.UserRepository userRepository;

    @Override
    @Cacheable(value = "users", key = "#userId.id", unless = "#result == null")
    public User findUserOrThrow(UserId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId.getId()));
    }

    @Override
    public boolean userExists(UserId userId) {
        log.info("사용자 존재 확인: userId={}", userId.getId());
        try {
            findUserOrThrow(userId);
            return true;
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    @Override
    @Cacheable(value = "userNames", key = "#userId.id")
    public Optional<com.boardly.features.user.domain.port.UserFinder.UserNameDto> findUserNameById(UserId userId) {
        return userRepository.findById(userId)
            .map(u -> new com.boardly.features.user.domain.port.UserFinder.UserNameDto(u.getFirstName(), u.getLastName()));
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
