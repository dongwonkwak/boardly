package com.boardly.features.label.application.usecase;

import java.util.List;

import com.boardly.shared.common.value.BoardId;
import com.boardly.features.label.domain.Label;
import com.boardly.shared.common.value.LabelId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

public interface GetLabelUseCase {

    /**
     * 라벨을 조회합니다.
     * 
     * @param labelId     조회할 라벨ID
     * @param requesterId 조회 요청자ID
     * @return 조회 성공 시 {@code Either.right(Label)}, 실패 시
     *         {@code Either.left(Failure)}
     * 
     * @see LabelId
     * @see UserId
     * @see Label
     * @see Failure
     * @see Either
     */
    Either<Failure, Label> getLabel(LabelId labelId, UserId requesterId);

    /**
     * 보드의 라벨 목록을 조회합니다.
     * 
     * @param boardId     조회할 보드ID
     * @param requesterId 조회 요청자ID
     * @return 조회 성공 시 {@code Either.right(List<Label>)}, 실패 시
     *         {@code Either.left(Failure)}
     * 
     * @see BoardId
     * @see UserId
     * @see Label
     * @see Failure
     * @see Either
     */
    Either<Failure, List<Label>> getBoardLabels(BoardId boardId, UserId requesterId);

}
