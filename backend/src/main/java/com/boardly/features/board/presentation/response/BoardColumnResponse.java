package com.boardly.features.board.presentation.response;

import com.boardly.features.boardlist.domain.model.BoardList;

import java.util.List;

/**
 * 보드 컬럼 응답 DTO
 * 
 * @param columnId    컬럼 ID
 * @param columnName  컬럼 이름
 * @param columnColor 컬럼 색상
 * @param position    위치
 * @param cardCount   카드 개수
 * @param cards       카드 목록
 * 
 * @since 1.0.0
 */
public record BoardColumnResponse(
        String columnId,
        String columnName,
        String columnColor,
        int position,
        int cardCount,
        List<BoardCardResponse> cards) {

    /**
     * BoardList 도메인 모델을 BoardColumnResponse로 변환합니다.
     * 
     * @param boardList 변환할 BoardList 도메인 모델
     * @param cards     카드 목록
     * @return BoardColumnResponse 객체
     */
    public static BoardColumnResponse from(BoardList boardList, List<BoardCardResponse> cards) {
        return new BoardColumnResponse(
                boardList.getListId().getId(),
                boardList.getTitle(),
                boardList.getColor().color(),
                boardList.getPosition(),
                cards.size(),
                cards);
    }
}