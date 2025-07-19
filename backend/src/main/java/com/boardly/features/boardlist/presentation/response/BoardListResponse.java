package com.boardly.features.boardlist.presentation.response;

import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * 보드 리스트 응답 DTO
 * 
 * @param listId 리스트 ID
 * @param title 리스트 제목
 * @param description 리스트 설명
 * @param position 리스트 위치 (0부터 시작)
 * @param color 리스트 색상
 * @param boardId 소속 보드 ID
 * @param createdAt 생성 시간
 * @param updatedAt 수정 시간
 * 
 * @since 1.0.0
 */
public record BoardListResponse(
    String listId,
    String title,
    String description,
    int position,
    String color,
    String boardId,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant createdAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant updatedAt
) {
    
    /**
     * BoardList 도메인 모델을 BoardListResponse로 변환합니다.
     * 
     * @param boardList 변환할 BoardList 도메인 모델
     * @return BoardListResponse 객체
     */
    public static BoardListResponse from(BoardList boardList) {
        return new BoardListResponse(
            boardList.getListId().getId(),
            boardList.getTitle(),
            boardList.getDescription(),
            boardList.getPosition(),
            boardList.getColor().color(),
            boardList.getBoardId().getId(),
            boardList.getCreatedAt(),
            boardList.getUpdatedAt()
        );
    }
} 