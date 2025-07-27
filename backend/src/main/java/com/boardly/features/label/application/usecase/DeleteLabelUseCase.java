package com.boardly.features.label.application.usecase;

import com.boardly.features.label.application.port.input.DeleteLabelCommand;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

public interface DeleteLabelUseCase {

    /**
     * 라벨을 삭제합니다.
     * 
     * @param command 라벨 삭제 명령. 삭제할 라벨ID, 사용자ID를 포함합니다.
     * @return 삭제 성공 시 {@code Either.right(null)}, 실패 시 {@code Either.left(Failure)}
     * 
     * @see DeleteLabelCommand
     * @see Label
     * @see Failure
     * @see Either
     */
    Either<Failure, Void> deleteLabel(DeleteLabelCommand command);

}
