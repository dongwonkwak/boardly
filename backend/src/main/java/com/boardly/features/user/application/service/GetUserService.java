package com.boardly.features.user.application.service;

import org.springframework.stereotype.Service;

import com.boardly.features.user.application.usecase.GetUserUseCase;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.repository.UserRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

  private final UserRepository userRepository;
  private final ValidationMessageResolver validationMessageResolver;

  @Transactional(readOnly = true)
  @Override
  public Either<Failure, User> get(UserId userId) {
    try {
      return userRepository.findById(userId)
        .map(Either::<Failure, User>right)
        .orElseGet(() -> Either.left(Failure.ofNotFound(validationMessageResolver.getMessage("validation.user.email.not.found"))));
    } catch (Exception e) {
      log.error("사용자 조회 실패: userId={}, error={}", userId, e.getMessage());
      return Either.left(Failure.ofInternalServerError(e.getMessage()));
    }
  }
}