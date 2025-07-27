package com.boardly.features.label.application.usecase;

import com.boardly.features.label.application.port.input.UpdateLabelCommand;
import com.boardly.features.label.domain.model.Label;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface UpdateLabelUseCase {

    /**
     * 라벨을 수정합니다.
     * 
     * @param command 라벨 수정 명령. 수정할 라벨ID, 수정자ID, 라벨 이름, 라벨 색상을 포함합니다.
     * @return 수정 성공 시 {@code Either.right(Label)}, 실패 시
     *         {@code Either.left(Failure)}
     * 
     * @see UpdateLabelCommand
     * @see Label
     * @see Failure
     * @see Either
     */
    Either<Failure, Label> updateLabel(UpdateLabelCommand command);

}
