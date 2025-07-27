package com.boardly.features.label.application.usecase;

import com.boardly.features.label.application.port.input.CreateLabelCommand;
import com.boardly.features.label.domain.model.Label;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface CreateLabelUseCase {

    /**
     * 라벨을 생성합니다.
     * 
     * @param command 라벨 생성 명령. 라벨을 생성할 보드ID, 생성자ID, 라벨 이름, 라벨 색상을 포함합니다.
     * @return 생성 성공 시 {@code Either.right(Label)}, 실패 시
     *         {@code Either.left(Failure)}
     * 
     * @see CreateLabelCommand
     * @see Label
     * @see Failure
     * @see Either
     */
    Either<Failure, Label> createLabel(CreateLabelCommand command);

}
