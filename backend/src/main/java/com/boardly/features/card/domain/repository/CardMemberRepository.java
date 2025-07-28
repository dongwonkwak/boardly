package com.boardly.features.card.domain.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

@Repository
public interface CardMemberRepository {
    /**
     * 카드에 담당자를 추가합니다.
     */
    Either<Failure, Void> addMember(CardId cardId, UserId userId);

    /**
     * 카드에서 담당자를 제거합니다.
     */
    Either<Failure, Void> removeMember(CardId cardId, UserId userId);

    /**
     * 카드별 담당자 조회
     */
    List<CardMember> findByCardIdOrderByAssignedAt(CardId cardId);

    /**
     * 사용자별 담당 카드 조회
     */
    List<CardMember> findByUserIdOrderByAssignedAtDesc(UserId userId);

    /**
     * 특정 카드-사용자 담당 관계 존재 확인
     */
    boolean existsByCardIdAndUserId(CardId cardId, UserId userId);

    /**
     * 카드 삭제 시 관련 담당자 관계 모두 삭제
     */
    Either<Failure, Void> deleteByCardId(CardId cardId);

    /**
     * 사용자 삭제 시 관련 담당자 관계 모두 삭제
     */
    Either<Failure, Void> deleteByUserId(UserId userId);
}
