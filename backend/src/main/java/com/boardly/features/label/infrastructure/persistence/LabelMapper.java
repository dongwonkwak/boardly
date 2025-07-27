package com.boardly.features.label.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.board.domain.model.BoardId;

@Component
public class LabelMapper {

    /**
     * 도메인 객체를 엔티티로 변환
     */
    public LabelEntity toEntity(Label label) {
        return LabelEntity.from(label);
    }

    /**
     * 엔티티를 도메인 객체로 변환
     */
    public Label toDomain(LabelEntity entity) {
        return Label.restore(
                new LabelId(entity.getLabelId()),
                new BoardId(entity.getBoardId()),
                entity.getName(),
                entity.getColor(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

}
