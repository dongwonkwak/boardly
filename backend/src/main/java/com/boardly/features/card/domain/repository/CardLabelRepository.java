package com.boardly.features.card.domain.repository;

import java.util.List;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface CardLabelRepository {
    /**
     * 카드에 라벨을 추가합니다.
     */
    Either<Failure, Void> addLabelToCard(CardId cardId, LabelId labelId);

    /**
     * 카드에서 라벨을 제거합니다.
     */
    Either<Failure, Void> removeLabelFromCard(CardId cardId, LabelId labelId);

    /**
     * 카드별 라벨 ID 목록 조회
     */
    List<LabelId> findLabelIdsByCardId(CardId cardId);

    /**
     * 카드별 라벨 목록 조회 (Label 객체 반환)
     */
    List<Label> findLabelsByCardId(CardId cardId);

    /**
     * 라벨별 카드 ID 목록 조회
     */
    List<CardId> findCardIdsByLabelId(LabelId labelId);

    /**
     * 특정 카드-라벨 연결 존재 확인
     */
    boolean existsByCardIdAndLabelId(CardId cardId, LabelId labelId);

    /**
     * 카드별 라벨 수 조회
     */
    int countLabelsByCardId(CardId cardId);

    /**
     * 라벨별 카드 수 조회
     */
    int countCardsByLabelId(LabelId labelId);

    /**
     * 카드 삭제 시 관련 라벨 연결 모두 삭제
     */
    Either<Failure, Void> deleteByCardId(CardId cardId);

    /**
     * 라벨 삭제 시 관련 카드 연결 모두 삭제
     */
    Either<Failure, Void> deleteByLabelId(LabelId labelId);

    /**
     * 보드의 모든 카드-라벨 연결 삭제 (보드 삭제 시)
     */
    Either<Failure, Void> deleteByBoardId(BoardId boardId);
}
