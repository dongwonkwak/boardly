package com.boardly.features.boardlist.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardListMapper {
  
  /**
   * 도메인 모델을 JPA 엔티티로 변환합니다.
   * 
   * @param boardList 변환할 도메인 모델
   * @return 변환된 JPA 엔티티
   */
  public BoardListEntity toEntity(BoardList boardList) {
    if (boardList == null) {
      return null;
    }

    return BoardListEntity.builder()
            .listId(boardList.getListId().getId())
            .boardId(boardList.getBoardId().getId())
            .title(boardList.getTitle())
            .description(boardList.getDescription())
            .position(boardList.getPosition())
            .color(boardList.getColor() != null ? boardList.getColor().color() : null)
            .createdAt(boardList.getCreatedAt())
            .updatedAt(boardList.getUpdatedAt())
            .build();
  }

  /**
   * JPA 엔티티를 도메인 모델로 변환합니다.
   * 
   * @param entity 변환할 JPA 엔티티
   * @return 변환된 도메인 모델
   */
  public BoardList toDomain(BoardListEntity entity) {
    if (entity == null) {
      return null;
    }

    ListColor color = null;
    if (entity.getColor() != null) {
      try {
        color = ListColor.of(entity.getColor());
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 색상 값: {}, listId: {}", entity.getColor(), entity.getListId());
        color = ListColor.defaultColor(); // 기본값으로 설정
      }
    }

    return BoardList.builder()
            .listId(new ListId(entity.getListId()))
            .boardId(new BoardId(entity.getBoardId()))
            .title(entity.getTitle())
            .description(entity.getDescription())
            .position(entity.getPosition())
            .color(color)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
  }

  /**
   * 도메인 모델의 변경사항을 기존 JPA 엔티티에 적용합니다.
   * 업데이트 작업 시 성능 최적화를 위해 사용됩니다.
   * 
   * @param boardList 변경된 도메인 모델
   * @param entity 업데이트할 JPA 엔티티
   */
  public void updateEntity(BoardList boardList, BoardListEntity entity) {
    if (boardList == null || entity == null) {
      return;
    }

    entity.updateTitle(boardList.getTitle());
    entity.updateDescription(boardList.getDescription());
    entity.updatePosition(boardList.getPosition());
    entity.updateColor(boardList.getColor() != null ? boardList.getColor().color() : null);
  } 
}
