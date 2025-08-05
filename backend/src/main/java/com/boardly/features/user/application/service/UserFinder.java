package com.boardly.features.user.application.service;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.boardly.features.user.application.dto.UserNameDto;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFinder {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#userId.id", unless = "#result == null")
    public User findUserOrThrow(UserId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId.getId()));
    }

    public boolean checkUserExists(UserId userId) {
        log.info("사용자 존재 확인: userId={}", userId.getId());
        try {
            findUserOrThrow(userId);
            return true;
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    @Cacheable(value = "userNames", key = "#userId.id")
    public Optional<UserNameDto> findUserNameById(UserId userId) {
        return userRepository.findUserNameById(userId);
    }
}
