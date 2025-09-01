package com.boardly.features.label.domain.port;

import com.boardly.features.label.domain.Label;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.LabelId;
import io.vavr.control.Either;
import java.util.List;
import java.util.Optional;

public interface LabelRepository {
    /**
     * 라벨을 저장합니다.
     */
    Either<Failure, Label> save(Label label);

    /**
     * 라벨 ID로 라벨을 조회합니다.
     */
    Optional<Label> findById(LabelId labelId);

    /**
     * 보드별 라벨 조회 (이름순)
     */
    List<Label> findByBoardIdOrderByName(BoardId boardId);

    /**
     * 보드 내 라벨명 중복 확인
     */
    Optional<Label> findByBoardIdAndName(BoardId boardId, String name);

    /**
     * 라벨을 삭제합니다.
     */
    Either<Failure, Void> delete(LabelId labelId);

    /**
     * 라벨이 존재하는지 확인합니다.
     */
    boolean existsById(LabelId labelId);

    /**
     * 보드별 라벨 수 조회
     */
    int countByBoardId(BoardId boardId);

    /**
     * 보드 삭제 시 관련 라벨 모두 삭제
     */
    Either<Failure, Void> deleteByBoardId(BoardId boardId);
}
