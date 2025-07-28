package com.boardly.features.card.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.boardly.features.card.domain.repository.CardMemberRepository;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CardMemberRepositoryImpl implements CardMemberRepository {

    private final CardMemberJpaRepository cardMemberJpaRepository;
    private final CardMemberMapper cardMemberMapper;

    @Override
    public Either<Failure, Void> addMember(CardId cardId, UserId userId) {
        log.debug("카드 담당자 추가 시작: cardId={}, userId={}", cardId.getId(), userId.getId());

        try {
            // 이미 존재하는지 확인
            if (cardMemberJpaRepository.existsByCardIdAndUserId(cardId.getId(), userId.getId())) {
                log.warn("이미 존재하는 카드 담당자: cardId={}, userId={}", cardId.getId(), userId.getId());
                return Either.left(Failure.ofConflict("이미 해당 사용자가 카드에 할당되어 있습니다"));
            }

            CardMemberEntity entity = CardMemberEntity.create(cardId.getId(), userId.getId());
            cardMemberJpaRepository.save(entity);
            log.debug("카드 담당자 추가 완료: cardId={}, userId={}", cardId.getId(), userId.getId());

            return Either.right(null);
        } catch (Exception e) {
            log.error("카드 담당자 추가 실패: cardId={}, userId={}, 예외={}", cardId.getId(), userId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("카드 담당자 추가 실패: " + e.getMessage()));
        }
    }

    @Override
    public Either<Failure, Void> removeMember(CardId cardId, UserId userId) {
        log.debug("카드 담당자 제거 시작: cardId={}, userId={}", cardId.getId(), userId.getId());

        try {
            cardMemberJpaRepository.deleteByCardIdAndUserId(cardId.getId(), userId.getId());
            log.debug("카드 담당자 제거 완료: cardId={}, userId={}", cardId.getId(), userId.getId());

            return Either.right(null);
        } catch (Exception e) {
            log.error("카드 담당자 제거 실패: cardId={}, userId={}, 예외={}", cardId.getId(), userId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("카드 담당자 제거 실패: " + e.getMessage()));
        }
    }

    @Override
    public List<CardMember> findByCardIdOrderByAssignedAt(CardId cardId) {
        log.debug("카드별 담당자 조회 시작: cardId={}", cardId.getId());
        var entities = cardMemberJpaRepository.findByCardIdOrderByAssignedAt(cardId.getId());
        var cardMembers = entities.stream()
                .map(cardMemberMapper::toDomain)
                .toList();
        log.debug("카드별 담당자 조회 완료: cardId={}, 담당자 수={}", cardId.getId(), cardMembers.size());
        return cardMembers;
    }

    @Override
    public List<CardMember> findByUserIdOrderByAssignedAtDesc(UserId userId) {
        log.debug("사용자별 담당 카드 조회 시작: userId={}", userId.getId());
        var entities = cardMemberJpaRepository.findByUserIdOrderByAssignedAtDesc(userId.getId());
        var cardMembers = entities.stream()
                .map(cardMemberMapper::toDomain)
                .toList();
        log.debug("사용자별 담당 카드 조회 완료: userId={}, 담당 카드 수={}", userId.getId(), cardMembers.size());
        return cardMembers;
    }

    @Override
    public boolean existsByCardIdAndUserId(CardId cardId, UserId userId) {
        return cardMemberJpaRepository.existsByCardIdAndUserId(cardId.getId(), userId.getId());
    }

    @Override
    public Either<Failure, Void> deleteByCardId(CardId cardId) {
        log.debug("카드별 담당자 관계 삭제 시작: cardId={}", cardId.getId());

        try {
            cardMemberJpaRepository.deleteByCardId(cardId.getId());
            log.debug("카드별 담당자 관계 삭제 완료: cardId={}", cardId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("카드별 담당자 관계 삭제 실패: cardId={}, 예외={}", cardId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("카드별 담당자 관계 삭제 실패: " + e.getMessage()));
        }
    }

    @Override
    public Either<Failure, Void> deleteByUserId(UserId userId) {
        log.debug("사용자별 담당자 관계 삭제 시작: userId={}", userId.getId());

        try {
            cardMemberJpaRepository.deleteByUserId(userId.getId());
            log.debug("사용자별 담당자 관계 삭제 완료: userId={}", userId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("사용자별 담당자 관계 삭제 실패: userId={}, 예외={}", userId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("사용자별 담당자 관계 삭제 실패: " + e.getMessage()));
        }
    }
}
