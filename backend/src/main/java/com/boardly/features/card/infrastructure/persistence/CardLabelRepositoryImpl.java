package com.boardly.features.card.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardLabelRepository;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.label.infrastructure.persistence.LabelEntity;
import com.boardly.features.label.infrastructure.persistence.LabelMapper;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CardLabelRepositoryImpl implements CardLabelRepository {
    private final CardLabelJpaRepository cardLabelJpaRepository;
    private final LabelMapper labelMapper;

    @Override
    public Either<Failure, Void> addLabelToCard(CardId cardId, LabelId labelId) {
        log.debug("카드에 라벨 추가 시작: cardId={}, labelId={}", cardId.getId(), labelId.getId());

        try {
            // 이미 존재하는지 확인
            if (cardLabelJpaRepository.existsByCardIdAndLabelId(cardId.getId(), labelId.getId())) {
                log.warn("이미 존재하는 카드-라벨 연결: cardId={}, labelId={}", cardId.getId(), labelId.getId());
                return Either.left(Failure.ofConflict("이미 해당 라벨이 카드에 적용되어 있습니다"));
            }

            CardLabelEntity entity = CardLabelEntity.create(cardId.getId(), labelId.getId());
            cardLabelJpaRepository.save(entity);
            log.debug("카드에 라벨 추가 완료: cardId={}, labelId={}", cardId.getId(), labelId.getId());

            return Either.right(null);
        } catch (Exception e) {
            log.error("카드에 라벨 추가 실패: cardId={}, labelId={}, 예외={}",
                    cardId.getId(), labelId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("카드에 라벨 추가 실패: " + e.getMessage()));
        }
    }

    @Override
    public Either<Failure, Void> removeLabelFromCard(CardId cardId, LabelId labelId) {
        log.debug("카드에서 라벨 제거 시작: cardId={}, labelId={}", cardId.getId(), labelId.getId());

        try {
            cardLabelJpaRepository.deleteByCardIdAndLabelId(cardId.getId(), labelId.getId());
            log.debug("카드에서 라벨 제거 완료: cardId={}, labelId={}", cardId.getId(), labelId.getId());

            return Either.right(null);
        } catch (Exception e) {
            log.error("카드에서 라벨 제거 실패: cardId={}, labelId={}, 예외={}",
                    cardId.getId(), labelId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("카드에서 라벨 제거 실패: " + e.getMessage()));
        }
    }

    @Override
    public List<LabelId> findLabelIdsByCardId(CardId cardId) {
        log.debug("카드별 라벨 ID 조회 시작: cardId={}", cardId.getId());
        List<String> labelIdStrings = cardLabelJpaRepository.findLabelIdsByCardId(cardId.getId());
        List<LabelId> labelIds = labelIdStrings.stream()
                .map(LabelId::new)
                .toList();
        log.debug("카드별 라벨 ID 조회 완료: cardId={}, 라벨 수={}", cardId.getId(), labelIds.size());
        return labelIds;
    }

    @Override
    public List<Label> findLabelsByCardId(CardId cardId) {
        log.debug("카드별 라벨 조회 시작: cardId={}", cardId.getId());

        // JOIN 쿼리로 한 번에 조회
        List<LabelEntity> labelEntities = cardLabelJpaRepository.findLabelsByCardId(cardId.getId());

        // Entity를 Domain 객체로 변환
        List<Label> labels = labelEntities.stream()
                .map(labelMapper::toDomain)
                .toList();

        log.debug("카드별 라벨 조회 완료: cardId={}, 라벨 수={}", cardId.getId(), labels.size());
        return labels;
    }

    @Override
    public List<CardId> findCardIdsByLabelId(LabelId labelId) {
        log.debug("라벨별 카드 ID 조회 시작: labelId={}", labelId.getId());
        List<String> cardIdStrings = cardLabelJpaRepository.findCardIdsByLabelId(labelId.getId());
        List<CardId> cardIds = cardIdStrings.stream()
                .map(CardId::new)
                .toList();
        log.debug("라벨별 카드 ID 조회 완료: labelId={}, 카드 수={}", labelId.getId(), cardIds.size());
        return cardIds;
    }

    @Override
    public boolean existsByCardIdAndLabelId(CardId cardId, LabelId labelId) {
        return cardLabelJpaRepository.existsByCardIdAndLabelId(cardId.getId(), labelId.getId());
    }

    @Override
    public int countLabelsByCardId(CardId cardId) {
        return cardLabelJpaRepository.countByCardId(cardId.getId());
    }

    @Override
    public int countCardsByLabelId(LabelId labelId) {
        return cardLabelJpaRepository.countByLabelId(labelId.getId());
    }

    @Override
    public Either<Failure, Void> deleteByCardId(CardId cardId) {
        log.debug("카드별 라벨 연결 삭제 시작: cardId={}", cardId.getId());

        try {
            cardLabelJpaRepository.deleteByCardId(cardId.getId());
            log.debug("카드별 라벨 연결 삭제 완료: cardId={}", cardId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("카드별 라벨 연결 삭제 실패: cardId={}, 예외={}", cardId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("카드별 라벨 연결 삭제 실패: " + e.getMessage()));
        }
    }

    @Override
    public Either<Failure, Void> deleteByLabelId(LabelId labelId) {
        log.debug("라벨별 카드 연결 삭제 시작: labelId={}", labelId.getId());

        try {
            cardLabelJpaRepository.deleteByLabelId(labelId.getId());
            log.debug("라벨별 카드 연결 삭제 완료: labelId={}", labelId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("라벨별 카드 연결 삭제 실패: labelId={}, 예외={}", labelId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("라벨별 카드 연결 삭제 실패: " + e.getMessage()));
        }
    }

    @Override
    public Either<Failure, Void> deleteByBoardId(BoardId boardId) {
        log.debug("보드별 카드-라벨 연결 삭제 시작: boardId={}", boardId.getId());

        try {
            cardLabelJpaRepository.deleteByBoardId(boardId.getId());
            log.debug("보드별 카드-라벨 연결 삭제 완료: boardId={}", boardId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("보드별 카드-라벨 연결 삭제 실패: boardId={}, 예외={}", boardId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("보드별 카드-라벨 연결 삭제 실패: " + e.getMessage()));
        }
    }
}
