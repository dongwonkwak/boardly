package com.boardly.features.user.domain.repository;

import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

import java.util.Optional;

/**
 * 사용자 도메인 Repository 인터페이스
 * 도메인 레이어에서 정의하여 의존성 역전 원칙을 적용
 */
public interface UserRepository {

    /**
     * 사용자를 저장합니다.
     */
    Either<Failure, User> save(User user);

    /**
     * 사용자 ID로 사용자를 조회합니다.
     */
    Optional<User> findById(UserId userId);

    /**
     * 이메일로 사용자를 조회합니다.
     */
    Optional<User> findByEmail(String email);

    /**
     * 사용자를 삭제합니다.
     */
    Either<Failure, Void> delete(UserId userId);

    /**
     * 이메일이 이미 존재하는지 확인합니다.
     */
    boolean existsByEmail(String email);
} 