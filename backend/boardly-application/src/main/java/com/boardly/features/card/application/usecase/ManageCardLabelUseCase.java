package com.boardly.features.card.application.usecase;

import java.util.List;

import com.boardly.features.card.application.command.AddCardLabelCommand;
import com.boardly.features.card.application.command.RemoveCardLabelCommand;
import com.boardly.shared.common.value.CardId;
import com.boardly.features.label.domain.Label;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

public interface ManageCardLabelUseCase {
    /**
     * 카드에 라벨을 추가합니다.
     */
    Either<Failure, Void> addLabel(AddCardLabelCommand command);

    /**
     * 카드에서 라벨을 제거합니다.
     */
    Either<Failure, Void> removeLabel(RemoveCardLabelCommand command);

    /**
     * 카드에 할당된 라벨 목록 조회
     */
    List<Label> getCardLabels(CardId cardId, UserId requesterId);

}
