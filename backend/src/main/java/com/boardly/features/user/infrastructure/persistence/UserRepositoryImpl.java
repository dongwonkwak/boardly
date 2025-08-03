package com.boardly.features.user.infrastructure.persistence;

import com.boardly.features.user.application.dto.UserNameDto;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.repository.UserRepository;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    @CacheEvict(value = { "users", "userNames" }, key = "#user.userId.id")
    public Either<Failure, User> save(User user) {
        try {
            UserEntity savedEntity;

            // 새로운 객체인지 기존 객체인지 판단
            if (user.isNew()) {
                // 새로운 객체 저장
                log.debug("새로운 사용자 저장: userId={}, email={}", user.getUserId().getId(), user.getEmail());
                UserEntity userEntity = UserEntity.fromDomainEntity(user);
                savedEntity = userJpaRepository.save(userEntity);
                log.debug("새로운 사용자 저장 완료: userId={}, email={}", savedEntity.getUserId(), savedEntity.getEmail());
            } else {
                // 기존 객체 업데이트
                log.debug("기존 사용자 업데이트: userId={}, email={}", user.getUserId().getId(), user.getEmail());
                Optional<UserEntity> existingEntity = userJpaRepository.findById(user.getUserId().getId());

                if (existingEntity.isEmpty()) {
                    return Either.left(Failure.ofNotFound("USER_NOT_FOUND"));
                }

                UserEntity entityToUpdate = existingEntity.get();
                entityToUpdate.updateFromDomainEntity(user);
                savedEntity = userJpaRepository.save(entityToUpdate);
            }

            return Either.right(savedEntity.toDomainEntity());

        } catch (DataIntegrityViolationException e) {
            log.error("사용자 저장 중 중복 이메일 오류: {}", e.getMessage());
            return Either.left(Failure.ofConflict("DUPLICATE_EMAIL"));
        } catch (Exception e) {
            log.error("사용자 저장 중 오류 발생: {}", e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    @Override
    public Optional<User> findById(UserId userId) {
        log.debug("findById: userId={}", userId.getId());
        return userJpaRepository.findById(userId.getId())
                .map(UserEntity::toDomainEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntity::toDomainEntity);
    }

    @Override
    @CacheEvict(value = { "users", "userNames" }, key = "#userId.id")
    public Either<Failure, Void> delete(UserId userId) {
        try {
            if (userJpaRepository.existsById(userId.getId())) {
                userJpaRepository.deleteById(userId.getId());
                return Either.right(null);
            } else {
                return Either.left(Failure.ofNotFound("USER_NOT_FOUND"));
            }
        } catch (Exception e) {
            log.error("사용자 삭제 중 오류 발생: {}", e.getMessage());
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<UserNameDto> findUserNameById(UserId userId) {
        log.debug("findUserNameById: userId={}", userId.getId());
        return userJpaRepository.findUserNameById(userId.getId())
                .map(entity -> new UserNameDto(entity.getFirstName(), entity.getLastName()));
    }
}