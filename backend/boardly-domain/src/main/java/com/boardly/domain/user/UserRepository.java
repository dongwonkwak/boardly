package com.boardly.domain.user;

import java.util.Optional;

/**
 * 사용자 저장소 인터페이스
 */
public interface UserRepository {

    /**
     * 사용자 저장
     */
    User save(User user);

    /**
     * ID로 사용자 조회
     */
    Optional<User> findById(UserId id);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 사용자명으로 사용자 조회
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 사용자명 존재 여부 확인
     */
    boolean existsByUsername(String username);
}
